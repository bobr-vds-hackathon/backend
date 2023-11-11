package dev.argraur.bobry.server

import dev.argraur.bobry.controllers.di.ControllerModule
import dev.argraur.bobry.controllers.utils.routeControllers
import dev.argraur.bobry.di.application.HandlerModule
import dev.argraur.bobry.di.application.MLModule
import dev.argraur.bobry.di.application.ManagerModule
import dev.argraur.bobry.di.application.ServiceModule
import dev.argraur.bobry.ml.CorrectionService
import dev.argraur.bobry.ml.MLService
import dev.argraur.bobry.utils.Constants
import dev.argraur.bobry.utils.LoggerDelegate
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
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.time.Duration
import kotlin.system.exitProcess

fun Application.setup() {
    val logger by LoggerDelegate("Application")

    logger.info("Setting up Ktor application...")

    install(Koin) {
        slf4jLogger()
        modules(
            ControllerModule().module,
            ServiceModule().module,
            HandlerModule().module,
            ManagerModule().module,
            MLModule().module
        )
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

    get<MLService>().startService()
    get<CorrectionService>().startService()
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

    private val logger by LoggerDelegate()

    fun start() {
        logger.info("БОБРЫ СИЛА!!!")
        embeddedServer.start(wait = true)
    }

    fun stop() {
        logger.info("Gracefully stopping server...")
        embeddedServer.stop()
        exitProcess(0)
    }
}
