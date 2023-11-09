package dev.argraur.bobry.model

import kotlinx.serialization.Serializable

@Serializable
data class WebSocketCommand(
    val command: String,
    val args: Map<String, String> = mapOf()
)
