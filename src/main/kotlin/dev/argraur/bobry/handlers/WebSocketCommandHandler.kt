package dev.argraur.bobry.handlers

import dev.argraur.bobry.managers.StreamManager
import dev.argraur.bobry.managers.VideoManager
import dev.argraur.bobry.model.Stream
import dev.argraur.bobry.model.WebSocketCommand
import dev.argraur.bobry.model.WebSocketResponse
import dev.argraur.bobry.services.StreamService
import dev.argraur.bobry.services.VideoService
import dev.argraur.bobry.utils.LoggerDelegate
import io.ktor.server.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Factory

const val UUID_COMMAND = "get_uuid"
const val CLOSE_COMMAND = "close"
const val RESUME_COMMAND = "resume"
const val STREAM_COMMAND = "stream"
const val VIDEO_COMMAND = "video"

@Factory
class WebSocketCommandHandler(
    private val videoManager: VideoManager,
    private val streamManager: StreamManager
) {
    private val logger by LoggerDelegate()

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
            VIDEO_COMMAND -> {
                val id = videoManager.addVideoToQueue()
                handler.webSocketSession?.respond("ok", id)
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    val time = System.currentTimeMillis()
                    videoManager.videos.collect {
                        if (it.id == id) {
                            handler.actionJobs.add(VideoService(handler.webSocketSession!!, it).startObserving())
                            logger.info("Video was added. Cancelling scope")
                            scope.cancel()
                        }
                        if (System.currentTimeMillis() - time > 1000 * 60 * 10) {
                            logger.info("Video observing scope has timed out")
                            scope.cancel()
                        }
                    }
                }
            }
            else -> handler.webSocketSession?.respond("bad_request", "")
        }
    }

    private suspend fun DefaultWebSocketServerSession.respond(status: String, data: String) =
        sendSerialized(WebSocketResponse(status, data))
}
