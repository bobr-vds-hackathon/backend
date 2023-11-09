package dev.argraur.bobry

import dev.argraur.bobry.di.server.ServerModule
import dev.argraur.bobry.di.server.engineModule
import dev.argraur.bobry.server.BobrServer
import io.ktor.server.application.*
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import org.koin.ksp.generated.module

fun main() {
    startKoin {
        modules(engineModule, ServerModule().module)
    }

    val server by inject<BobrServer>(BobrServer::class.java)

    server.start()
}
