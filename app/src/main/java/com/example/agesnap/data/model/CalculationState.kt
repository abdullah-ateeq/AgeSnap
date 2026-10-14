package com.example.agesnap.data.model

/**
 * Encapsulates calculation validation error messages.
 */
data class CalculationError(
    val message: String
)

/**
 * Sealed interface representing the current state of the age calculation.
 */
sealed interface CalculationState {
    data object Idle : CalculationState
    data class Calculated(val result: AgeResult) : CalculationState
    data class Error(val error: CalculationError) : CalculationState
}
