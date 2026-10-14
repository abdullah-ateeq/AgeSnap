package com.example.agesnap.data.repository

import android.content.SharedPreferences
import com.example.agesnap.data.model.AgeProfile
import java.time.LocalDate

interface ProfileRepository {
    fun getProfiles(): List<AgeProfile>
    fun saveProfile(profile: AgeProfile): Boolean
    fun updateProfile(profile: AgeProfile): Boolean
    fun deleteProfile(profileId: String): Boolean
    fun getProfileById(profileId: String): AgeProfile?
    fun clearAllProfiles()
}

class SharedPrefsProfileRepository(
    private val preferences: SharedPreferences
) : ProfileRepository {

    companion object {
        private const val KEY_PROFILES = "saved_profiles_json"
        const val MAX_PROFILES = 20
    }

    override fun getProfiles(): List<AgeProfile> {
        val raw = preferences.getString(KEY_PROFILES, null) ?: return emptyList()
        return deserializeProfiles(raw)
    }

    override fun saveProfile(profile: AgeProfile): Boolean {
        val current = getProfiles().toMutableList()
        if (current.size >= MAX_PROFILES) {
            return false
        }
        val safeName = profile.name.trim().ifEmpty { "My Age" }
        val toSave = profile.copy(name = safeName)
        // If profile with same id already exists, update it instead
        val existingIndex = current.indexOfFirst { it.id == toSave.id }
        if (existingIndex >= 0) {
            current[existingIndex] = toSave
        } else {
            current.add(toSave)
        }
        persistProfiles(current)
        return true
    }

    override fun updateProfile(profile: AgeProfile): Boolean {
        val current = getProfiles().toMutableList()
        val index = current.indexOfFirst { it.id == profile.id }
        if (index == -1) return false
        val safeName = profile.name.trim().ifEmpty { "My Age" }
        current[index] = profile.copy(name = safeName)
        persistProfiles(current)
        return true
    }

    override fun deleteProfile(profileId: String): Boolean {
        val current = getProfiles().toMutableList()
        val removed = current.removeAll { it.id == profileId }
        if (removed) {
            persistProfiles(current)
        }
        return removed
    }

    override fun getProfileById(profileId: String): AgeProfile? {
        return getProfiles().firstOrNull { it.id == profileId }
    }

    override fun clearAllProfiles() {
        preferences.edit().remove(KEY_PROFILES).apply()
    }

    private fun persistProfiles(profiles: List<AgeProfile>) {
        val serialized = serializeProfiles(profiles)
        preferences.edit().putString(KEY_PROFILES, serialized).apply()
    }

    internal fun serializeProfiles(profiles: List<AgeProfile>): String {
        return profiles.joinToString(prefix = "[", postfix = "]", separator = ",") { profile ->
            val escapedName = profile.name
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
            """{"id":"${profile.id}","name":"$escapedName","birthDate":"${profile.birthDate}"}"""
        }
    }

    internal fun deserializeProfiles(json: String): List<AgeProfile> {
        val trimmed = json.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()
        val content = trimmed.substring(1, trimmed.length - 1).trim()
        if (content.isEmpty()) return emptyList()

        // Match individual object tokens
        val objects = splitJsonObjects(content)
        val list = mutableListOf<AgeProfile>()
        for (objStr in objects) {
            val id = extractJsonStringField(objStr, "id")
            val name = extractJsonStringField(objStr, "name")
            val birthDateStr = extractJsonStringField(objStr, "birthDate")
            if (id != null && name != null && birthDateStr != null) {
                try {
                    val date = LocalDate.parse(birthDateStr)
                    list.add(AgeProfile(id = id, name = name, birthDate = date))
                } catch (_: Exception) {
                    // Skip corrupt entry
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
}
