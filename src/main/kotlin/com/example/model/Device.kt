package com.example.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Device (
    val deviceName: String,
    val deviceId: String
)

