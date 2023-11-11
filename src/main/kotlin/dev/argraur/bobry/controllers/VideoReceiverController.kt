package dev.argraur.bobry.controllers

import dev.argraur.bobry.controllers.annotations.HttpPost
import dev.argraur.bobry.handlers.WebSocketHandlerManager
import dev.argraur.bobry.managers.VideoManager
import dev.argraur.bobry.utils.HttpContext
import dev.argraur.bobry.utils.LoggerDelegate
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import io.ktor.util.pipeline.*
import org.koin.core.annotation.Single
import org.koin.ktor.ext.inject

@Single
class VideoReceiverController : ApiController {
    override val route: String get() = "/video"

    private val logger by LoggerDelegate()

    @HttpPost("/submit") suspend fun PipelineContext<*, ApplicationCall>.submitVideo() {
        val videoManager by application.inject<VideoManager>()

        val multipart = call.receiveMultipart()
        var videoFilePart: PartData.FileItem? = null
        var videoIdPart: PartData.FormItem? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem ->
                    if (part.name == "id")
                        videoIdPart = part

                is PartData.FileItem ->
                    if (part.name == "video" && part.originalFileName?.endsWith(".mp4") == true)
                        videoFilePart = part

                else -> part.dispose()
            }
        }

        if (videoFilePart == null || videoIdPart == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing video file or ID")
            return
        }

        val videoId = videoIdPart?.value

        logger.info("Received videoId = $videoId.mp4")
        logger.info("Video file name: ${videoFilePart?.originalFileName}")

        val result = videoManager.saveVideo(videoId!!, videoFilePart!!)

        videoFilePart!!.dispose()
        videoIdPart!!.dispose()

        if (result)
            call.respond(HttpStatusCode.OK)

        call.respond(HttpStatusCode.InternalServerError)
    }
}
