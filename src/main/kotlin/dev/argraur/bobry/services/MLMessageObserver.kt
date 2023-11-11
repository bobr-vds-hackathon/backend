package dev.argraur.bobry.services

import dev.argraur.bobry.ml.MLService
import dev.argraur.bobry.utils.LoggerDelegate
import io.ktor.server.websocket.*
import kotlinx.coroutines.*
import org.koin.ktor.ext.inject
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class MLMessageObserver(
    private val session: DefaultWebSocketServerSession,
    private val id: String
) {
    private val logger by LoggerDelegate()
    private val mlService by session.application.inject<MLService>()

    @OptIn(ExperimentalEncodingApi::class)
    fun startObserving() : Job =
        CoroutineScope(Dispatchers.IO).launch {
            logger.info("Observing events for source id = $id")
            try {
                mlService.outputFlow.collect {
                    logger.info("Collected MLMessage: $it")
                    if (session.isActive && it.id == id) {
                        it.base64Image = Base64.encode(File(MLService.mlServiceOutputPath + "/$id/${it.file}").readBytes())
                        session.sendSerialized(it)
                    }
                }
            } catch (e: CancellationException) {
                logger.info("Source processing was cancelled")
            }
        }
}
