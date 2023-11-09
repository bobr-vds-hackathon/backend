package dev.argraur.bobry.controllers.utils

import dev.argraur.bobry.controllers.ApiController
import dev.argraur.bobry.controllers.annotations.HttpGet
import dev.argraur.bobry.controllers.annotations.HttpPost
import dev.argraur.bobry.controllers.annotations.WebSocket
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.koin.ktor.ext.getKoin
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.functions

fun Routing.routeControllers() {
    lazy { getKoin().getAll<ApiController>() }.value.forEach { route ->
        route::class.functions.forEach { func ->
            func.annotations.forEach {
                when (it) {
                    is HttpGet -> get(route.route + it.route) { func.callSuspend(route, this) }
                    is HttpPost -> post(route.route + it.route) { func.callSuspend(route, this) }
                    is WebSocket -> webSocket(route.route + it.route) { func.callSuspend(route, this) }
                }
            }
        }
    }
}
