package com.murali.worldfind.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val title: String,
    val description: String = "",
    val imageUrl: String = "",
    val smallImages: List<String> = emptyList(),
    val category: String = "All",
    val categoryId: String? = null,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val deliveryEstimate: String = "Estimated delivery unavailable",
    val estimatedDeliveryDays: Int? = null,
    val availability: Boolean = true,
    val brand: String? = null,
    
    // Country and Currency Info
    val countryId: String = "IN",
    val countryName: String = "India",
    val countryCode: String = "IN",
    val currencyCode: String = "INR",
    val currencySymbol: String = "₹",
    val localPrice: Double = 0.0,
    val priceInINR: Double = 0.0,
    val originalPriceInINR: Double? = null,
    val discountPercent: Int? = null,
    
    val shopUrl: String? = null,
    val videoUrl: String? = null,
    val affiliateUrl: String? = null,
    val source: String = "aliexpress"
) {
    val discount: Int?
        get() = discountPercent ?: if (originalPriceInINR != null && originalPriceInINR > priceInINR) {
            (((originalPriceInINR - priceInINR) / originalPriceInINR) * 100).toInt()
        } else null

    // Compatibility for UI using 'price' and 'currency'
    val price: Double get() = priceInINR
    val currency: String get() = "INR"
}
