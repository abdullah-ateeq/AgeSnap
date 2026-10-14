package com.example.agesnap.viewmodel

import com.example.agesnap.data.model.AgeProfile
import com.example.agesnap.data.model.AgeResult
import com.example.agesnap.data.model.CalculationHistory
import com.example.agesnap.data.model.CalculationState
import com.example.agesnap.data.model.UsageStatistics
import com.example.agesnap.data.model.UserSettings
import java.time.LocalDate

/**
 * Immutable UI State representing the complete presentation state of AgeSnap.
 */
data class AgeSnapUiState(
    val birthDate: LocalDate? = null,
    val asOfDate: LocalDate = LocalDate.now(),
    val ageResult: AgeResult? = null,
    val calculationState: CalculationState = CalculationState.Idle,
    val profiles: List<AgeProfile> = emptyList(),
    val history: List<CalculationHistory> = emptyList(),
    val selectedProfileId: String? = null,
    val profileName: String = "",
    val errorMessage: String? = null,
    val settings: UserSettings = UserSettings(),
    val statistics: UsageStatistics = UsageStatistics()
)
