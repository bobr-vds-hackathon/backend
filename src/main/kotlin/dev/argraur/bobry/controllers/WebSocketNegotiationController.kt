package dev.argraur.bobry.controllers

import dev.argraur.bobry.controllers.annotations.WebSocket
import dev.argraur.bobry.handlers.WebSocketHandlerManager
import io.ktor.server.websocket.*
import org.koin.core.annotation.Single
import org.koin.ktor.ext.inject

@Single
class WebSocketNegotiationController : ApiController {
    override val route: String get() = "/ws"

    @WebSocket suspend fun DefaultWebSocketServerSession.session() =
        application.inject<WebSocketHandlerManager>().value.requestHandler(this)
}
