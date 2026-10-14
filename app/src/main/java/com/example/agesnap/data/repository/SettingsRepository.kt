package com.example.agesnap.data.repository

import android.content.SharedPreferences
import com.example.agesnap.data.model.ThemeMode
import com.example.agesnap.data.model.UsageStatistics
import com.example.agesnap.data.model.UserSettings

interface SettingsRepository {
    fun getSettings(): UserSettings
    fun updateThemeMode(mode: ThemeMode)
    fun updateDynamicColor(enabled: Boolean)
    fun updateHapticFeedback(enabled: Boolean)
    fun updateAnimationsEnabled(enabled: Boolean)
    fun updateConfirmProfileDelete(enabled: Boolean)
    fun resetSettings()

    fun getUsageStatistics(savedProfilesCount: Int): UsageStatistics
    fun incrementCalculationCount()
    fun recordCalculationTimestamp(timestamp: Long)
}

class SharedPrefsSettingsRepository(
    private val preferences: SharedPreferences
) : SettingsRepository {

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_DYNAMIC_COLOR = "pref_dynamic_color"
        private const val KEY_HAPTIC_FEEDBACK = "pref_haptic_feedback"
        private const val KEY_ANIMATIONS = "pref_animations_enabled"
        private const val KEY_CONFIRM_DELETE = "pref_confirm_profile_delete"
        private const val KEY_CALCULATION_COUNT = "stat_calculation_count"
        private const val KEY_LAST_CALC_TIMESTAMP = "stat_last_calc_timestamp"
    }

    override fun getSettings(): UserSettings {
        val themeModeStr = preferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
        val dynamicColor = preferences.getBoolean(KEY_DYNAMIC_COLOR, true)
        val haptics = preferences.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        val animations = preferences.getBoolean(KEY_ANIMATIONS, true)
        val confirmDelete = preferences.getBoolean(KEY_CONFIRM_DELETE, true)

        return UserSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            hapticFeedback = haptics,
            animationsEnabled = animations,
            confirmProfileDelete = confirmDelete
        )
    }

    override fun updateThemeMode(mode: ThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    override fun updateDynamicColor(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
    }

    override fun updateHapticFeedback(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    override fun updateAnimationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
    }

    override fun updateConfirmProfileDelete(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_CONFIRM_DELETE, enabled).apply()
    }

    override fun resetSettings() {
        preferences.edit()
            .remove(KEY_THEME_MODE)
            .remove(KEY_DYNAMIC_COLOR)
            .remove(KEY_HAPTIC_FEEDBACK)
            .remove(KEY_ANIMATIONS)
            .remove(KEY_CONFIRM_DELETE)
            .apply()
    }

    override fun getUsageStatistics(savedProfilesCount: Int): UsageStatistics {
        val count = preferences.getLong(KEY_CALCULATION_COUNT, 0L)
        val lastTimestamp = if (preferences.contains(KEY_LAST_CALC_TIMESTAMP)) {
            preferences.getLong(KEY_LAST_CALC_TIMESTAMP, 0L)
        } else {
            null
        }
        return UsageStatistics(
            calculationCount = count,
            savedProfilesCount = savedProfilesCount,
            lastCalculationTimestamp = lastTimestamp
        )
    }

    override fun incrementCalculationCount() {
        val current = preferences.getLong(KEY_CALCULATION_COUNT, 0L)
        preferences.edit().putLong(KEY_CALCULATION_COUNT, current + 1L).apply()
    }

    override fun recordCalculationTimestamp(timestamp: Long) {
        preferences.edit().putLong(KEY_LAST_CALC_TIMESTAMP, timestamp).apply()
    }
}
