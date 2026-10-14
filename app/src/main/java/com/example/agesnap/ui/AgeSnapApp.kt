package com.example.agesnap.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.agesnap.ui.about.AboutScreen
import com.example.agesnap.ui.home.HomeScreen
import com.example.agesnap.ui.navigation.AgeSnapDestination
import com.example.agesnap.ui.profiles.ProfilesScreen
import com.example.agesnap.ui.settings.SettingsScreen
import com.example.agesnap.ui.splash.SplashScreen
import com.example.agesnap.ui.theme.AgeSnapTheme
import com.example.agesnap.viewmodel.AgeSnapViewModel

@Composable
fun AgeSnapApp(
    viewModel: AgeSnapViewModel = viewModel(
        factory = AgeSnapViewModel.createFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    AgeSnapTheme(
        themeMode = uiState.settings.themeMode,
        dynamicColor = uiState.settings.dynamicColor
    ) {
        val showBottomBar = currentRoute != AgeSnapDestination.Splash.route

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        AgeSnapDestination.bottomNavDestinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != dest.route) {
                                        navController.navigate(dest.route) {
                                            popUpTo(AgeSnapDestination.Home.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    dest.icon?.let { icon ->
                                        Icon(imageVector = icon, contentDescription = dest.title)
                                    }
                                },
                                label = { Text(dest.title) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = AgeSnapDestination.Splash.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(AgeSnapDestination.Splash.route) {
                    SplashScreen(
                        onSplashComplete = {
                            navController.navigate(AgeSnapDestination.Home.route) {
                                popUpTo(AgeSnapDestination.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(AgeSnapDestination.Home.route) {
                    HomeScreen(uiState = uiState, viewModel = viewModel)
                }

                composable(AgeSnapDestination.Profiles.route) {
                    ProfilesScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onNavigateToHome = {
                            navController.navigate(AgeSnapDestination.Home.route) {
                                popUpTo(AgeSnapDestination.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(AgeSnapDestination.Settings.route) {
                    SettingsScreen(uiState = uiState, viewModel = viewModel)
                }

                composable(AgeSnapDestination.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}
