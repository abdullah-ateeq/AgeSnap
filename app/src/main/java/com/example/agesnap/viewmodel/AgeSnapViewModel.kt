package com.example.agesnap.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.agesnap.data.model.AgeProfile
import com.example.agesnap.data.model.CalculationHistory
import com.example.agesnap.data.model.CalculationState
import com.example.agesnap.data.model.ThemeMode
import com.example.agesnap.data.repository.CalculationHistoryRepository
import com.example.agesnap.data.repository.ProfileRepository
import com.example.agesnap.data.repository.SettingsRepository
import com.example.agesnap.data.repository.SharedPrefsCalculationHistoryRepository
import com.example.agesnap.data.repository.SharedPrefsProfileRepository
import com.example.agesnap.data.repository.SharedPrefsSettingsRepository
import com.example.agesnap.domain.AgeCalculator
import com.example.agesnap.domain.AgeValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

/**
 * ViewModel orchestrating date state, calculations, profile persistence, history, and settings.
 */
class AgeSnapViewModel(
    private val profileRepository: ProfileRepository,
    private val historyRepository: CalculationHistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AgeSnapUiState(
            birthDate = null,
            asOfDate = LocalDate.now(),
            settings = settingsRepository.getSettings(),
            profiles = profileRepository.getProfiles(),
            history = historyRepository.getHistory(),
            statistics = settingsRepository.getUsageStatistics(profileRepository.getProfiles().size)
        )
    )
    val uiState: StateFlow<AgeSnapUiState> = _uiState.asStateFlow()

    private var lastCalculationTimeMs: Long = 0L

    init {
        refreshData()
    }

    fun refreshData() {
        val profiles = profileRepository.getProfiles()
        val history = historyRepository.getHistory()
        val settings = settingsRepository.getSettings()
        val stats = settingsRepository.getUsageStatistics(profiles.size)
        _uiState.update { current ->
            current.copy(
                profiles = profiles,
                history = history,
                settings = settings,
                statistics = stats
            )
        }
    }

    /**
     * Updates selected birth date.
     * Clears any previous or stale calculation results.
     */
    fun setBirthDate(date: LocalDate) {
        _uiState.update { current ->
            current.copy(
                birthDate = date,
                ageResult = null,
                calculationState = CalculationState.Idle,
                errorMessage = null
            )
        }
    }

    /**
     * Updates selected as-of calculation date.
     * Clears any stale calculation results.
     */
    fun setAsOfDate(date: LocalDate) {
        _uiState.update { current ->
            current.copy(
                asOfDate = date,
                ageResult = null,
                calculationState = CalculationState.Idle,
                errorMessage = null
            )
        }
    }

    /**
     * Resets the as-of date to the current date (today).
     */
    fun resetAsOfDateToToday() {
        setAsOfDate(LocalDate.now())
    }

    /**
     * Performs exact age calculation with input validation and history recording.
     * Protected against rapid double-taps.
     */
    fun calculateAge() {
        val now = System.currentTimeMillis()
        if (now - lastCalculationTimeMs < 300L) {
            // Rapid double-tap debounce
            return
        }
        lastCalculationTimeMs = now

        val state = _uiState.value
        val birthDate = state.birthDate
        val asOfDate = state.asOfDate

        // Validate date constraints
        val error = AgeValidator.validate(birthDate, asOfDate)
        if (error != null) {
            _uiState.update { current ->
                current.copy(
                    ageResult = null,
                    calculationState = CalculationState.Error(error),
                    errorMessage = error.message
                )
            }
            return
        }

        try {
            val result = AgeCalculator.calculateAge(birthDate!!, asOfDate)

            // Record into history
            val historyEntry = CalculationHistory(
                id = UUID.randomUUID().toString(),
                birthDate = birthDate,
                asOfDate = asOfDate,
                ageYears = result.years,
                ageMonths = result.months,
                ageDays = result.days,
                calculatedAt = now
            )
            historyRepository.addCalculation(historyEntry)

            // Update stats
            settingsRepository.incrementCalculationCount()
            settingsRepository.recordCalculationTimestamp(now)

            val updatedHistory = historyRepository.getHistory()
            val updatedProfiles = profileRepository.getProfiles()
            val updatedStats = settingsRepository.getUsageStatistics(updatedProfiles.size)

            _uiState.update { current ->
                current.copy(
                    ageResult = result,
                    calculationState = CalculationState.Calculated(result),
                    errorMessage = null,
                    history = updatedHistory,
                    statistics = updatedStats
                )
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "An unexpected error occurred during calculation."
            _uiState.update { current ->
                current.copy(
                    ageResult = null,
                    calculationState = CalculationState.Error(com.example.agesnap.data.model.CalculationError(errorMsg)),
                    errorMessage = errorMsg
                )
            }
        }
    }

    /**
     * Saves a profile with optional name (defaults to "My Age") and birth date.
     * Capped at 20 profiles max.
     */
    fun saveProfile(name: String, birthDate: LocalDate): Boolean {
        val safeName = name.trim().ifEmpty { "My Age" }
        val newProfile = AgeProfile(
            id = UUID.randomUUID().toString(),
            name = safeName,
            birthDate = birthDate
        )
        val success = profileRepository.saveProfile(newProfile)
        if (success) {
            val updatedProfiles = profileRepository.getProfiles()
            val stats = settingsRepository.getUsageStatistics(updatedProfiles.size)
            _uiState.update { current ->
                current.copy(
                    profiles = updatedProfiles,
                    selectedProfileId = newProfile.id,
                    profileName = newProfile.name,
                    statistics = stats
                )
            }
        }
        return success
    }

    /**
     * Updates an existing profile.
     */
    fun updateProfile(id: String, name: String, birthDate: LocalDate): Boolean {
        val safeName = name.trim().ifEmpty { "My Age" }
        val updated = AgeProfile(id = id, name = safeName, birthDate = birthDate)
        val success = profileRepository.updateProfile(updated)
        if (success) {
            val updatedProfiles = profileRepository.getProfiles()
            _uiState.update { current ->
                current.copy(
                    profiles = updatedProfiles,
                    profileName = if (current.selectedProfileId == id) safeName else current.profileName,
                    birthDate = if (current.selectedProfileId == id) birthDate else current.birthDate
                )
            }
        }
        return success
    }

    /**
     * Deletes a profile by ID.
     */
    fun deleteProfile(id: String): Boolean {
        val success = profileRepository.deleteProfile(id)
        if (success) {
            val updatedProfiles = profileRepository.getProfiles()
            val stats = settingsRepository.getUsageStatistics(updatedProfiles.size)
            _uiState.update { current ->
                current.copy(
                    profiles = updatedProfiles,
                    selectedProfileId = if (current.selectedProfileId == id) null else current.selectedProfileId,
                    profileName = if (current.selectedProfileId == id) "" else current.profileName,
                    statistics = stats
                )
            }
        }
        return success
    }

    /**
     * Selects a profile, populates birth date on Home, and recalculates age as of current as-of date.
     */
    fun selectProfile(profile: AgeProfile) {
        _uiState.update { current ->
            current.copy(
                birthDate = profile.birthDate,
                selectedProfileId = profile.id,
                profileName = profile.name,
                ageResult = null,
                calculationState = CalculationState.Idle,
                errorMessage = null
            )
        }
        calculateAge()
    }

    /**
     * Loads a calculation from recent history.
     */
    fun loadFromHistory(history: CalculationHistory) {
        _uiState.update { current ->
            current.copy(
                birthDate = history.birthDate,
                asOfDate = history.asOfDate,
                selectedProfileId = null,
                profileName = "",
                ageResult = null,
                calculationState = CalculationState.Idle,
                errorMessage = null
            )
        }
        calculateAge()
    }

    /**
     * Clears all calculation history entries.
     */
    fun clearHistory() {
        historyRepository.clearHistory()
        _uiState.update { current ->
            current.copy(history = emptyList())
        }
    }

    /**
     * Resets temporary Home screen calculation state to initial empty state.
     * Does NOT remove saved profiles or history.
     */
    fun resetHome() {
        _uiState.update { current ->
            current.copy(
                birthDate = null,
                asOfDate = LocalDate.now(),
                ageResult = null,
                calculationState = CalculationState.Idle,
                selectedProfileId = null,
                profileName = "",
                errorMessage = null
            )
        }
    }

    // Settings modifiers
    fun updateThemeMode(mode: ThemeMode) {
        settingsRepository.updateThemeMode(mode)
        _uiState.update { it.copy(settings = it.settings.copy(themeMode = mode)) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        settingsRepository.updateDynamicColor(enabled)
        _uiState.update { it.copy(settings = it.settings.copy(dynamicColor = enabled)) }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        settingsRepository.updateHapticFeedback(enabled)
        _uiState.update { it.copy(settings = it.settings.copy(hapticFeedback = enabled)) }
    }

    fun updateAnimationsEnabled(enabled: Boolean) {
        settingsRepository.updateAnimationsEnabled(enabled)
        _uiState.update { it.copy(settings = it.settings.copy(animationsEnabled = enabled)) }
    }

    fun updateConfirmProfileDelete(enabled: Boolean) {
        settingsRepository.updateConfirmProfileDelete(enabled)
        _uiState.update { it.copy(settings = it.settings.copy(confirmProfileDelete = enabled)) }
    }

    fun resetSettings() {
        settingsRepository.resetSettings()
        _uiState.update { it.copy(settings = settingsRepository.getSettings()) }
    }

    companion object {
        fun createFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appContext = context.applicationContext
                val prefs = appContext.getSharedPreferences("agesnap_preferences", Context.MODE_PRIVATE)
                return AgeSnapViewModel(
                    profileRepository = SharedPrefsProfileRepository(prefs),
                    historyRepository = SharedPrefsCalculationHistoryRepository(prefs),
                    settingsRepository = SharedPrefsSettingsRepository(prefs)
                ) as T
            }
        }
    }
}
