package dev.argraur.bobry.controllers

import dev.argraur.bobry.controllers.annotations.HttpPost
import dev.argraur.bobry.ml.CorrectionService
import dev.argraur.bobry.ml.MLService
import dev.argraur.bobry.model.CorrectionRequest
import dev.argraur.bobry.utils.HttpContext
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.core.annotation.Single
import java.io.File
import java.io.IOException

@Single
class CorrectionController : ApiController {
    override val route: String
        get() = "/correct"

    @HttpPost suspend fun HttpContext.correct() {
        try {
            val request = call.receive<CorrectionRequest>()
            val path = MLService.mlServiceOutputPath + "/${request.id}/${request.file}"
            File(path).copyTo(File(CorrectionService.correctionTrashFolder + "/${request.file}"))
            call.respond(HttpStatusCode.OK)
        } catch (e: ContentTransformationException) {
            call.respond(HttpStatusCode.BadRequest)
        } catch (e: FileAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: IOException) {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
