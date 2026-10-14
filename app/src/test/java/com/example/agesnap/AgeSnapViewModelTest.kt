package com.example.agesnap

import com.example.agesnap.data.model.AgeProfile
import com.example.agesnap.data.model.CalculationHistory
import com.example.agesnap.data.model.CalculationState
import com.example.agesnap.data.model.ThemeMode
import com.example.agesnap.data.repository.SharedPrefsCalculationHistoryRepository
import com.example.agesnap.data.repository.SharedPrefsProfileRepository
import com.example.agesnap.data.repository.SharedPrefsSettingsRepository
import com.example.agesnap.viewmodel.AgeSnapViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AgeSnapViewModelTest {

    private lateinit var prefs: FakeSharedPreferences
    private lateinit var viewModel: AgeSnapViewModel

    @Before
    fun setup() {
        prefs = FakeSharedPreferences()
        viewModel = AgeSnapViewModel(
            profileRepository = SharedPrefsProfileRepository(prefs),
            historyRepository = SharedPrefsCalculationHistoryRepository(prefs),
            settingsRepository = SharedPrefsSettingsRepository(prefs)
        )
    }

    @Test
    fun testInitialState() {
        val state = viewModel.uiState.value
        assertNull(state.birthDate)
        assertEquals(LocalDate.now(), state.asOfDate)
        assertNull(state.ageResult)
        assertTrue(state.calculationState is CalculationState.Idle)
        assertTrue(state.profiles.isEmpty())
        assertTrue(state.history.isEmpty())
        assertNull(state.errorMessage)
    }

    @Test
    fun testDateSelection_and_ClearingStaleResult() {
        val birth = LocalDate.of(1995, 3, 15)
        viewModel.setBirthDate(birth)
        assertEquals(birth, viewModel.uiState.value.birthDate)

        // Perform calculation
        viewModel.calculateAge()
        assertNotNull(viewModel.uiState.value.ageResult)
        assertTrue(viewModel.uiState.value.calculationState is CalculationState.Calculated)

        // Changing birth date MUST clear stale result
        val newBirth = LocalDate.of(1998, 5, 20)
        viewModel.setBirthDate(newBirth)
        assertEquals(newBirth, viewModel.uiState.value.birthDate)
        assertNull(viewModel.uiState.value.ageResult)
        assertTrue(viewModel.uiState.value.calculationState is CalculationState.Idle)
    }

    @Test
    fun testCalculation_nullBirthDate_showsError() {
        viewModel.calculateAge()
        val state = viewModel.uiState.value
        assertNull(state.ageResult)
        assertTrue(state.calculationState is CalculationState.Error)
        assertEquals("Please select a birth date.", state.errorMessage)
    }

    @Test
    fun testCalculation_futureBirthDate_showsError() {
        viewModel.setBirthDate(LocalDate.now().plusDays(10))
        viewModel.calculateAge()
        val state = viewModel.uiState.value
        assertNull(state.ageResult)
        assertTrue(state.calculationState is CalculationState.Error)
        assertEquals("Birth date cannot be in the future.", state.errorMessage)
    }

    @Test
    fun testCalculation_birthAfterAsOf_showsError() {
        val birth = LocalDate.of(2025, 1, 1)
        val asOf = LocalDate.of(2024, 1, 1)
        viewModel.setBirthDate(birth)
        viewModel.setAsOfDate(asOf)
        viewModel.calculateAge()

        val state = viewModel.uiState.value
        assertNull(state.ageResult)
        assertTrue(state.calculationState is CalculationState.Error)
        assertEquals("Birth date cannot be after the calculation date.", state.errorMessage)
    }

    @Test
    fun testCalculation_valid_successAndHistoryRecorded() {
        val birth = LocalDate.of(1995, 3, 15)
        val asOf = LocalDate.of(2026, 10, 14)
        viewModel.setBirthDate(birth)
        viewModel.setAsOfDate(asOf)
        viewModel.calculateAge()

        val state = viewModel.uiState.value
        assertNotNull(state.ageResult)
        assertTrue(state.calculationState is CalculationState.Calculated)
        assertEquals(31, state.ageResult?.years)
        assertEquals(6, state.ageResult?.months)
        assertEquals(29, state.ageResult?.days)

        // History recorded
        assertEquals(1, state.history.size)
        assertEquals(birth, state.history[0].birthDate)
        assertEquals(asOf, state.history[0].asOfDate)

        // Statistics incremented
        assertEquals(1L, state.statistics.calculationCount)
    }

    @Test
    fun testResetHome_clearsCalculation_preservesProfiles() {
        viewModel.saveProfile("Child", LocalDate.of(2018, 6, 20))
        viewModel.setBirthDate(LocalDate.of(1995, 3, 15))
        viewModel.calculateAge()

        assertNotNull(viewModel.uiState.value.ageResult)
        assertEquals(1, viewModel.uiState.value.profiles.size)

        viewModel.resetHome()

        val state = viewModel.uiState.value
        assertNull(state.birthDate)
        assertEquals(LocalDate.now(), state.asOfDate)
        assertNull(state.ageResult)
        assertTrue(state.calculationState is CalculationState.Idle)
        // Saved profiles remain untouched
        assertEquals(1, state.profiles.size)
    }

    @Test
    fun testProfileManagement() {
        val birth = LocalDate.of(2018, 6, 20)
        assertTrue(viewModel.saveProfile("Child", birth))
        assertEquals(1, viewModel.uiState.value.profiles.size)

        val profile = viewModel.uiState.value.profiles.first()
        assertEquals("Child", profile.name)

        // Update profile
        assertTrue(viewModel.updateProfile(profile.id, "First Child", birth))
        assertEquals("First Child", viewModel.uiState.value.profiles.first().name)

        // Select profile
        viewModel.selectProfile(viewModel.uiState.value.profiles.first())
        assertEquals(birth, viewModel.uiState.value.birthDate)
        assertNotNull(viewModel.uiState.value.ageResult)

        // Delete profile
        assertTrue(viewModel.deleteProfile(profile.id))
        assertTrue(viewModel.uiState.value.profiles.isEmpty())
    }

    @Test
    fun testLoadFromHistory() {
        val entry = CalculationHistory(
            id = "test-h",
            birthDate = LocalDate.of(2000, 1, 1),
            asOfDate = LocalDate.of(2025, 1, 1),
            ageYears = 25,
            ageMonths = 0,
            ageDays = 0,
            calculatedAt = 100L
        )

        viewModel.loadFromHistory(entry)
        val state = viewModel.uiState.value
        assertEquals(LocalDate.of(2000, 1, 1), state.birthDate)
        assertEquals(LocalDate.of(2025, 1, 1), state.asOfDate)
        assertEquals(25, state.ageResult?.years)
    }

    @Test
    fun testClearHistory() {
        viewModel.setBirthDate(LocalDate.of(1995, 3, 15))
        viewModel.calculateAge()
        assertEquals(1, viewModel.uiState.value.history.size)

        viewModel.clearHistory()
        assertTrue(viewModel.uiState.value.history.isEmpty())
    }

    @Test
    fun testSettingsUpdates() {
        viewModel.updateThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.settings.themeMode)

        viewModel.updateDynamicColor(false)
        assertFalse(viewModel.uiState.value.settings.dynamicColor)

        viewModel.updateHapticFeedback(false)
        assertFalse(viewModel.uiState.value.settings.hapticFeedback)

        viewModel.updateAnimationsEnabled(false)
        assertFalse(viewModel.uiState.value.settings.animationsEnabled)

        viewModel.updateConfirmProfileDelete(false)
        assertFalse(viewModel.uiState.value.settings.confirmProfileDelete)

        viewModel.resetSettings()
        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.settings.themeMode)
        assertTrue(viewModel.uiState.value.settings.dynamicColor)
        assertTrue(viewModel.uiState.value.settings.hapticFeedback)
    }
}
