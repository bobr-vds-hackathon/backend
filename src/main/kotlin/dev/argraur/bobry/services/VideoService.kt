package dev.argraur.bobry.services

import dev.argraur.bobry.ml.MLService
import dev.argraur.bobry.model.Video
import dev.argraur.bobry.utils.LoggerDelegate
import io.ktor.server.websocket.*
import kotlinx.coroutines.*
import org.koin.ktor.ext.inject
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class VideoService(
    private val session: DefaultWebSocketServerSession,
    private val video: Video
) {
    private val logger by LoggerDelegate()
    private val mlService by session.application.inject<MLService>()
    val id = video.id

    @OptIn(ExperimentalEncodingApi::class)
    fun startObserving() : Job =
        CoroutineScope(Dispatchers.IO).launch {
            logger.info("Observing events for video id = ${video.id}")
            try {
                mlService.outputFlow.collect {
                    logger.info("Collected MLMessage: $it")
                    if (session.isActive && it.id == id) {
                        it.base64Image = Base64.encode(File(MLService.mlServiceOutputPath + "/$id/${it.file}").readBytes())
                        session.sendSerialized(it)
                    }
                }
            } catch (e: CancellationException) {
                logger.info("Video processing was cancelled")
            }
        }
}
