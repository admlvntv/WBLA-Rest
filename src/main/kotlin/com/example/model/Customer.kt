package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val customerName: String,
    val customerId: String
)
