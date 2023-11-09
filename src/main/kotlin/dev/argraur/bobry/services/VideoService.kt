package dev.argraur.bobry.services

import dev.argraur.bobry.model.Video
import io.ktor.server.websocket.*
import kotlinx.coroutines.*
import org.koin.ktor.ext.inject

class VideoService(
    private val session: DefaultWebSocketServerSession,
    private val video: Video
) {
    private val mlService by session.application.inject<MLService>()
    val id = video.id

    fun startObserving() : Job =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // TODO: Put input files
                mlService.outputFlow.collect {
                    if (session.isActive && it.id == id)
                        session.sendSerialized(it)
                }
            } catch (e: CancellationException) {
                println("Stream processing was cancelled")
            }
        }
}
