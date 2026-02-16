cmake_minimum_required(VERSION 3.22)

# Project name used in the build system
project("PlectuneMetronome")

# Define sources for our native library
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Fetch and build Oboe from source
include(FetchContent)
FetchContent_Declare(
    oboe
    URL https://github.com/google/oboe/archive/refs/tags/1.9.0.zip
)
FetchContent_MakeAvailable(oboe)

if(TARGET oboe AND NOT TARGET oboe::oboe)
    add_library(oboe::oboe ALIAS oboe)
endif()

add_library(
    metronome-native
    SHARED
    metronome_native.cpp
)

target_link_libraries(
    metronome-native
    PRIVATE
    oboe::oboe
    android
    jnigraphics
)