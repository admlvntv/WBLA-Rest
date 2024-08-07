package com.example.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class License(
    val id: String,
    val expiration: Instant,
    val used: Boolean
)
