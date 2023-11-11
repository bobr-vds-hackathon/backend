package dev.argraur.bobry.managers

import dev.argraur.bobry.ml.MLService
import dev.argraur.bobry.model.Stream
import dev.argraur.bobry.utils.LoggerDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import java.io.File
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.notExists

@Single
class StreamManager {
    private val logger by LoggerDelegate()
    private val streamStorage = "./stream_storage.json"
    private val streams: MutableList<Stream> = if (Path(streamStorage).exists()) {
        try {
            val streamStorageText = File(streamStorage).readText()
            Json.decodeFromString<Array<Stream>>(streamStorageText).toMutableList()
        } catch (e: SerializationException) {
            mutableListOf()
        }
    } else {
        File(streamStorage).createNewFile()
        mutableListOf()
    }

    init {
        updateStorage()
    }

    fun addStream(stream: Stream) : String {
        logger.info("Adding stream = $stream")
        val dup = streams.firstOrNull {
            it.login == stream.login && it.password == stream.password && it.url == stream.url
        }

        if (dup != null) {
            logger.warn("Stream is a duplicate of ${dup.id}")
            stream.id = dup.id
        } else {
            stream.id = UUID.randomUUID().toString()
            streams.add(stream)
        }

        val streamMlPath = MLService.mlServiceInputPath + "/stream_${stream.id}.json"

        if (Path(streamMlPath).notExists())
            File(streamMlPath).writeText(Json.encodeToString(stream))

        updateStorage()

        return stream.id
    }

    private fun updateStorage() {
        CoroutineScope(Dispatchers.IO).launch {
            logger.info("Updating stream storage")
            File(streamStorage).writeText(Json.encodeToString(streams))
        }
    }
}
