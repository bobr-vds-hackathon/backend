package dev.argraur.bobry.controllers

import dev.argraur.bobry.controllers.annotations.HttpGet
import dev.argraur.bobry.controllers.utils.HttpContext
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.koin.core.annotation.Single
import org.koin.ktor.ext.inject

@Single
class PingController : ApiController {
    override val route: String get() = "/"

    @HttpGet suspend fun HttpContext.ping() {
        call.respond("Hey!")
    }
}
