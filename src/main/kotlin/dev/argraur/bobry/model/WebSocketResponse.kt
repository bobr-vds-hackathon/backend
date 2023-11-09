package dev.argraur.bobry.model

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketResponse(
    val status: String = "",
    val data: String = ""
)
