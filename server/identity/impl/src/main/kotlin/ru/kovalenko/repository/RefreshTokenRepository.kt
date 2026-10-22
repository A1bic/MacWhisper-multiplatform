package ru.kovalenko.repository

import ru.kovalenko.repository.entity.NewRefreshToken

interface RefreshTokenRepository {
    fun save(token: NewRefreshToken);
}
