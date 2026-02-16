package com.trishit.plectune.feature.chords.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.trishit.plectune.R
import com.trishit.plectune.ui.components.AppTextureBackground
import com.trishit.plectune.ui.components.FretboardView
import com.trishit.plectune.ui.components.LiquidButton
import com.trishit.plectune.ui.theme.PlectuneGreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordScreen(
    viewModel: ChordViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }
    
    val rootsListState = rememberLazyListState()
    val variantsListState = rememberLazyListState()
    
    val pagerState = rememberPagerState(
        initialPage = state.selectedVariantIndex,
        pageCount = { state.displayedChords.size.coerceAtLeast(1) }
    )

    val listItemWidth = 96.dp
    val density = LocalDensity.current
    val itemWidthPx = with(density) { listItemWidth.toPx() }

    // Sync State -> Pager (HorizontalPager)
    // We use a separate state to track the last seen root to distinguish between 
    // root changes (which should snap the pager) and variant changes (which should animate).
    var lastRoot by remember { mutableStateOf(state.selectedRoot) }
    LaunchedEffect(state.selectedRoot, state.selectedVariantIndex) {
        if (state.displayedChords.isNotEmpty()) {
            if (lastRoot != state.selectedRoot) {
                // Root changed: Snap to the new root's first variant
                pagerState.scrollToPage(state.selectedVariantIndex)
                lastRoot = state.selectedRoot
            } else if (pagerState.currentPage != state.selectedVariantIndex) {
                // Variant changed (via click or list sync): Animate the pager
                pagerState.animateScrollToPage(state.selectedVariantIndex)
            }
        }
    }

    // Sync Pager -> State
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (state.displayedChords.isNotEmpty()) {
                    val clamped = page.coerceIn(0, state.displayedChords.lastIndex)
                    if (clamped != state.selectedVariantIndex) {
                        viewModel.onEvent(ChordEvent.SelectVariant(clamped))
                    }
                }
            }
    }

    // Auto-scroll roots list to center the selected root
    LaunchedEffect(state.selectedRoot) {
        val selectedIndex = state.availableRoots.indexOf(state.selectedRoot)
        if (selectedIndex >= 0) {
            snapshotFlow { rootsListState.layoutInfo.viewportSize.width }
                .filter { it > 0 }
                .first()

            val viewportWidth = rootsListState.layoutInfo.viewportSize.width
            val centerOffset = (viewportWidth - itemWidthPx.toInt()) / 2 - 42
            rootsListState.animateScrollToItem(index = selectedIndex, scrollOffset = -centerOffset)
        }
    }

    // Auto-scroll variants list to center the selected variant
    LaunchedEffect(state.selectedVariantIndex) {
        if (state.displayedChords.isNotEmpty() && state.selectedVariantIndex in state.displayedChords.indices) {
            snapshotFlow { variantsListState.layoutInfo.viewportSize.width }
                .filter { it > 0 }
                .first()

            val viewportWidth = variantsListState.layoutInfo.viewportSize.width
            val centerOffset = (viewportWidth - itemWidthPx.toInt()) / 2 - 42
            variantsListState.animateScrollToItem(index = state.selectedVariantIndex, scrollOffset = -centerOffset)
        }
    }

    Box(Modifier
        .fillMaxSize()
        .layerBackdrop(backdrop)) {
        AppTextureBackground(modifier = Modifier.fillMaxSize())
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 64.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
        ) {
            // 1) Horizontal list of 12 roots
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                state = rootsListState,
                flingBehavior = rememberSnapFlingBehavior(rootsListState)
            ) {
                itemsIndexed(state.availableRoots) { _, root ->
                    val isSelected = root == state.selectedRoot
                    LiquidButton(
                        onClick = { viewModel.onEvent(ChordEvent.SelectRoot(root)) },
                        isInteractive = true,
                        modifier = Modifier
                            .width(listItemWidth)
                            .height(60.dp),
                        surfaceColor = if (isSelected) PlectuneGreen.copy(0.6f) else Color.Transparent,
                        backdrop = backdrop,
                    ) {
                        Text(
                            text = root,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }
            }

            // 2) Horizontal list for variants (qualities) for that root
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                state = variantsListState,
                flingBehavior = rememberSnapFlingBehavior(variantsListState)
            ) {
                itemsIndexed(state.displayedChords) { index, chord ->
                    val isSelected = index == state.selectedVariantIndex
                    val label = chord.suffix.ifBlank { chord.name }

                    LiquidButton(
                        onClick = { viewModel.onEvent(ChordEvent.SelectVariant(index)) },
                        isInteractive = true,
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .width(listItemWidth),
                        surfaceColor = if (isSelected) PlectuneGreen.copy(0.5f) else Color.Transparent,
                        backdrop = backdrop,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            // 3) Pager with diagrams, synced with variant list
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.displayedChords.isEmpty()) {
                    Text(
                        text = "No chords",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        val chord = state.displayedChords.getOrNull(page) ?: return@HorizontalPager
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${chord.root} ${chord.suffix}".trim(),
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            FretboardView(
                                modifier = Modifier.size(220.dp, 280.dp),
                                chord = chord
                            )
                        }
                    }
                }
            }

            // Play button in bottom-left corner
            Row(
                Modifier.fillMaxWidth().padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidButton(
                    onClick = { viewModel.onEvent(ChordEvent.PlaySelected) },
                    isInteractive = true,
                    modifier = Modifier
                        .width(72.dp),
                    surfaceColor = PlectuneGreen.copy(alpha = 0.2f),
                    backdrop = backdrop,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Speaker,
                        contentDescription = "Play chord",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Box(Modifier.width(48.dp)) {
                    Image(
                        painterResource(R.drawable.hand),
                        contentScale = ContentScale.Fit,
                        contentDescription = "Finger indicator",
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChordScreenPreview() {
    ChordScreen()
}
