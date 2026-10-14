package com.example.agesnap

import com.example.agesnap.domain.AgeValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class AgeValidatorTest {

    private val today = LocalDate.of(2026, 10, 14)

    @Test
    fun testNullBirthDate() {
        val error = AgeValidator.validate(
            birthDate = null,
            asOfDate = today,
            today = today
        )
        assertNotNull(error)
        assertEquals("Please select a birth date.", error?.message)
    }

    @Test
    fun testFutureBirthDate_relativeToToday() {
        val futureBirth = LocalDate.of(2030, 1, 1)
        val error = AgeValidator.validate(
            birthDate = futureBirth,
            asOfDate = today,
            today = today
        )
        assertNotNull(error)
        assertEquals("Birth date cannot be in the future.", error?.message)
    }

    @Test
    fun testBirthDateAfterAsOfDate() {
        val birth = LocalDate.of(2025, 1, 1)
        val asOf = LocalDate.of(2024, 1, 1)
        val error = AgeValidator.validate(
            birthDate = birth,
            asOfDate = asOf,
            today = today
        )
        assertNotNull(error)
        assertEquals("Birth date cannot be after the calculation date.", error?.message)
    }

    @Test
    fun testValidDates() {
        val birth = LocalDate.of(1995, 3, 15)
        val asOf = LocalDate.of(2026, 10, 14)
        val error = AgeValidator.validate(
            birthDate = birth,
            asOfDate = asOf,
            today = today
        )
        assertNull(error)
    }
}
