package com.example.agesnap

import com.example.agesnap.domain.AgeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate

class AgeCalculatorTest {

    @Test
    fun testSameBirthAndAsOfDate_zeroAge() {
        val birth = LocalDate.of(2026, 1, 1)
        val asOf = LocalDate.of(2026, 1, 1)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(0, result.years)
        assertEquals(0, result.months)
        assertEquals(0, result.days)
        assertEquals(0L, result.totalDays)
        assertEquals(0L, result.totalWeeks)
        assertEquals(0L, result.totalMonths)
        assertEquals(0L, result.totalHours)
        assertEquals(0L, result.totalMinutes)
        assertEquals(0L, result.daysUntilBirthday)
        assertTrue(result.birthdayReachedThisYear)
    }

    @Test
    fun testOneDayLater() {
        val birth = LocalDate.of(2026, 1, 1)
        val asOf = LocalDate.of(2026, 1, 2)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(0, result.years)
        assertEquals(0, result.months)
        assertEquals(1, result.days)
        assertEquals(1L, result.totalDays)
        assertEquals(0L, result.totalWeeks)
        assertEquals(0L, result.totalMonths)
        assertEquals(24L, result.totalHours)
        assertEquals(1440L, result.totalMinutes)
    }

    @Test
    fun testOneMonthLater() {
        val birth = LocalDate.of(2026, 1, 1)
        val asOf = LocalDate.of(2026, 2, 1)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(0, result.years)
        assertEquals(1, result.months)
        assertEquals(0, result.days)
        assertEquals(31L, result.totalDays)
        assertEquals(4L, result.totalWeeks)
        assertEquals(1L, result.totalMonths)
        assertEquals(744L, result.totalHours)
    }

    @Test
    fun testOneYearLater() {
        val birth = LocalDate.of(2000, 3, 15)
        val asOf = LocalDate.of(2001, 3, 15)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(1, result.years)
        assertEquals(0, result.months)
        assertEquals(0, result.days)
        assertEquals(365L, result.totalDays)
        assertEquals(52L, result.totalWeeks)
        assertEquals(12L, result.totalMonths)
        assertEquals(365L * 24L, result.totalHours)
    }

    @Test
    fun testPromptExample_15Mar1995_to_14Oct2026() {
        val birth = LocalDate.of(1995, 3, 15)
        val asOf = LocalDate.of(2026, 10, 14)
        val result = AgeCalculator.calculateAge(birth, asOf)

        // Expected: 31 years, 6 months, 29 days
        assertEquals(31, result.years)
        assertEquals(6, result.months)
        assertEquals(29, result.days)

        // Total days: 11,536 (from 1995-03-15 to 2026-10-14)
        assertEquals(11536L, result.totalDays)
        assertEquals(1648L, result.totalWeeks)
        // Total months: 31 * 12 + 6 = 378
        assertEquals(378L, result.totalMonths)
        assertEquals(11536L * 24L, result.totalHours)

        // Next birthday: 15 March 2027
        assertEquals(LocalDate.of(2027, 3, 15), result.nextBirthday)
        assertEquals(152L, result.daysUntilBirthday)
        assertTrue(result.birthdayReachedThisYear)
    }

    @Test
    fun testPromptExample_1Jan2000_to_1Jan2025() {
        val birth = LocalDate.of(2000, 1, 1)
        val asOf = LocalDate.of(2025, 1, 1)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(25, result.years)
        assertEquals(0, result.months)
        assertEquals(0, result.days)
        assertEquals(300L, result.totalMonths)
        assertEquals(0L, result.daysUntilBirthday)
    }

    @Test
    fun testPromptExample_LeapDayBirth_29Feb2000_to_28Feb2021() {
        val birth = LocalDate.of(2000, 2, 29)
        val asOf = LocalDate.of(2021, 2, 28)
        val result = AgeCalculator.calculateAge(birth, asOf)

        // Period.between yields 20y 11m 30d
        assertEquals(20, result.years)
        assertEquals(11, result.months)
        assertEquals(30, result.days)
    }

    @Test
    fun testPromptExample_LeapDayBirth_29Feb2000_to_1Mar2021() {
        val birth = LocalDate.of(2000, 2, 29)
        val asOf = LocalDate.of(2021, 3, 1)
        val result = AgeCalculator.calculateAge(birth, asOf)

        // Period.between yields 21y 0m 1d
        assertEquals(21, result.years)
        assertEquals(0, result.months)
        assertEquals(1, result.days)
    }

    @Test
    fun testLeapDayBirth_to_NextLeapYear() {
        val birth = LocalDate.of(2000, 2, 29)
        val asOf = LocalDate.of(2004, 2, 29)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(4, result.years)
        assertEquals(0, result.months)
        assertEquals(0, result.days)
    }

    @Test
    fun testBirthdayToday() {
        val birth = LocalDate.of(1995, 10, 14)
        val asOf = LocalDate.of(2026, 10, 14)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(31, result.years)
        assertEquals(0, result.months)
        assertEquals(0, result.days)
        assertEquals(0L, result.daysUntilBirthday)
        assertTrue(result.birthdayReachedThisYear)
    }

    @Test
    fun testBirthdayOneDayBefore() {
        val birth = LocalDate.of(1995, 10, 15)
        val asOf = LocalDate.of(2026, 10, 14)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(30, result.years)
        assertEquals(11, result.months)
        assertEquals(29, result.days)
        assertEquals(1L, result.daysUntilBirthday)
        assertFalse(result.birthdayReachedThisYear)
        assertEquals(LocalDate.of(2026, 10, 15), result.nextBirthday)
    }

    @Test
    fun testBirthdayOneDayAfter() {
        val birth = LocalDate.of(1995, 10, 13)
        val asOf = LocalDate.of(2026, 10, 14)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(31, result.years)
        assertEquals(0, result.months)
        assertEquals(1, result.days)
        assertEquals(LocalDate.of(2027, 10, 13), result.nextBirthday)
        assertTrue(result.birthdayReachedThisYear)
    }

    @Test
    fun testJanuary31_to_February28() {
        val birth = LocalDate.of(2023, 1, 31)
        val asOf = LocalDate.of(2023, 2, 28)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(0, result.years)
        assertEquals(0, result.months)
        assertEquals(28, result.days)
        assertEquals(28L, result.totalDays)
    }

    @Test
    fun testMonthBoundary_30Apr_to_1May() {
        val birth = LocalDate.of(2023, 4, 30)
        val asOf = LocalDate.of(2023, 5, 1)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(0, result.years)
        assertEquals(0, result.months)
        assertEquals(1, result.days)
        assertEquals(1L, result.totalDays)
    }

    @Test
    fun testYearBoundary_31Dec_to_1Jan() {
        val birth = LocalDate.of(2025, 12, 31)
        val asOf = LocalDate.of(2026, 1, 1)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(0, result.years)
        assertEquals(0, result.months)
        assertEquals(1, result.days)
        assertEquals(1L, result.totalDays)
    }

    @Test
    fun testLargeAge_Century() {
        val birth = LocalDate.of(1920, 1, 1)
        val asOf = LocalDate.of(2020, 1, 1)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(100, result.years)
        assertEquals(0, result.months)
        assertEquals(0, result.days)
        assertEquals(1200L, result.totalMonths)
    }

    @Test
    fun testHistoricalDate_1850_to_1900() {
        val birth = LocalDate.of(1850, 6, 15)
        val asOf = LocalDate.of(1900, 6, 15)
        val result = AgeCalculator.calculateAge(birth, asOf)

        assertEquals(50, result.years)
        assertEquals(0, result.months)
        assertEquals(0, result.days)
    }

    @Test
    fun testInvalidRange_throwsException() {
        val birth = LocalDate.of(2026, 1, 2)
        val asOf = LocalDate.of(2026, 1, 1)
        try {
            AgeCalculator.calculateAge(birth, asOf)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Birth date cannot be after the calculation date.", e.message)
        }
    }
}
