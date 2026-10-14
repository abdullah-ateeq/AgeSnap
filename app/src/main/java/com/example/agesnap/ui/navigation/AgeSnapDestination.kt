package com.example.agesnap.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AgeSnapDestination(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    data object Splash : AgeSnapDestination("splash", "Splash")
    data object Home : AgeSnapDestination("home", "Home", Icons.Outlined.Calculate)
    data object Profiles : AgeSnapDestination("profiles", "Profiles", Icons.Outlined.Person)
    data object Settings : AgeSnapDestination("settings", "Settings", Icons.Outlined.Settings)
    data object About : AgeSnapDestination("about", "About", Icons.Outlined.Info)

    companion object {
        val bottomNavDestinations = listOf(Home, Profiles, Settings, About)
    }
}
