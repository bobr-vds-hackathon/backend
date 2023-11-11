package dev.argraur.bobry.managers

import dev.argraur.bobry.model.Video
import dev.argraur.bobry.ml.MLService
import dev.argraur.bobry.utils.LoggerDelegate
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.io.FileOutputStream
import java.util.*

@Single
class VideoManager {
    private val logger by LoggerDelegate()
    private val videoQueue: MutableList<String> = mutableListOf()
    private val _videos: MutableSharedFlow<Video> = MutableSharedFlow()
    val videos: SharedFlow<Video> get() = _videos

    fun addVideoToQueue(): String {
        val videoId = UUID.randomUUID().toString()

        videoQueue.add(videoId)

        return videoId
    }

    suspend fun saveVideo(id: String, videoFilePart: PartData.FileItem) : Boolean {
        if (!videoQueue.contains(id)) {
            logger.info("Unknown video id. Ignoring")
            return false
        }

        logger.info("Saving video with id = $id")

        val stream = videoFilePart.streamProvider()

        val path = MLService.mlServiceInputPath + "/video_$id.mp4"

        withContext(Dispatchers.IO) {
            val fileStream = FileOutputStream(path)
            logger.info("Writing to $path")
            stream.copyTo(fileStream)
            stream.close()
            fileStream.close()
        }

        val video = Video(
            id = id,
            file = path
        )

        logger.info("Emitting video to the flow")
        _videos.emit(video)

        return true
    }
}
