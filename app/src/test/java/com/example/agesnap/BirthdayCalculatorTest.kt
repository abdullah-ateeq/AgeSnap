package com.example.agesnap

import com.example.agesnap.domain.BirthdayCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BirthdayCalculatorTest {

    @Test
    fun testBirthdayToday() {
        val birth = LocalDate.of(1995, 10, 14)
        val asOf = LocalDate.of(2026, 10, 14)

        assertTrue(BirthdayCalculator.isBirthdayToday(birth, asOf))
        assertTrue(BirthdayCalculator.hasBirthdayReachedThisYear(birth, asOf))
        assertEquals(0L, BirthdayCalculator.calculateDaysUntilBirthday(birth, asOf))
        assertEquals(LocalDate.of(2026, 10, 14), BirthdayCalculator.calculateNextBirthday(birth, asOf))
    }

    @Test
    fun testBirthdayUpcomingThisYear() {
        val birth = LocalDate.of(1995, 12, 25)
        val asOf = LocalDate.of(2026, 10, 14)

        assertFalse(BirthdayCalculator.isBirthdayToday(birth, asOf))
        assertFalse(BirthdayCalculator.hasBirthdayReachedThisYear(birth, asOf))
        assertEquals(LocalDate.of(2026, 12, 25), BirthdayCalculator.calculateNextBirthday(birth, asOf))
        assertEquals(72L, BirthdayCalculator.calculateDaysUntilBirthday(birth, asOf))
    }

    @Test
    fun testBirthdayAlreadyPassedThisYear() {
        val birth = LocalDate.of(1995, 3, 15)
        val asOf = LocalDate.of(2026, 10, 14)

        assertFalse(BirthdayCalculator.isBirthdayToday(birth, asOf))
        assertTrue(BirthdayCalculator.hasBirthdayReachedThisYear(birth, asOf))
        assertEquals(LocalDate.of(2027, 3, 15), BirthdayCalculator.calculateNextBirthday(birth, asOf))
        assertEquals(152L, BirthdayCalculator.calculateDaysUntilBirthday(birth, asOf))
    }

    @Test
    fun testBirthdayTomorrow() {
        val birth = LocalDate.of(1995, 10, 15)
        val asOf = LocalDate.of(2026, 10, 14)

        assertEquals(1L, BirthdayCalculator.calculateDaysUntilBirthday(birth, asOf))
        assertEquals(LocalDate.of(2026, 10, 15), BirthdayCalculator.calculateNextBirthday(birth, asOf))
    }

    @Test
    fun testBirthdayYesterday() {
        val birth = LocalDate.of(1995, 10, 13)
        val asOf = LocalDate.of(2026, 10, 14)

        // Next birthday will be next year
        assertEquals(LocalDate.of(2027, 10, 13), BirthdayCalculator.calculateNextBirthday(birth, asOf))
        assertEquals(364L, BirthdayCalculator.calculateDaysUntilBirthday(birth, asOf))
    }

    @Test
    fun testFebruary29_inNonLeapYear_resolvesToFeb28() {
        val birth = LocalDate.of(2000, 2, 29)
        // 2021 is non-leap
        val birthday2021 = BirthdayCalculator.getBirthdayForYear(birth, 2021)
        assertEquals(LocalDate.of(2021, 2, 28), birthday2021)

        // As of 2021-01-01, next birthday is 2021-02-28
        val next = BirthdayCalculator.calculateNextBirthday(birth, LocalDate.of(2021, 1, 1))
        assertEquals(LocalDate.of(2021, 2, 28), next)
    }

    @Test
    fun testFebruary29_inLeapYear_resolvesToFeb29() {
        val birth = LocalDate.of(2000, 2, 29)
        // 2024 is a leap year
        val birthday2024 = BirthdayCalculator.getBirthdayForYear(birth, 2024)
        assertEquals(LocalDate.of(2024, 2, 29), birthday2024)

        val next = BirthdayCalculator.calculateNextBirthday(birth, LocalDate.of(2024, 1, 1))
        assertEquals(LocalDate.of(2024, 2, 29), next)
    }
}
