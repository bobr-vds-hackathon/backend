package dev.argraur.bobry.utils

import io.ktor.server.application.*
import io.ktor.util.pipeline.*

typealias HttpContext = PipelineContext<*, ApplicationCall>
