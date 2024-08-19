package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomerLicense(
    val customerId: String,
    val licenses: List<License>
)
