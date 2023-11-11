package dev.argraur.bobry.controllers

import dev.argraur.bobry.controllers.annotations.HttpGet
import dev.argraur.bobry.services.HelloService
import dev.argraur.bobry.utils.HttpContext
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.koin.core.annotation.Single
import org.koin.ktor.ext.inject

@Single
class HelloController : ApiController {
    override val route: String get() = "/hello"

    @HttpGet("/read") suspend fun HttpContext.hello() {
        val service by application.inject<HelloService>()

        call.respondText { service.invoke() }
    }
}
