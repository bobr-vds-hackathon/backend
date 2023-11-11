package dev.argraur.bobry.handlers

import dev.argraur.bobry.model.WebSocketCommand
import dev.argraur.bobry.utils.LoggerDelegate
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Factory
import org.koin.ktor.ext.get
import java.util.UUID
import java.util.concurrent.CancellationException
import kotlin.math.log

@Factory
class WebSocketHandler {
    val sessionUUID: UUID by lazy { UUID.randomUUID() }

    var webSocketSession: DefaultWebSocketServerSession? = null
    private lateinit var webSocketCommandHandler : WebSocketCommandHandler

    private var sessionJob: Job? = null
    val actionJobs: MutableList<Job> = mutableListOf()

    private val logger by LoggerDelegate()

    suspend fun handleSession(session: DefaultWebSocketServerSession) {
        webSocketSession = session

        with(webSocketSession!!) {
            webSocketCommandHandler = application.get<WebSocketCommandHandler>()
            sessionJob = CoroutineScope(Dispatchers.Default).launch {
                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            logger.debug("Received a frame!")
                            val text = frame.readText()
                            logger.debug("Text: $text")
                            val command = Json.decodeFromString<WebSocketCommand>(text)
                            logger.debug("Command: $command")
                            webSocketCommandHandler.handleCommand(this@WebSocketHandler, command)
                        }
                    }
                } catch (e: CancellationException) {
                    logger.debug("WebSocketHandler session closed!")
                } finally {
                    closeSession()
                }
            }
            sessionJob?.join()
        }
    }

    suspend fun resumeSession(session: DefaultWebSocketServerSession) {
        // Logic for resuming session
        logger.debug("Resuming session in new handler. ID = {}", sessionUUID)
        handleSession(session)
    }

    suspend fun closeSession() {
        logger.info("Closing WebSocketHandler session id = $sessionUUID")
        actionJobs.forEach { it.cancel() }
        sessionJob?.cancel(CancellationException("Session has been closed"))
        webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Session closed"))
        sessionJob = null
        webSocketSession = null
    }

    suspend fun resumeOtherSession(uuid: String) {
        logger.debug("Resuming session: $uuid")
        val manager = webSocketSession?.application?.get<WebSocketHandlerManager>()
        manager?.changeHandler(webSocketSession!!, this@WebSocketHandler, uuid)
        sessionJob?.cancel(CancellationException("Handler is changing"))
        sessionJob = null
    }
}
