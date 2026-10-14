package com.example.agesnap.domain

import com.example.agesnap.data.model.AgeResult
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

/**
 * Core domain service responsible for exact calendar-based age calculations.
 *
 * All date calculations use standard java.time classes (LocalDate, Period, ChronoUnit)
 * to strictly prevent approximation errors (e.g. 30-day months or 365-day years).
 * No Compose code is present in this class.
 */
object AgeCalculator {

    /**
     * Calculates the exact age and secondary breakdowns.
     *
     * @param birthDate The person's date of birth.
     * @param asOfDate The reference date for the calculation.
     * @return AgeResult containing calendar-accurate years, months, days, total units, and next birthday info.
     * @throws IllegalArgumentException if birthDate is after asOfDate.
     */
    fun calculateAge(
        birthDate: LocalDate,
        asOfDate: LocalDate
    ): AgeResult {
        require(!birthDate.isAfter(asOfDate)) {
            "Birth date cannot be after the calculation date."
        }

        // Exact calendar period: years, months, days
        val period = Period.between(birthDate, asOfDate)
        val years = period.years
        val months = period.months
        val days = period.days

        // Total calendar days
        val totalDays = ChronoUnit.DAYS.between(birthDate, asOfDate)

        // Total complete weeks
        val totalWeeks = totalDays / 7L

        // Total months based on exact Period result (years * 12 + months)
        val totalMonths = (years.toLong() * 12L) + months.toLong()

        // Total calendar hours (totalDays * 24)
        val totalHours = totalDays * 24L

        // Next birthday details
        val nextBirthday = BirthdayCalculator.calculateNextBirthday(birthDate, asOfDate)
        val daysUntilBirthday = BirthdayCalculator.calculateDaysUntilBirthday(birthDate, asOfDate)
        val birthdayReachedThisYear = BirthdayCalculator.hasBirthdayReachedThisYear(birthDate, asOfDate)

        return AgeResult(
            years = years,
            months = months,
            days = days,
            totalDays = totalDays,
            totalWeeks = totalWeeks,
            totalMonths = totalMonths,
            totalHours = totalHours,
            nextBirthday = nextBirthday,
            daysUntilBirthday = daysUntilBirthday,
            birthdayReachedThisYear = birthdayReachedThisYear
        )
    }

    /**
     * Validates birth date and as-of date.
     */
    fun validateDates(
        birthDate: LocalDate?,
        asOfDate: LocalDate,
        today: LocalDate = LocalDate.now()
    ) = AgeValidator.validate(birthDate, asOfDate, today)

    /**
     * Returns the exact Period between two dates.
     */
    fun calculatePeriod(birthDate: LocalDate, asOfDate: LocalDate): Period {
        return Period.between(birthDate, asOfDate)
    }

    /**
     * Calculates total calendar days between birthDate and asOfDate.
     */
    fun calculateTotalDays(birthDate: LocalDate, asOfDate: LocalDate): Long {
        return ChronoUnit.DAYS.between(birthDate, asOfDate)
    }

    /**
     * Calculates total complete weeks (totalDays / 7).
     */
    fun calculateTotalWeeks(birthDate: LocalDate, asOfDate: LocalDate): Long {
        return calculateTotalDays(birthDate, asOfDate) / 7L
    }

    /**
     * Calculates total months (years * 12 + months) based on exact Period.
     */
    fun calculateTotalMonths(birthDate: LocalDate, asOfDate: LocalDate): Long {
        val period = Period.between(birthDate, asOfDate)
        return (period.years.toLong() * 12L) + period.months.toLong()
    }

    /**
     * Calculates total calendar hours (totalDays * 24).
     */
    fun calculateTotalHours(birthDate: LocalDate, asOfDate: LocalDate): Long {
        return calculateTotalDays(birthDate, asOfDate) * 24L
    }

    /**
     * Calculates the next birthday relative to the as-of date.
     */
    fun calculateNextBirthday(birthDate: LocalDate, asOfDate: LocalDate): LocalDate {
        return BirthdayCalculator.calculateNextBirthday(birthDate, asOfDate)
    }

    /**
     * Calculates exact days until the next birthday.
     */
    fun calculateDaysUntilBirthday(birthDate: LocalDate, asOfDate: LocalDate): Long {
        return BirthdayCalculator.calculateDaysUntilBirthday(birthDate, asOfDate)
    }
}
