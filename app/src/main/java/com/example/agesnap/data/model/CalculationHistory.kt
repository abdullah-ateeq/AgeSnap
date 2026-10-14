package com.example.agesnap.data.model

import java.time.LocalDate

/**
 * Represents a saved calculation history entry.
 */
data class CalculationHistory(
    val id: String,
    val birthDate: LocalDate,
    val asOfDate: LocalDate,
    val ageYears: Int,
    val ageMonths: Int,
    val ageDays: Int,
    val calculatedAt: Long
)
