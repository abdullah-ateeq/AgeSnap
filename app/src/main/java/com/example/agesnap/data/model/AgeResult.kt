package com.example.agesnap.data.model

import java.time.LocalDate

/**
 * Represents the detailed calculation result of a person's age.
 *
 * All values are computed using java.time calendar-accurate methods.
 */
data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val totalWeeks: Long,
    val totalMonths: Long,
    val totalHours: Long,
    val nextBirthday: LocalDate,
    val daysUntilBirthday: Long,
    val birthdayReachedThisYear: Boolean
) {
    /**
     * Total calendar minutes based on totalDays * 24 * 60.
     */
    val totalMinutes: Long get() = totalHours * 60L
}
