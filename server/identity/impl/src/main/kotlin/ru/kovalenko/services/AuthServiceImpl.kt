package ru.kovalenko.services

import ru.kovalenko.dto.RefreshTokenDto
import ru.kovalenko.operations.RegisterUser
import ru.kovalenko.operations.LoginUser
import ru.kovalenko.operations.UpdateRefreshToken
import ru.kovalenko.JwtConfig
import ru.kovalenko.PasswordHasher
import ru.kovalenko.TokenService
import ru.kovalenko.repository.RefreshTokenRepository
import ru.kovalenko.repository.UserRepository
import ru.kovalenko.repository.entity.NewRefreshToken
import ru.kovalenko.repository.entity.NewUser
import ru.kovalenko.repository.entity.UserEntity
import ru.kovalenko.repository.mappers.toDto
import kotlin.time.Clock

class AuthServiceImpl(
    private val users: UserRepository,
    private val refreshTokens: RefreshTokenRepository,
    private val passwords: PasswordHasher,
    private val tokens: TokenService,
    private val cfg: JwtConfig
) : AuthService {
    override fun register(req: RegisterUser.Request): RegisterUser.Response {
        if (!req.email.contains('@')) return RegisterUser.Response.Failure("Invalid Email")
        if (req.password.length < 8) return RegisterUser.Response.Failure("Password too short (should be more than 8 characters)")
        if (users.findByEmail(req.email) != null) return RegisterUser.Response.Failure("Email is already in use")

        val user = users.save(
            NewUser(
                email = req.email,
                password = passwords.hash(req.password),
                displayName = req.displayName
            )
        )

        return RegisterUser.Response.Success(issueToken(user, req.deviceInfo))
    }

    override fun login(req: LoginUser.Request): LoginUser.Response {
        if (!req.email.contains('@')) return LoginUser.Response.Failure("Invalid Email")
        if (req.password.length < 8) return LoginUser.Response.Failure("Password too short (should be more than 8 characters)")

        val user = users.findByEmail(req.email) ?: return LoginUser.Response.Failure("There is no user with such email")

        if (!passwords.verify(req.password, user.passwordHash)) return LoginUser.Response.Failure("Password does not match")

        return LoginUser.Response.Success(issueToken(user, req.deviceInfo))
    }

    override fun refresh(req: UpdateRefreshToken.Request): UpdateRefreshToken.Response {
        TODO("Not yet implemented")
    }

    private fun issueToken(user: UserEntity, deviceInfo: String?): RefreshTokenDto {
        val refresh = tokens.createRefreshToken()
        refreshTokens.save(
            NewRefreshToken(
                userId = user.id,
                tokenHash = tokens.hashRefreshToken(refresh),
                deviceInfo = deviceInfo,
                expiresAt = Clock.System.now() + cfg.refreshTtl
            )
        )
        return RefreshTokenDto(
            accessToken = tokens.createAccessToken(user.id),
            refreshToken = refresh,
            expiresIn = cfg.accessTtl.inWholeSeconds,
            user = user.toDto()
        )
    }
}