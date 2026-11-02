package ru.kovalenko.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class UserDto(
    val id: Uuid,
    val email: String,
    val displayName: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)