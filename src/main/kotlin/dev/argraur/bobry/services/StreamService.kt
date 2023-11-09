package dev.argraur.bobry.services

import dev.argraur.bobry.model.Stream
import io.ktor.server.websocket.*
import kotlinx.coroutines.*
import org.koin.ktor.ext.inject

class StreamService(
    private val session: DefaultWebSocketServerSession,
    private val stream: Stream
) {
    private val mlService by session.application.inject<MLService>()
    val id = stream.id

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
