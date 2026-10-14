package com.example.agesnap

import com.example.agesnap.data.model.AgeProfile
import com.example.agesnap.data.model.CalculationHistory
import com.example.agesnap.data.model.ThemeMode
import com.example.agesnap.data.repository.SharedPrefsCalculationHistoryRepository
import com.example.agesnap.data.repository.SharedPrefsProfileRepository
import com.example.agesnap.data.repository.SharedPrefsSettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ProfileAndHistoryRepositoryTest {

    private lateinit var prefs: FakeSharedPreferences
    private lateinit var profileRepo: SharedPrefsProfileRepository
    private lateinit var historyRepo: SharedPrefsCalculationHistoryRepository
    private lateinit var settingsRepo: SharedPrefsSettingsRepository

    @Before
    fun setup() {
        prefs = FakeSharedPreferences()
        profileRepo = SharedPrefsProfileRepository(prefs)
        historyRepo = SharedPrefsCalculationHistoryRepository(prefs)
        settingsRepo = SharedPrefsSettingsRepository(prefs)
    }

    @Test
    fun testProfileCreationAndPersistence() {
        val profile = AgeProfile(
            id = "child-1",
            name = "Child",
            birthDate = LocalDate.of(2018, 6, 20)
        )
        val saved = profileRepo.saveProfile(profile)
        assertTrue(saved)

        // Verify loaded from fresh instance
        val freshRepo = SharedPrefsProfileRepository(prefs)
        val loaded = freshRepo.getProfiles()
        assertEquals(1, loaded.size)
        assertEquals("Child", loaded[0].name)
        assertEquals(LocalDate.of(2018, 6, 20), loaded[0].birthDate)
    }

    @Test
    fun testProfileBlankNameDefaultsToMyAge() {
        val profile = AgeProfile(
            id = "me-1",
            name = "   ",
            birthDate = LocalDate.of(1995, 3, 15)
        )
        profileRepo.saveProfile(profile)

        val loaded = profileRepo.getProfiles()
        assertEquals("My Age", loaded[0].name)
    }

    @Test
    fun testProfileUpdate() {
        val profile = AgeProfile(
            id = "user-1",
            name = "Old Name",
            birthDate = LocalDate.of(1990, 1, 1)
        )
        profileRepo.saveProfile(profile)

        val updated = profile.copy(name = "New Name")
        val result = profileRepo.updateProfile(updated)
        assertTrue(result)

        val loaded = profileRepo.getProfileById("user-1")
        assertEquals("New Name", loaded?.name)
    }

    @Test
    fun testProfileDeletion() {
        val profile = AgeProfile(
            id = "delete-me",
            name = "Temporary",
            birthDate = LocalDate.of(2000, 1, 1)
        )
        profileRepo.saveProfile(profile)
        assertEquals(1, profileRepo.getProfiles().size)

        val deleted = profileRepo.deleteProfile("delete-me")
        assertTrue(deleted)
        assertEquals(0, profileRepo.getProfiles().size)
    }

    @Test
    fun testProfileLimit_20Max() {
        for (i in 1..20) {
            val p = AgeProfile(
                id = "id-$i",
                name = "Profile $i",
                birthDate = LocalDate.of(2000, 1, i)
            )
            assertTrue(profileRepo.saveProfile(p))
        }

        assertEquals(20, profileRepo.getProfiles().size)

        // 21st profile should fail
        val extra = AgeProfile(
            id = "id-21",
            name = "Profile 21",
            birthDate = LocalDate.of(2000, 1, 21)
        )
        assertFalse(profileRepo.saveProfile(extra))
        assertEquals(20, profileRepo.getProfiles().size)
    }

    @Test
    fun testCalculationHistoryCreationAndLimit() {
        for (i in 1..15) {
            val hist = CalculationHistory(
                id = "calc-$i",
                birthDate = LocalDate.of(1990 + i, 1, 1),
                asOfDate = LocalDate.of(2026, 10, 14),
                ageYears = 36 - i,
                ageMonths = 9,
                ageDays = 13,
                calculatedAt = System.currentTimeMillis() + i
            )
            historyRepo.addCalculation(hist)
        }

        val history = historyRepo.getHistory()
        assertEquals(10, history.size)
        // Most recent should be first
        assertEquals("calc-15", history.first().id)
    }

    @Test
    fun testCalculationHistoryDeduplication() {
        val hist1 = CalculationHistory(
            id = "calc-1",
            birthDate = LocalDate.of(1995, 3, 15),
            asOfDate = LocalDate.of(2026, 10, 14),
            ageYears = 31,
            ageMonths = 6,
            ageDays = 29,
            calculatedAt = 1000L
        )
        val hist2 = CalculationHistory(
            id = "calc-2",
            birthDate = LocalDate.of(1995, 3, 15),
            asOfDate = LocalDate.of(2026, 10, 14),
            ageYears = 31,
            ageMonths = 6,
            ageDays = 29,
            calculatedAt = 1005L
        )

        historyRepo.addCalculation(hist1)
        assertEquals(1, historyRepo.getHistory().size)

        // Immediate recalculation with same birth and as-of date is deduplicated
        historyRepo.addCalculation(hist2)
        assertEquals(1, historyRepo.getHistory().size)
    }

    @Test
    fun testCalculationHistoryClear() {
        val hist = CalculationHistory(
            id = "c1",
            birthDate = LocalDate.of(2000, 1, 1),
            asOfDate = LocalDate.of(2026, 1, 1),
            ageYears = 26,
            ageMonths = 0,
            ageDays = 0,
            calculatedAt = 100L
        )
        historyRepo.addCalculation(hist)
        assertEquals(1, historyRepo.getHistory().size)

        historyRepo.clearHistory()
        assertEquals(0, historyRepo.getHistory().size)
    }

    @Test
    fun testSettingsPersistenceAndReset() {
        settingsRepo.updateThemeMode(ThemeMode.DARK)
        settingsRepo.updateDynamicColor(false)
        settingsRepo.updateHapticFeedback(false)
        settingsRepo.updateAnimationsEnabled(false)
        settingsRepo.updateConfirmProfileDelete(false)

        val freshSettings = SharedPrefsSettingsRepository(prefs).getSettings()
        assertEquals(ThemeMode.DARK, freshSettings.themeMode)
        assertFalse(freshSettings.dynamicColor)
        assertFalse(freshSettings.hapticFeedback)
        assertFalse(freshSettings.animationsEnabled)
        assertFalse(freshSettings.confirmProfileDelete)

        settingsRepo.resetSettings()
        val resetSettings = SharedPrefsSettingsRepository(prefs).getSettings()
        assertEquals(ThemeMode.SYSTEM, resetSettings.themeMode)
        assertTrue(resetSettings.dynamicColor)
        assertTrue(resetSettings.hapticFeedback)
        assertTrue(resetSettings.animationsEnabled)
        assertTrue(resetSettings.confirmProfileDelete)
    }

    @Test
    fun testUsageStatistics() {
        settingsRepo.incrementCalculationCount()
        settingsRepo.incrementCalculationCount()
        settingsRepo.recordCalculationTimestamp(12345678L)

        val stats = settingsRepo.getUsageStatistics(savedProfilesCount = 3)
        assertEquals(2L, stats.calculationCount)
        assertEquals(3, stats.savedProfilesCount)
        assertEquals(12345678L, stats.lastCalculationTimestamp)
    }
}
