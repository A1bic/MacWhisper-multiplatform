package ru.kovalenko.repository.entity

import ru.kovalenko.UserId
import ru.kovalenko.dto.UserDto
import ru.kovalenko.identity.jooq.tables.records.UsersRecord
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import kotlin.uuid.toKotlinUuid

data class UserEntity(
    val id: UserId,
    val email: String,
    val passwordHash: String,
    val displayName: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

internal fun UsersRecord.toEntity() = UserEntity(
    id = UserId(id!!.toKotlinUuid()),
    email = email,
    passwordHash = passwordHash,
    displayName = displayName,
    createdAt = createdAt!!.toInstant().toKotlinInstant(),
    updatedAt = updatedAt!!.toInstant().toKotlinInstant()
)

internal fun UserEntity.toDto() = UserDto(
    id = id.value,
    email = email,
    displayName = displayName,
    createdAt = createdAt,
    updatedAt = updatedAt
)
