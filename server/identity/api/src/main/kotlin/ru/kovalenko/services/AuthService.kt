package ru.kovalenko.services

import ru.kovalenko.operations.RegisterUser
import ru.kovalenko.operations.LoginUser
import ru.kovalenko.operations.UpdateRefreshToken

interface AuthService {
    fun register(req: RegisterUser.Request): RegisterUser.Response
    fun login(req: LoginUser.Request): LoginUser.Response
    fun refresh(req: UpdateRefreshToken.Request): UpdateRefreshToken.Response
}