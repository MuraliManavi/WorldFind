package com.murali.worldfind.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Country(
    val id: String,
    val name: String,
    val countryCode: String,
    val currencyCode: String,
    val currencyName: String,
    val currencySymbol: String,
    val exchangeRateFromINR: Double, // 1 INR = X local units
    val flag: String,
    val isINRAdvantage: Boolean,
    val displayOrder: Int
)
