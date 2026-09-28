package com.murali.worldfind.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: String,
    val name: String,
    val phone: String,
    val addressLine: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String = "India",
    val isDefault: Boolean = false
)
