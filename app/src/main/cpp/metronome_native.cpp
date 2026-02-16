#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>

#include <atomic>
#include <cmath>
#include <cstdint>
#include <memory>
#include <algorithm>

#define LOG_TAG "MetronomeNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace oboe;

static JavaVM* gJvm = nullptr;

// Global reference to the Java MetronomeEngine object and its tick method ID
static jobject gEngineObj = nullptr;
static jmethodID gOnTickFromNativeMid = nullptr;

static inline JNIEnv* getJniEnv(bool* didAttach) {
    *didAttach = false;
    if (!gJvm) return nullptr;

    JNIEnv* env = nullptr;
    const jint getEnvRes = gJvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
    if (getEnvRes == JNI_OK) return env;

    if (getEnvRes == JNI_EDETACHED) {
        if (gJvm->AttachCurrentThread(&env, nullptr) == JNI_OK) {
            *didAttach = true;
            return env;
        }
        return nullptr;
    }
    return nullptr;
}

static void callJavaTick(int beatInBar, bool isAccent) {
    if (!gEngineObj || !gOnTickFromNativeMid) return;

    bool didAttach = false;
    JNIEnv* env = getJniEnv(&didAttach);
    if (!env) return;

    env->CallVoidMethod(
            gEngineObj,
            gOnTickFromNativeMid,
            static_cast<jint>(beatInBar),
            static_cast<jboolean>(isAccent)
    );
    if (env->ExceptionCheck()) {
        env->ExceptionClear(); // don't crash audio thread
        LOGE("Exception while calling onTickFromNative");
    }

    if (didAttach) {
        gJvm->DetachCurrentThread();
    }
}

struct MetronomeState {
    std::atomic<int> bpm{120};
    std::atomic<int> beatsPerBar{4};

    std::atomic<bool> resetPhaseRequested{false};

    int32_t sampleRate = 48000;

    // Scheduling in frames
    int64_t absoluteFrame = 0;
    int64_t nextBeatFrame = 0;
    int beatInBar = 0;

    // Click synthesis state
    int clickFramesRemaining = 0;
    float clickPhase = 0.0f;
    float clickPhaseInc = 0.0f;
    float clickAmp = 0.0f;
};

static std::shared_ptr<AudioStream> gStream;
static std::unique_ptr<MetronomeState> gState;

static inline int64_t framesPerBeat(int bpm, int32_t sampleRate) {
    // framesPerBeat = sampleRate * 60 / bpm
    // keep it integer; jitter is bounded to < 1 frame per beat
    return (static_cast<int64_t>(sampleRate) * 60LL) / static_cast<int64_t>(bpm);
}

static void startClick(MetronomeState& st, bool accent) {
    const float freqHz = accent ? 1500.0f : 1000.0f;
    const float amp = accent ? 0.9f : 0.55f;

    st.clickFramesRemaining = st.sampleRate / 200; // ~5ms
    st.clickPhase = 0.0f;
    st.clickPhaseInc = 2.0f * static_cast<float>(M_PI) * freqHz / static_cast<float>(st.sampleRate);
    st.clickAmp = amp;
}

class MetronomeCallback final : public AudioStreamCallback {
public:
    DataCallbackResult onAudioReady(AudioStream* audioStream, void* audioData, int32_t numFrames) override {
        auto* out = static_cast<int16_t*>(audioData);
        auto& st = *gState;

        if (st.resetPhaseRequested.exchange(false, std::memory_order_acq_rel)) {
            st.beatInBar = 0;
            st.clickFramesRemaining = 0;

            // Make the next beat happen immediately (at the next sample we generate)
            st.nextBeatFrame = st.absoluteFrame;
        }

        const int bpm = std::max(1, st.bpm.load(std::memory_order_relaxed));
        const int bpb = std::max(1, st.beatsPerBar.load(std::memory_order_relaxed));
        const int64_t fpb = framesPerBeat(bpm, st.sampleRate);

        for (int32_t i = 0; i < numFrames; i++) {
            // If we reached a beat boundary, trigger click + Java tick
            if (st.absoluteFrame >= st.nextBeatFrame) {
                const bool accent = (st.beatInBar == 0);
                startClick(st, accent);
                callJavaTick(st.beatInBar, accent);

                st.beatInBar = (st.beatInBar + 1) % bpb;
                st.nextBeatFrame += fpb;
            }

            float s = 0.0f;
            if (st.clickFramesRemaining > 0) {
                // Simple decaying sine burst
                const float t = static_cast<float>(st.clickFramesRemaining) / (st.sampleRate / 200.0f);
                const float env = t * t; // quick decay
                s = std::sinf(st.clickPhase) * st.clickAmp * env;

                st.clickPhase += st.clickPhaseInc;
                st.clickFramesRemaining--;
            }

            // Convert float [-1..1] to I16
            const float clamped = std::fmax(-1.0f, std::fmin(1.0f, s));
            out[i] = static_cast<int16_t>(clamped * 32767.0f);

            st.absoluteFrame++;
        }

        return DataCallbackResult::Continue;
    }

    void onErrorAfterClose(AudioStream* /*stream*/, Result error) override {
        LOGE("Oboe stream error after close: %s", convertToText(error));
    }
};

static std::unique_ptr<MetronomeCallback> gCallback;

extern "C" jint JNI_OnLoad(JavaVM *vm, void* /*reserved*/) {
    gJvm = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_trishit_plectune_feature_metronome_data_MetronomeEngine_nativeStart(
        JNIEnv* env, jobject thiz, jint bpm, jint beatsPerBar) {

    // Stop existing stream if any
    if (gStream) {
        gStream->requestStop();
        gStream->close();
        gStream.reset();
    }

    if (gEngineObj) {
        env->DeleteGlobalRef(gEngineObj);
        gEngineObj = nullptr;
    }

    gEngineObj = env->NewGlobalRef(thiz);
    if (!gEngineObj) {
        LOGE("Failed to create global ref for engine object");
        return JNI_FALSE;
    }

    jclass cls = env->GetObjectClass(thiz);
    if (!cls) {
        LOGE("Failed to get MetronomeEngine class");
        return JNI_FALSE;
    }

    // IMPORTANT: must match Kotlin method name/signature exactly
    gOnTickFromNativeMid = env->GetMethodID(cls, "onTickFromNative", "(IZ)V");
    env->DeleteLocalRef(cls);

    if (!gOnTickFromNativeMid) {
        LOGE("Failed to find method onTickFromNative(IZ)V");
        return JNI_FALSE;
    }

    gState = std::make_unique<MetronomeState>();
    gState->bpm.store(static_cast<int>(bpm), std::memory_order_relaxed);
    gState->beatsPerBar.store(static_cast<int>(beatsPerBar), std::memory_order_relaxed);

    gCallback = std::make_unique<MetronomeCallback>();

    AudioStreamBuilder builder;
    builder.setDirection(Direction::Output);
    builder.setSharingMode(SharingMode::Exclusive);
    builder.setPerformanceMode(PerformanceMode::LowLatency);
    builder.setUsage(Usage::Media);
    builder.setContentType(ContentType::Sonification);
    builder.setChannelCount(1);
    builder.setFormat(AudioFormat::I16);
    builder.setCallback(gCallback.get());

    Result r = builder.openStream(gStream);
    if (r != Result::OK || !gStream) {
        LOGE("openStream failed: %s", convertToText(r));
        return JNI_FALSE;
    }

    gState->sampleRate = gStream->getSampleRate();
    gState->absoluteFrame = 0;
    gState->nextBeatFrame = 0;
    gState->beatInBar = 0;

    r = gStream->requestStart();
    if (r != Result::OK) {
        LOGE("requestStart failed: %s", convertToText(r));
        gStream->close();
        gStream.reset();
        return JNI_FALSE;
    }

    LOGI("Metronome started. sr=%d bpm=%d bpb=%d", gState->sampleRate, (int)bpm, (int)beatsPerBar);
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_trishit_plectune_feature_metronome_data_MetronomeEngine_nativeStop(
        JNIEnv* env, jobject /*thiz*/) {

    if (gStream) {
        gStream->requestStop();
        gStream->close();
        gStream.reset();
    }
    gCallback.reset();
    gState.reset();

    if (gEngineObj) {
        env->DeleteGlobalRef(gEngineObj);
        gEngineObj = nullptr;
    }
    gOnTickFromNativeMid = nullptr;

    LOGI("Metronome stopped.");
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_trishit_plectune_feature_metronome_data_MetronomeEngine_nativeSetBpm(
        JNIEnv* /*env*/, jobject /*thiz*/, jint bpm) {

    if (!gState) return JNI_FALSE;
    const int clamped = std::max(1, static_cast<int>(bpm));
    gState->bpm.store(clamped, std::memory_order_relaxed);
    gState->resetPhaseRequested.store(true, std::memory_order_release);
    return JNI_TRUE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_com_trishit_plectune_feature_metronome_data_MetronomeEngine_nativeSetBeatsPerBar(
        JNIEnv* /*env*/, jobject /*thiz*/, jint beatsPerBar) {

    if (!gState) return JNI_FALSE;

    const int clamped = std::max(1, static_cast<int>(beatsPerBar));
    gState->beatsPerBar.store(clamped, std::memory_order_relaxed);

    // Phase reset so beatInBar goes back to 0 and the next click is an accent
    gState->resetPhaseRequested.store(true, std::memory_order_release);

    return JNI_TRUE;
}