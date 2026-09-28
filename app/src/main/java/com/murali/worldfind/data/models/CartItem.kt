package com.murali.worldfind.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val product: Product,
    val quantity: Int = 1
)
