package com.trishit.plectune.ui.navigation

import android.content.res.Configuration
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.trishit.plectune.R
import com.trishit.plectune.feature.chords.presentation.ChordScreen
import com.trishit.plectune.feature.metronome.presentation.MetronomeScreen
import com.trishit.plectune.feature.tuner.presentation.TunerScreen
import com.trishit.plectune.ui.components.LiquidBottomTab
import com.trishit.plectune.ui.components.LiquidBottomTabs
import com.trishit.plectune.ui.components.ThemeSwitcher
import com.trishit.plectune.ui.theme.AnimatedPlectuneTheme

sealed class Screen(val route: String, val title: String, val icon: Int) {
    object Tuner : Screen("tuner", "Tuner", R.drawable.tuner)
    object Metronome : Screen("metronome", "Metronome", R.drawable.metro)
    object Chords : Screen("chords", "Chords", R.drawable.chord_og)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {
    val systemDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemDark) }

    AnimatedPlectuneTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()
        val screens = listOf(Screen.Metronome, Screen.Tuner, Screen.Chords)

        var selectedIndex by remember { mutableIntStateOf(1) }
        var previousIndex by remember { mutableIntStateOf(1) }
        var navDirection by remember { mutableIntStateOf(0) } // -1: left, 1: right, 0: none

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val backgroundColor = MaterialTheme.colorScheme.background
        val backdrop = rememberLayerBackdrop {
            drawRect(backgroundColor)
            drawContent()
        }

        LaunchedEffect(currentRoute) {
            val index = screens.indexOfFirst { it.route == currentRoute }
            if (index != -1) {
                navDirection = when {
                    index > previousIndex -> 1 // right
                    index < previousIndex -> -1 // left
                    else -> 0
                }
                previousIndex = selectedIndex
                selectedIndex = index
            }
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                LiquidBottomTabs(
                    selectedTabIndex = { selectedIndex },
                    onTabSelected = { index ->
                        navDirection = when {
                            index > selectedIndex -> 1 // right
                            index < selectedIndex -> -1 // left
                            else -> 0
                        }
                        previousIndex = selectedIndex
                        selectedIndex = index
                        navController.navigate(screens[index].route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    backdrop = backdrop,
                    tabsCount = screens.size,
                    modifier = Modifier.padding(horizontal = 56.dp, vertical = 24.dp)
                ) {
                    screens.forEachIndexed { index, screen ->
                        LiquidBottomTab(
                            onClick = { selectedIndex = index },
                        ){
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    painter = painterResource(screen.icon),
                                    contentDescription = screen.title,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Tuner.route,
                    modifier = Modifier
                        .layerBackdrop(backdrop)
                        .padding(PaddingValues(top = innerPadding.calculateTopPadding())),
                    enterTransition = {
                        if (navDirection >= 0) {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            )
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(300)
                            )
                        }
                    },
                    exitTransition = {
                        if (navDirection >= 0) {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            )
                        } else {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            )
                        }
                    },
                    popEnterTransition = {
                        if (navDirection < 0) {
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(300)
                            )
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            )
                        }
                    },
                    popExitTransition = {
                        if (navDirection < 0) {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            )
                        } else {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            )
                        }
                    }
                ) {
                    composable(Screen.Tuner.route) { TunerScreen() }
                    composable(Screen.Metronome.route) { MetronomeScreen() }
                    composable(Screen.Chords.route) { ChordScreen() }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        // Apply top padding first to clear status bar/scaffold area
                        .padding(top = innerPadding.calculateTopPadding())
                        // Then add some margin from the edges
                        .padding(start = 16.dp, top = 8.dp)
                        // Set a fixed size for the container
                        .size(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.guitar_pick_logo_transparent),
                        contentDescription = "Logo",
                        // Use Fit to ensure the whole logo is visible without cropping
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Theme Switcher in the top right corner
                ThemeSwitcher(
                    isDark = isDarkTheme,
                    onToggle = { isDarkTheme = !isDarkTheme },
                    backdrop = backdrop,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .padding(top = innerPadding.calculateTopPadding())
                )
            }
        }
    }
}



@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppNavigationPreview() {
    AnimatedPlectuneTheme {
        AppNavigation()
    }
}
