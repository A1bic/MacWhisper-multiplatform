package ru.kovalenko.repository.mappers

import ru.kovalenko.dto.UserDto
import ru.kovalenko.UserId
import ru.kovalenko.identity.jooq.tables.records.UsersRecord
import ru.kovalenko.repository.entity.UserEntity
import kotlin.time.toKotlinInstant
import kotlin.uuid.toKotlinUuid

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