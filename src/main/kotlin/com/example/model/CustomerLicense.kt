package com.example.model

data class CustomerLicense(
    val customerId: String,
    val licenses: List<License>
)
