package com.example.agesnap.domain

import com.example.agesnap.data.model.AgeResult
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formats AgeResult data into human-readable text for copying to the clipboard
 * and sharing via native Android intent.
 */
object AgeShareFormatter {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

    private val numberFormat: NumberFormat =
        NumberFormat.getIntegerInstance(Locale.getDefault())

    /**
     * Formats a complete summary for copy/share.
     */
    fun formatDetailedText(
        birthDate: LocalDate,
        asOfDate: LocalDate,
        result: AgeResult
    ): String {
        return buildString {
            appendLine("Age: ${result.years} years, ${result.months} months, ${result.days} days")
            appendLine("Date of birth: ${birthDate.format(dateFormatter)}")
            appendLine("Calculated as of: ${asOfDate.format(dateFormatter)}")
            appendLine("Total days: ${numberFormat.format(result.totalDays)}")
            appendLine("Total weeks: ${numberFormat.format(result.totalWeeks)}")
            appendLine("Total months: ${numberFormat.format(result.totalMonths)}")
            appendLine("Next birthday: ${result.nextBirthday.format(dateFormatter)}")
            append("Days until birthday: ${numberFormat.format(result.daysUntilBirthday)}")
        }
    }

    /**
     * Formats a compact share summary.
     */
    fun formatCompactText(
        birthDate: LocalDate,
        asOfDate: LocalDate,
        result: AgeResult
    ): String {
        return buildString {
            appendLine("Age: ${result.years} years, ${result.months} months, ${result.days} days")
            appendLine("Born: ${birthDate.format(dateFormatter)}")
            appendLine("As of: ${asOfDate.format(dateFormatter)}")
            append("Next birthday: ${result.nextBirthday.format(dateFormatter)}")
        }
    }
}
