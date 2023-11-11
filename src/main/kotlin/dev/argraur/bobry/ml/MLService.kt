package dev.argraur.bobry.ml

import dev.argraur.bobry.model.MLMessage
import dev.argraur.bobry.server.BobrServer
import dev.argraur.bobry.utils.LoggerDelegate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@Single
class MLService {
    companion object {
        val dir = System.getProperty("user.dir")
        val mlServicePythonFile = "$dir/ml/real_time_obj_detection.py"
        val mlServiceInputPath = "$dir/input"
        val mlServiceOutputPath = "$dir/output"
    }

    private val _outputFlow = MutableSharedFlow<MLMessage>()
    val outputFlow = _outputFlow.asSharedFlow()

    private lateinit var process: Process
    private lateinit var observerJob: Job

    private val logger by LoggerDelegate()

    val server by KoinJavaComponent.inject<BobrServer>(BobrServer::class.java)

    fun startService(): Boolean {
        println("Dir: $dir")
        logger.info("Starting ML service!")

        val processBuilder = ProcessBuilder(
            "python", mlServicePythonFile,
            "-i", mlServiceInputPath,
            "-o", mlServiceOutputPath
        )

        logger.info("Command line: ${processBuilder.command().joinToString(" ")}")

        try {
            process = processBuilder.start()
        } catch (e: IOException) {
            logger.error("Failed to start service: ${e.message}")
            server.stop()
            return false
        }

        logger.info("Launching observer job")

        observerJob = CoroutineScope(Dispatchers.IO).launch {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errReader = BufferedReader(InputStreamReader(process.errorStream))
            reader.use { i ->
                errReader.use { e ->
                    while (process.isAlive && isActive) {
                        val line = i.readLine()
                        val errLine = e.readLine()
                        if (line != null) {
                            logger.info("Read line from STDOUT: $line")
                            try {
                                val message = Json.decodeFromString<MLMessage>(line!!)
                                logger.info("Decoded to message: $message. Emitting value")
                                _outputFlow.emit(message)
                            } catch (_: SerializationException) { }
                        }
                        if (errLine != null) {
                            logger.error(errLine)
                        }
                        delay(100)
                    }
                }
            }

            if (!process.isAlive) {
                logger.error("ML Python service died!")
                BufferedReader(InputStreamReader(process.errorStream)).use { errorReader ->
                    errorReader.lines().forEach { logger.error(it) }
                }
            }

            if (!isActive) {
                logger.info("MLService observer job has been stopped!")
                logger.info("Stopping Python service...")
                process.onExit().whenCompleteAsync { _, _ ->
                    logger.info("ML Python service was stopped successfully")
                }
            }
            server.stop()
        }
        return true
    }

    fun stopService() {
        observerJob.cancel()
    }
}
