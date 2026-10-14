package com.example.agesnap.data.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animationsEnabled: Boolean = true,
    val confirmProfileDelete: Boolean = true
)

data class UsageStatistics(
    val calculationCount: Long = 0L,
    val savedProfilesCount: Int = 0,
    val lastCalculationTimestamp: Long? = null
)
