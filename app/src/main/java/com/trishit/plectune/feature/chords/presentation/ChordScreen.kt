package com.trishit.plectune.feature.chords.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trishit.plectune.ui.components.FretboardView
import com.trishit.plectune.ui.theme.DarkGrey850
import com.trishit.plectune.ui.theme.DarkGrey900
import com.trishit.plectune.ui.theme.PlectuneGreen
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordScreen(
    viewModel: ChordViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = state.selectedVariantIndex,
        pageCount = { state.displayedChords.size.coerceAtLeast(1) }
    )

    // When user taps a variant chip, drive the pager.
    LaunchedEffect(state.selectedVariantIndex, state.displayedChords.size) {
        val target = state.selectedVariantIndex.coerceIn(
            0,
            (state.displayedChords.size - 1).coerceAtLeast(0)
        )
        if (state.displayedChords.isNotEmpty() && pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }

    // When user swipes pager, update selected variant chip.
    LaunchedEffect(pagerState, state.displayedChords.size) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp) // extra top padding under the app bar (as requested)
        ) {
            // 1) Horizontal list of 12 roots
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.availableRoots) { _, root ->
                    val isSelected = root == state.selectedRoot
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PlectuneGreen else DarkGrey850)
                            .clickable { viewModel.onEvent(ChordEvent.SelectRoot(root)) }
                    ) {
                        Text(
                            text = root,
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(state.displayedChords) { index, chord ->
                    val isSelected = index == state.selectedVariantIndex
                    val label = chord.suffix.ifBlank { chord.name }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelected) PlectuneGreen else DarkGrey900)
                            .clickable { viewModel.onEvent(ChordEvent.SelectVariant(index)) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 3) Pager with diagrams, synced with variant list
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGrey900),
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
                        val chord = state.displayedChords[page]
                        Column(
                            modifier = Modifier.fillMaxSize().padding(18.dp),
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

            // spacer at bottom so FAB doesn't overlap pager content too much
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) { /* intentionally empty */ }
        }

        // Play button in bottom-left corner (moved from chord click)
        FloatingActionButton(
            onClick = { viewModel.onEvent(ChordEvent.PlaySelected) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            containerColor = PlectuneGreen,
            contentColor = Color.Black
        ) {
            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play chord")
        }
    }
}
