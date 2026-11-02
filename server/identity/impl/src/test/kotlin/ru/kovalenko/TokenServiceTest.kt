package ru.kovalenko

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlin.uuid.Uuid

class TokenServiceTest {
    private val cfg = JwtConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm",
        accessTtl = 15.minutes,
        refreshTtl = 30.days,
    )
    private val tokens = TokenService(cfg)
    private val userId = UserId(Uuid.random())

    private fun verifier(secret: String = cfg.secret) =
        JWT.require(Algorithm.HMAC256(secret))
            .withIssuer(cfg.issuer)
            .withAudience(cfg.audience)
            .build()

    @Test
    fun `access token carries issuer, audience and user id`() {
        val decoded = verifier().verify(tokens.createAccessToken(userId))

        assertThat(decoded.issuer).isEqualTo("test-issuer")
        assertThat(decoded.audience).containsExactly("test-audience")
        assertThat(decoded.subject).isEqualTo(userId.value.toString())
    }

    @Test
    fun `access token expires after access ttl`() {
        val before = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val token = tokens.createAccessToken(userId)
        val after = Instant.now()

        val ttl = cfg.accessTtl.toJavaDuration()
        assertThat(verifier().verify(token).expiresAtAsInstant).isBetween(before + ttl, after + ttl)
    }

    @Test
    fun `access token signed with another secret is rejected`() {
        val token = tokens.createAccessToken(userId)

        assertThatThrownBy { verifier("another-secret").verify(token) }
            .isInstanceOf(JWTVerificationException::class.java)
    }

    @Test
    fun `access token from another issuer is rejected`() {
        val foreign = TokenService(cfg.copy(issuer = "someone-else")).createAccessToken(userId)

        assertThatThrownBy { verifier().verify(foreign) }
            .isInstanceOf(JWTVerificationException::class.java)
    }

    @Test
    fun `refresh token is url safe random string`() {
        val first = tokens.createRefreshToken()
        val second = tokens.createRefreshToken()

        assertThat(first).hasSize(43).matches("[A-Za-z0-9_-]+")
        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun `refresh token hash is stable sha256 hex`() {
        val hash = tokens.hashRefreshToken("token")

        assertThat(hash)
            .isEqualTo(tokens.hashRefreshToken("token"))
    }

    @Test
    fun `different refresh tokens have different hashes`() {
        assertThat(tokens.hashRefreshToken("token-1")).isNotEqualTo(tokens.hashRefreshToken("token-2"))
    }
}
