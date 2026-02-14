package com.trishit.plectune.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.trishit.plectune.ui.theme.PlectuneTheme

sealed class Screen(val route: String, val title: String, val icon: Int) {
    object Tuner : Screen("tuner", "Tuner", R.drawable.tuner)
    object Metronome : Screen("metronome", "Metronome", R.drawable.metro)
    object Chords : Screen("chords", "Chords", R.drawable.chord_og)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Metronome, Screen.Tuner, Screen.Chords)

    // 1. Track index for the Liquid Tabs
    var selectedIndex by remember { mutableIntStateOf(1) }

    // 2. Sync NavController with Tabs
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Get the color before passing to rememberLayerBackdrop (non-composable context)
    val backgroundColor = MaterialTheme.colorScheme.background
    val backdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }

    LaunchedEffect(currentRoute) {
        val index = screens.indexOfFirst { it.route == currentRoute }
        if (index != -1) {
            selectedIndex = index
        }
    }

    Scaffold( // Important: Let content draw behind the glass bar
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // 3. The Liquid Bar
            LiquidBottomTabs(
                selectedTabIndex = { selectedIndex },
                onTabSelected = { index ->
                    selectedIndex = index
                    navController.navigate(screens[index].route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                backdrop = backdrop, // The "Glass" Base
                tabsCount = screens.size,
                modifier = Modifier.padding(horizontal = 56.dp, vertical = 24.dp) // Floating look
            ) {
                // 4. Draw the Tabs
                screens.forEachIndexed { index, screen ->
                    LiquidBottomTab(
                        onClick = { selectedIndex = index },
                    ){
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f) // Distribute evenly
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
        NavHost(
            navController = navController,
            startDestination = Screen.Tuner.route,
            modifier = Modifier
                .layerBackdrop(backdrop)
                .padding(PaddingValues(top = innerPadding.calculateTopPadding()))
        ) {
            composable(Screen.Tuner.route) { TunerScreen() }
            composable(Screen.Metronome.route) { MetronomeScreen() }
            composable(Screen.Chords.route) { ChordScreen() }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppNavigationPreview() {
    PlectuneTheme {
        AppNavigation()
    }
}
