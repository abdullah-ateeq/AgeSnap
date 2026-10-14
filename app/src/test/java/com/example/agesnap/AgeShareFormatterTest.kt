package com.example.agesnap

import com.example.agesnap.domain.AgeCalculator
import com.example.agesnap.domain.AgeShareFormatter
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AgeShareFormatterTest {

    @Test
    fun testDetailedShareText() {
        val birth = LocalDate.of(1995, 3, 15)
        val asOf = LocalDate.of(2026, 10, 14)
        val result = AgeCalculator.calculateAge(birth, asOf)

        val text = AgeShareFormatter.formatDetailedText(birth, asOf, result)

        assertTrue(text.contains("Age: 31 years, 6 months, 29 days"))
        assertTrue(text.contains("Date of birth:"))
        assertTrue(text.contains("1995"))
        assertTrue(text.contains("Calculated as of:"))
        assertTrue(text.contains("2026"))
        assertTrue(text.contains("Total days:"))
        assertTrue(text.contains("Total weeks: 1,648"))
        assertTrue(text.contains("Total months: 378"))
        assertTrue(text.contains("Next birthday:"))
        assertTrue(text.contains("2027"))
        assertTrue(text.contains("Days until birthday: 152"))
    }

    @Test
    fun testCompactShareText() {
        val birth = LocalDate.of(1995, 3, 15)
        val asOf = LocalDate.of(2026, 10, 14)
        val result = AgeCalculator.calculateAge(birth, asOf)

        val text = AgeShareFormatter.formatCompactText(birth, asOf, result)

        assertTrue(text.contains("Age: 31 years, 6 months, 29 days"))
        assertTrue(text.contains("Born:"))
        assertTrue(text.contains("As of:"))
        assertTrue(text.contains("Next birthday:"))
    }
}
