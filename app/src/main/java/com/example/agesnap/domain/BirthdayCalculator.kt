package com.example.agesnap.domain

import java.time.LocalDate
import java.time.Year
import java.time.temporal.ChronoUnit

/**
 * Handles calculation of birthdays, upcoming birthdays, countdowns, and leap-day adjustments.
 */
object BirthdayCalculator {

    /**
     * Resolves the birthday date for a specific target year.
     *
     * EXPLICIT FEBRUARY 29 BEHAVIOR:
     * For persons born on a leap day (February 29), if the target year is not a leap year,
     * February 28 is consistently chosen as their calendar birthday date for calculation
     * and display purposes. In leap years, February 29 is preserved.
     */
    fun getBirthdayForYear(birthDate: LocalDate, year: Int): LocalDate {
        return if (birthDate.monthValue == 2 && birthDate.dayOfMonth == 29) {
            if (Year.isLeap(year.toLong())) {
                LocalDate.of(year, 2, 29)
            } else {
                LocalDate.of(year, 2, 28)
            }
        } else {
            LocalDate.of(year, birthDate.month, birthDate.dayOfMonth)
        }
    }

    /**
     * Checks if the as-of date coincides with the birthday in that year.
     */
    fun isBirthdayToday(birthDate: LocalDate, asOfDate: LocalDate): Boolean {
        val birthdayThisYear = getBirthdayForYear(birthDate, asOfDate.year)
        return asOfDate.isEqual(birthdayThisYear)
    }

    /**
     * Calculates the next upcoming birthday relative to the as-of date.
     *
     * Rules:
     * 1. If today is the birthday, returns today's birthday.
     * 2. If the birthday has not yet occurred this year, returns this year's birthday.
     * 3. If the birthday has already passed this year, returns next year's birthday.
     */
    fun calculateNextBirthday(birthDate: LocalDate, asOfDate: LocalDate): LocalDate {
        val birthdayThisYear = getBirthdayForYear(birthDate, asOfDate.year)
        return when {
            asOfDate.isEqual(birthdayThisYear) -> birthdayThisYear
            asOfDate.isBefore(birthdayThisYear) -> birthdayThisYear
            else -> getBirthdayForYear(birthDate, asOfDate.year + 1)
        }
    }

    /**
     * Calculates the exact number of days remaining until the next birthday.
     * Returns 0 if today is the birthday.
     */
    fun calculateDaysUntilBirthday(birthDate: LocalDate, asOfDate: LocalDate): Long {
        val nextBirthday = calculateNextBirthday(birthDate, asOfDate)
        return ChronoUnit.DAYS.between(asOfDate, nextBirthday)
    }

    /**
     * Determines whether the birthday has already been reached in the calendar year of asOfDate.
     */
    fun hasBirthdayReachedThisYear(birthDate: LocalDate, asOfDate: LocalDate): Boolean {
        val birthdayThisYear = getBirthdayForYear(birthDate, asOfDate.year)
        return !asOfDate.isBefore(birthdayThisYear)
    }
}
