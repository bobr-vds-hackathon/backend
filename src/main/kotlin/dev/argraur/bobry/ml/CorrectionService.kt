package dev.argraur.bobry.ml

import dev.argraur.bobry.utils.LoggerDelegate
import java.io.IOException
import kotlin.io.path.Path
import kotlin.io.path.createDirectory

class CorrectionService {
    companion object {
        val dir = System.getProperty("user.dir")
        val correctionPythonFile = "$dir/ml/airflow/DAG_Fine_Tune.py"
        val correctionDatasetFolder = "$dir/ml/dataset"
        val correctionTrashFolder = "$dir/ml/dataset/images"
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
