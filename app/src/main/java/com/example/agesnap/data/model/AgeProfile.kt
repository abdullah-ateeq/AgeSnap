package com.example.agesnap.data.model

import java.time.LocalDate

/**
 * Represents a saved profile with a birth date.
 *
 * @param id Unique identifier (UUID string).
 * @param name Profile display name (defaults to "My Age" if empty).
 * @param birthDate The birth date of the person.
 */
data class AgeProfile(
    val id: String,
    val name: String,
    val birthDate: LocalDate
)
