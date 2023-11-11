package dev.argraur.bobry.model

import kotlinx.serialization.Serializable

@Serializable
data class Stream(
    var id: String = "",
    val url: String,
    val login: String,
    val password: String,
    val lat: Double,
    val long: Double
)
