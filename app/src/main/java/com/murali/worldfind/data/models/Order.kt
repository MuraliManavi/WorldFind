package com.murali.worldfind.data.models

import kotlinx.serialization.Serializable

enum class OrderStatus {
    PENDING, CONFIRMED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
}

@Serializable
data class Order(
    val id: String,
    val timestamp: Long,
    val items: List<CartItem>,
    val totalAmount: Double,
    val status: OrderStatus,
    val deliveryAddress: String,
    val paymentMethod: String
)
