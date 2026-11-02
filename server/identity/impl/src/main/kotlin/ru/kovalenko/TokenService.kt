package ru.kovalenko

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Date

class TokenService(private val cfg: JwtConfig) {
    private val algorithm = Algorithm.HMAC256(cfg.secret)

    fun createAccessToken(userId: UserId): String =
        JWT.create()
            .withIssuer(cfg.issuer)
            .withAudience(cfg.audience)
            .withSubject(userId.value.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + cfg.accessTtl.inWholeMilliseconds))
            .sign(algorithm)

    fun createRefreshToken(): String {
        val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun hashRefreshToken(token: String): String =
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray()).toHexString()
}