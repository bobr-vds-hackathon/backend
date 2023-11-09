package dev.argraur.bobry.handlers

import dev.argraur.bobry.model.WebSocketCommand
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Factory
import org.koin.ktor.ext.get
import java.util.UUID
import java.util.concurrent.CancellationException

@Factory
class WebSocketHandler {
    val sessionUUID: UUID by lazy { UUID.randomUUID() }

    var webSocketSession: DefaultWebSocketServerSession? = null
    private lateinit var webSocketCommandHandler : WebSocketCommandHandler

    private var sessionJob: Job? = null
    val actionJobs: MutableList<Job> = mutableListOf()

    suspend fun handleSession(session: DefaultWebSocketServerSession) {
        webSocketSession = session
        with(webSocketSession!!) {
            webSocketCommandHandler = application.get<WebSocketCommandHandler>()
            sessionJob = CoroutineScope(Dispatchers.Default).launch {
                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            println("Received a frame!")
                            val text = frame.readText()
                            println("Text: $text")
                            val command = Json.decodeFromString<WebSocketCommand>(text)
                            println("Command: $command")
                            webSocketCommandHandler.handleCommand(this@WebSocketHandler, command)
                        }
                    }
                } catch (e: CancellationException) {
                    e.printStackTrace()
                    println("Session closed!")
                }
            }
            sessionJob?.join()
        }
    }

    suspend fun resumeSession(session: DefaultWebSocketServerSession) {
        // Logic for resuming session
        println("Resuming session in new handler. ID = $sessionUUID")
        handleSession(session)
    }

    suspend fun closeSession() {
        sessionJob?.cancel(CancellationException("Session has been closed"))
        webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Session closed"))
        sessionJob = null
        webSocketSession = null
    }

    suspend fun resumeOtherSession(uuid: String) {
        println("Resuming session: $uuid")
        val manager = webSocketSession?.application?.get<WebSocketHandlerManager>()
        manager?.changeHandler(webSocketSession!!, this@WebSocketHandler, uuid)
        sessionJob?.cancel(CancellationException("Handler is changing"))
        sessionJob = null
    }
}
