package dev.argraur.bobry.handlers

import dev.argraur.bobry.utils.LoggerDelegate
import io.ktor.server.websocket.*
import org.koin.core.annotation.Single
import org.koin.ktor.ext.get

@Single
class WebSocketHandlerManager {
    private val webSocketHandlers: MutableList<WebSocketHandler> = mutableListOf()
    private val logger by LoggerDelegate()

    suspend fun requestHandler(session: DefaultWebSocketServerSession) {
        val handler = session.application.get<WebSocketHandler>()
        webSocketHandlers.add(handler)
        handler.handleSession(session)
    }

    suspend fun changeHandler(session: DefaultWebSocketServerSession, handler: WebSocketHandler, uuid: String) {
        logger.debug("Changing handler from {} to {}", handler.sessionUUID, uuid)
        val newHandler = webSocketHandlers.first { it.sessionUUID.toString() == uuid }
        webSocketHandlers.remove(handler)
        newHandler.resumeSession(session)
    }
}
