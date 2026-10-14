package com.example.agesnap.data.repository

import android.content.SharedPreferences
import com.example.agesnap.data.model.CalculationHistory
import java.time.LocalDate

interface CalculationHistoryRepository {
    fun getHistory(): List<CalculationHistory>
    fun addCalculation(calculation: CalculationHistory)
    fun clearHistory()
}

class SharedPrefsCalculationHistoryRepository(
    private val preferences: SharedPreferences
) : CalculationHistoryRepository {

    companion object {
        private const val KEY_HISTORY = "calculation_history_json"
        const val MAX_HISTORY_ENTRIES = 10
    }

    override fun getHistory(): List<CalculationHistory> {
        val raw = preferences.getString(KEY_HISTORY, null) ?: return emptyList()
        return deserializeHistory(raw)
    }

    override fun addCalculation(calculation: CalculationHistory) {
        val current = getHistory().toMutableList()

        // Guard against duplicate immediate recalculation with the identical birthDate and asOfDate
        if (current.isNotEmpty()) {
            val mostRecent = current.first()
            if (mostRecent.birthDate == calculation.birthDate && mostRecent.asOfDate == calculation.asOfDate) {
                return
            }
        }

        current.add(0, calculation)
        val capped = if (current.size > MAX_HISTORY_ENTRIES) {
            current.subList(0, MAX_HISTORY_ENTRIES)
        } else {
            current
        }
        persistHistory(capped)
    }

    override fun clearHistory() {
        preferences.edit().remove(KEY_HISTORY).apply()
    }

    private fun persistHistory(history: List<CalculationHistory>) {
        val serialized = serializeHistory(history)
        preferences.edit().putString(KEY_HISTORY, serialized).apply()
    }

    internal fun serializeHistory(history: List<CalculationHistory>): String {
        return history.joinToString(prefix = "[", postfix = "]", separator = ",") { item ->
            """{"id":"${item.id}","birthDate":"${item.birthDate}","asOfDate":"${item.asOfDate}","ageYears":${item.ageYears},"ageMonths":${item.ageMonths},"ageDays":${item.ageDays},"calculatedAt":${item.calculatedAt}}"""
        }
    }

    internal fun deserializeHistory(json: String): List<CalculationHistory> {
        val trimmed = json.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()
        val content = trimmed.substring(1, trimmed.length - 1).trim()
        if (content.isEmpty()) return emptyList()

        val objects = splitJsonObjects(content)
        val list = mutableListOf<CalculationHistory>()
        for (objStr in objects) {
            val id = extractJsonStringField(objStr, "id")
            val birthDateStr = extractJsonStringField(objStr, "birthDate")
            val asOfDateStr = extractJsonStringField(objStr, "asOfDate")
            val ageYears = extractJsonLongField(objStr, "ageYears")?.toInt()
            val ageMonths = extractJsonLongField(objStr, "ageMonths")?.toInt()
            val ageDays = extractJsonLongField(objStr, "ageDays")?.toInt()
            val calculatedAt = extractJsonLongField(objStr, "calculatedAt")

            if (id != null && birthDateStr != null && asOfDateStr != null &&
                ageYears != null && ageMonths != null && ageDays != null && calculatedAt != null) {
                try {
                    list.add(
                        CalculationHistory(
                            id = id,
                            birthDate = LocalDate.parse(birthDateStr),
                            asOfDate = LocalDate.parse(asOfDateStr),
                            ageYears = ageYears,
                            ageMonths = ageMonths,
                            ageDays = ageDays,
                            calculatedAt = calculatedAt
                        )
                    )
                } catch (_: Exception) {
                    // Skip malformed item
                }
            }
        }
        return list
    }

    private fun splitJsonObjects(content: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var inQuotes = false
        var isEscaped = false
        var start = -1

        for (i in content.indices) {
            val c = content[i]
            if (isEscaped) {
                isEscaped = false
                continue
            }
            if (c == '\\') {
                isEscaped = true
                continue
            }
            if (c == '"') {
                inQuotes = !inQuotes
                continue
            }
            if (!inQuotes) {
                if (c == '{') {
                    if (depth == 0) start = i
                    depth++
                } else if (c == '}') {
                    depth--
                    if (depth == 0 && start != -1) {
                        result.add(content.substring(start, i + 1))
                        start = -1
                    }
                }
            }
        }
        return result
    }

    private fun extractJsonStringField(obj: String, field: String): String? {
        val key = "\"$field\""
        val keyIndex = obj.indexOf(key)
        if (keyIndex == -1) return null
        val colonIndex = obj.indexOf(':', keyIndex + key.length)
        if (colonIndex == -1) return null
        val firstQuote = obj.indexOf('"', colonIndex + 1)
        if (firstQuote == -1) return null

        val sb = StringBuilder()
        var isEscaped = false
        for (i in (firstQuote + 1) until obj.length) {
            val c = obj[i]
            if (isEscaped) {
                when (c) {
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    '\\' -> sb.append('\\')
                    '"' -> sb.append('"')
                    else -> sb.append(c)
                }
                isEscaped = false
            } else if (c == '\\') {
                isEscaped = true
            } else if (c == '"') {
                return sb.toString()
            } else {
                sb.append(c)
            }
        }
        return null
    }

    private fun extractJsonLongField(obj: String, field: String): Long? {
        val key = "\"$field\""
        val keyIndex = obj.indexOf(key)
        if (keyIndex == -1) return null
        val colonIndex = obj.indexOf(':', keyIndex + key.length)
        if (colonIndex == -1) return null

        var start = colonIndex + 1
        while (start < obj.length && (obj[start] == ' ' || obj[start] == '\t')) {
            start++
        }
        var end = start
        while (end < obj.length && (obj[end].isDigit() || obj[end] == '-')) {
            end++
        }
        return obj.substring(start, end).toLongOrNull()
    }
}
