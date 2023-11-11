package dev.argraur.bobry.controllers.utils

import dev.argraur.bobry.controllers.ApiController
import dev.argraur.bobry.controllers.annotations.HttpGet
import dev.argraur.bobry.controllers.annotations.HttpPost
import dev.argraur.bobry.controllers.annotations.WebSocket
import dev.argraur.bobry.utils.LoggerDelegate
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.getKoin
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions

fun Routing.routeControllers() {
    val logger by LoggerDelegate("Routing.routeControllers")
    logger.info("Routing controllers")
    lazy { getKoin().getAll<ApiController>() }.value.forEach { controller ->
        val route = controller.route
        logger.info("${controller::class.simpleName} -> $route")
        controller::class.functions.forEach { func ->
            if (func.annotations.size > 1) {
                logger.error("${controller::class.simpleName}.${func.name} has more than 1 annotation! Skipping")
            } else func.annotations.forEach { annotation ->
                when (annotation) {
                    is HttpGet -> {
                        logger.info("    ${func.name} -> GET ${route + annotation.route}")
                        get(route + annotation.route) { func.callSuspend(controller, this) }
                    }
                    is HttpPost -> {
                        logger.info("    ${func.name} -> POST ${route + annotation.route}")
                        post(route + annotation.route) { func.callSuspend(controller, this) }
                    }
                    is WebSocket -> {
                        logger.info("    ${func.name} -> WS ${route + annotation.route}")
                        webSocket(route + annotation.route) { func.callSuspend(controller, this) }
                    }
                }
            }
        }
    }
}
