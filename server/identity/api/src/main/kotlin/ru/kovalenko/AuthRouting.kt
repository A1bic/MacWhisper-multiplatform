package ru.kovalenko

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import ru.kovalenko.operations.RegisterUser
import ru.kovalenko.operations.LoginUser
import ru.kovalenko.services.AuthService

fun Application.configureAuthRouting(authService: AuthService) {
    routing {
        route("/api/v1/auth") {
            post("/register") {
                val request = call.receive<RegisterUser.Request>()
                when (val r = authService.register(request)) {
                    is RegisterUser.Response.Success -> call.respond(HttpStatusCode.Created, r.userDto)
                    is RegisterUser.Response.Failure -> call.respond(HttpStatusCode.BadRequest, r.message)
                }
            }
            post("/login") {
                val request = call.receive<LoginUser.Request>()
                when (val r = authService.login(request)) {
                    is LoginUser.Response.Success -> call.respond(HttpStatusCode.OK, r.dto)
                    is LoginUser.Response.Failure -> call.respond(HttpStatusCode.Unauthorized, r.message)
                }
            }
            post("/refresh") {

            }
            post("/logout") {

            }
        }
    }
}
