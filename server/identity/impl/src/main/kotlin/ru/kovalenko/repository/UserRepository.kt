package ru.kovalenko.repository

import ru.kovalenko.UserId
import ru.kovalenko.repository.entity.NewUser
import ru.kovalenko.repository.entity.UserEntity

interface UserRepository {
    fun save(user: NewUser): UserEntity
    fun findByEmail(email: String): UserEntity?
    fun delete(id: UserId)
}