package dev.argraur.bobry.handlers

import dev.argraur.bobry.model.Stream
import dev.argraur.bobry.model.WebSocketCommand
import dev.argraur.bobry.model.WebSocketResponse
import dev.argraur.bobry.services.StreamService
import io.ktor.server.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Factory

const val UUID_COMMAND = "get_uuid"
const val CLOSE_COMMAND = "close"
const val RESUME_COMMAND = "resume"
const val STREAM_COMMAND = "stream"

@Factory
class WebSocketCommandHandler {
    suspend fun handleCommand(handler: WebSocketHandler, command: WebSocketCommand) {
        when(command.command) {
            UUID_COMMAND -> {
                handler.webSocketSession?.respond("ok", handler.sessionUUID.toString())
            }
            CLOSE_COMMAND -> handler.closeSession()
            RESUME_COMMAND -> handler.resumeOtherSession(command.args["uuid"]!!)
            STREAM_COMMAND -> {
                val streamText = command.args["stream"]!!
                val stream = Json.decodeFromString<Stream>(streamText)
                handler.actionJobs.add(StreamService(handler.webSocketSession!!, Stream("","","","",0.0, 0.0)).startObserving())
            }
            else -> handler.webSocketSession?.respond("bad_request", "")
        }
    }

    private suspend fun DefaultWebSocketServerSession.respond(status: String, data: String) =
        sendSerialized(WebSocketResponse(status, data))
}
