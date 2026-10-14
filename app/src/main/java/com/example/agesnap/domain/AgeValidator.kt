package com.example.agesnap.domain

import com.example.agesnap.data.model.CalculationError
import java.time.LocalDate

/**
 * Validates birth date and as-of calculation dates.
 */
object AgeValidator {

    /**
     * Validates date inputs for age calculation.
     *
     * @param birthDate The person's date of birth.
     * @param asOfDate The calculation reference date.
     * @param today Reference current date (defaults to LocalDate.now()).
     * @return CalculationError with an appropriate message, or null if dates are valid.
     */
    fun validate(
        birthDate: LocalDate?,
        asOfDate: LocalDate,
        today: LocalDate = LocalDate.now()
    ): CalculationError? {
        if (birthDate == null) {
            return CalculationError("Please select a birth date.")
        }
        if (birthDate.isAfter(today)) {
            return CalculationError("Birth date cannot be in the future.")
        }
        if (birthDate.isAfter(asOfDate)) {
            return CalculationError("Birth date cannot be after the calculation date.")
        }
        return null
    }
}
