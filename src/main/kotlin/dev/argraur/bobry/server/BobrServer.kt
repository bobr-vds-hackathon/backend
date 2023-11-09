package dev.argraur.bobry.server

import dev.argraur.bobry.controllers.di.ControllerModule
import dev.argraur.bobry.controllers.utils.routeControllers
import dev.argraur.bobry.di.application.HandlerModule
import dev.argraur.bobry.di.application.ServiceModule
import dev.argraur.bobry.services.MLService
import dev.argraur.bobry.utils.Constants
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.koin.ksp.generated.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import java.time.Duration

fun Application.setup() {
    install(Koin) {
        modules(ControllerModule().module, ServiceModule().module, HandlerModule().module)
    }

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    routing {
        routeControllers()
        swaggerUI(path = "swagger")
    }

    install(ContentNegotiation) {
        json()
    }

    inject<MLService>().value.startService()
}

@Single
class BobrServer(
    engineFactory: ApplicationEngineFactory<*, *>
) {
    private var embeddedServer: ApplicationEngine =
        embeddedServer(
            engineFactory,
            port = Constants.SERVER_PORT,
            host = Constants.SERVER_HOST,
            module = Application::setup
        )

    fun start() {
        embeddedServer.start(wait = true)
    }
}
