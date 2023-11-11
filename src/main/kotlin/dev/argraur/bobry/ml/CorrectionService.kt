package dev.argraur.bobry.ml

import dev.argraur.bobry.utils.LoggerDelegate
import org.koin.core.annotation.Single
import java.io.IOException
import kotlin.io.path.Path
import kotlin.io.path.createDirectory

@Single
class CorrectionService {
    companion object {
        val correctionPythonFile = "/app/ml/airflow/DAG_Fine_Tune.py"
        val correctionDatasetFolder = "/dataset"
        val correctionTrashFolder = "/dataset/images"
    }

    private val logger by LoggerDelegate()

    fun startService() {
        try {
            Path(correctionDatasetFolder).createDirectory()
            Path(correctionTrashFolder).createDirectory()
        } catch (_: FileAlreadyExistsException) {}

        logger.info("Starting ML Auto-correction service!")

        val processBuilder = ProcessBuilder("python", correctionPythonFile, correctionDatasetFolder)

        logger.info("Command line: ${processBuilder.command().joinToString(" ")}")

        try {
            processBuilder.start()
        } catch (e: IOException) {
            logger.error("Failed to start service: ${e.message}")
        }
    }
}
