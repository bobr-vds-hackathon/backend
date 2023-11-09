package dev.argraur.bobry.model

import kotlinx.serialization.Serializable

@Serializable
data class MLMessage(
    val id: String,
    val file: String,
    val timestamp: String
)
