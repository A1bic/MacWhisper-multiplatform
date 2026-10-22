package ru.kovalenko.repository

import org.jooq.DSLContext
import ru.kovalenko.identity.jooq.tables.references.REFRESH_TOKENS
import ru.kovalenko.repository.entity.NewRefreshToken
import java.time.ZoneOffset
import kotlin.time.toJavaInstant
import kotlin.uuid.toJavaUuid

class RefreshTokenRepositoryImpl(private val dsl: DSLContext) : RefreshTokenRepository {
    override fun save(token: NewRefreshToken) {
        dsl.insertInto(REFRESH_TOKENS)
            .set(REFRESH_TOKENS.USER_ID, token.userId.value.toJavaUuid())
            .set(REFRESH_TOKENS.TOKEN_HASH, token.tokenHash)
            .set(REFRESH_TOKENS.DEVICE_INFO, token.deviceInfo)
            .set(REFRESH_TOKENS.EXPIRES_AT, token.expiresAt.toJavaInstant().atOffset(ZoneOffset.UTC))
            .execute()
    }
}