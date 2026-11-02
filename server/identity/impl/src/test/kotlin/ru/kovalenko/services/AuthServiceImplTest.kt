package ru.kovalenko.services

import com.auth0.jwt.JWT
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.kovalenko.JwtConfig
import ru.kovalenko.PasswordHasher
import ru.kovalenko.TokenService
import ru.kovalenko.UserId
import ru.kovalenko.operations.LoginUser
import ru.kovalenko.operations.RegisterUser
import ru.kovalenko.repository.RefreshTokenRepository
import ru.kovalenko.repository.UserRepository
import ru.kovalenko.repository.entity.NewRefreshToken
import ru.kovalenko.repository.entity.NewUser
import ru.kovalenko.repository.entity.UserEntity
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

class AuthServiceImplTest {
    private val cfg = JwtConfig("test-secret", "test-issuer", "test-audience", "test-realm", 15.minutes, 30.days)
    private val users = FakeUserRepository()
    private val refreshTokens = FakeRefreshTokenRepository()
    private val passwords = PasswordHasher()
    private val tokens = TokenService(cfg)
    private val service = AuthServiceImpl(users, refreshTokens, passwords, tokens, cfg)

    @Test
    fun `register rejects email without at sign`() {
        val response = service.register(RegisterUser.Request("demo.test.ru", "password123"))

        assertThat(response).isEqualTo(RegisterUser.Response.Failure("Invalid Email"))
        assertThat(users.saved).isEmpty()
    }

    @Test
    fun `register rejects password shorter than 8 characters`() {
        val response = service.register(RegisterUser.Request("demo@test.ru", "1234567"))

        assertThat(response).isInstanceOf(RegisterUser.Response.Failure::class.java)
        assertThat(users.saved).isEmpty()
    }

    @Test
    fun `register accepts password of exactly 8 characters`() {
        val response = service.register(RegisterUser.Request("demo@test.ru", "12345678"))

        assertThat(response).isInstanceOf(RegisterUser.Response.Success::class.java)
    }

    @Test
    fun `register rejects already used email ignoring case`() {
        service.register(RegisterUser.Request("demo@test.ru", "password123"))

        val response = service.register(RegisterUser.Request("DEMO@test.ru", "password456"))

        assertThat(response).isEqualTo(RegisterUser.Response.Failure("Email is already in use"))
        assertThat(users.saved).hasSize(1)
    }

    @Test
    fun `register stores hashed password and display name`() {
        service.register(RegisterUser.Request("demo@test.ru", "password123", displayName = "Demo"))

        val user = users.saved.single()
        assertThat(user.email).isEqualTo("demo@test.ru")
        assertThat(user.displayName).isEqualTo("Demo")
        assertThat(user.passwordHash).isNotEqualTo("password123")
        assertThat(passwords.verify("password123", user.passwordHash)).isTrue()
    }

    @Test
    fun `register returns tokens for the new user`() {
        val response = service.register(RegisterUser.Request("demo@test.ru", "password123"))

        val dto = (response as RegisterUser.Response.Success).userDto
        val user = users.saved.single()
        assertThat(dto.user.id).isEqualTo(user.id.value)
        assertThat(dto.user.email).isEqualTo("demo@test.ru")
        assertThat(dto.tokenType).isEqualTo("Bearer")
        assertThat(dto.expiresIn).isEqualTo(15 * 60L)
        assertThat(JWT.decode(dto.accessToken).subject).isEqualTo(user.id.value.toString())
    }

    @Test
    fun `register saves only hash of refresh token`() {
        val before = Clock.System.now()
        val response = service.register(RegisterUser.Request("demo@test.ru", "password123", deviceInfo = "Pixel 8"))
        val after = Clock.System.now()

        val dto = (response as RegisterUser.Response.Success).userDto
        val saved = refreshTokens.saved.single()
        assertThat(saved.userId).isEqualTo(users.saved.single().id)
        assertThat(saved.tokenHash)
            .isNotEqualTo(dto.refreshToken)
            .isEqualTo(tokens.hashRefreshToken(dto.refreshToken))
        assertThat(saved.deviceInfo).isEqualTo("Pixel 8")
        assertThat(saved.expiresAt).isBetween(before + 30.days, after + 30.days)
    }

    @Test
    fun `login rejects email without at sign`() {
        val response = service.login(LoginUser.Request("demo.test.ru", "password123"))

        assertThat(response).isEqualTo(LoginUser.Response.Failure("Invalid Email"))
    }

    @Test
    fun `login rejects password shorter than 8 characters`() {
        val response = service.login(LoginUser.Request("demo@test.ru", "1234567"))

        assertThat(response).isInstanceOf(LoginUser.Response.Failure::class.java)
    }

    @Test
    fun `login rejects unknown email`() {
        val response = service.login(LoginUser.Request("nobody@test.ru", "password123"))

        assertThat(response).isEqualTo(LoginUser.Response.Failure("There is no user with such email"))
        assertThat(refreshTokens.saved).isEmpty()
    }

    @Test
    fun `login rejects wrong password`() {
        service.register(RegisterUser.Request("demo@test.ru", "password123"))
        refreshTokens.saved.clear()

        val response = service.login(LoginUser.Request("demo@test.ru", "password999"))

        assertThat(response).isEqualTo(LoginUser.Response.Failure("Password does not match"))
        assertThat(refreshTokens.saved).isEmpty()
    }

    @Test
    fun `login returns new tokens for correct password`() {
        service.register(RegisterUser.Request("demo@test.ru", "password123"))
        refreshTokens.saved.clear()

        val response = service.login(LoginUser.Request("Demo@Test.ru", "password123", deviceInfo = "iPhone"))

        val dto = (response as LoginUser.Response.Success).dto
        val user = users.saved.single()
        assertThat(dto.user.id).isEqualTo(user.id.value)
        assertThat(JWT.decode(dto.accessToken).subject).isEqualTo(user.id.value.toString())
        assertThat(refreshTokens.saved.single().tokenHash).isEqualTo(tokens.hashRefreshToken(dto.refreshToken))
        assertThat(refreshTokens.saved.single().deviceInfo).isEqualTo("iPhone")
    }

    private class FakeUserRepository : UserRepository {
        val saved = mutableListOf<UserEntity>()

        override fun save(user: NewUser): UserEntity {
            val now = Clock.System.now()
            return UserEntity(UserId(Uuid.random()), user.email, user.password, user.displayName, now, now)
                .also { saved += it }
        }

        override fun findByEmail(email: String): UserEntity? =
            saved.find { it.email.equals(email, ignoreCase = true) }

        override fun delete(id: UserId) {
            saved.removeIf { it.id == id }
        }
    }

    private class FakeRefreshTokenRepository : RefreshTokenRepository {
        val saved = mutableListOf<NewRefreshToken>()

        override fun save(token: NewRefreshToken) {
            saved += token
        }
    }
}
