package dev.argraur.bobry.services

import dev.argraur.bobry.model.MLMessage
import io.ktor.serialization.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

@Single
class MLService {
    private val mlServicePythonFile = "./real_time_obj_detection.py"
    private val mlServiceInputPath = "./input"
    private val mlServiceOutputPath = "./output"

    private val _outputFlow = MutableSharedFlow<MLMessage>()
    val outputFlow = _outputFlow.asSharedFlow()

    private lateinit var process: Process
    private lateinit var observerJob: Job

    fun startService(): Boolean {
        println("Starting ML service!")

        val processBuilder = ProcessBuilder(
            "py", mlServicePythonFile,
            "-i", mlServiceInputPath,
            "-o", mlServiceOutputPath
        )

        println("Command line: ${processBuilder.command().joinToString(" ")}")

        try {
            process = processBuilder.start()
        } catch (e: IOException) {
            println("Failed to start service: ${e.message}")
            return false
        }

        println("Launching observer job")

        observerJob = CoroutineScope(Dispatchers.IO).launch {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            reader.use {
                while (isActive) {
                    line = it.readLine()
                    println("Read line: $line")
                    if (line != null) {
                        try {
                            val message = Json.decodeFromString<MLMessage>(line!!)
                            println("Decoded to message: $message. Emitting value")
                            _outputFlow.emit(message)
                        } catch (_: SerializationException) {}
                    } else {
                        break
                    }
                }
            }
        }

        return true
    }

    fun stopService() {
        observerJob.cancel()
    }
}
