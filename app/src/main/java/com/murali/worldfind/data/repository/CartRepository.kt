package com.murali.worldfind.data.repository

import com.murali.worldfind.data.models.CartItem
import com.murali.worldfind.data.models.Product
import com.murali.worldfind.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartRepository(
    private val api: WorldFindApi = ApiClient.api
) {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    suspend fun fetchCart(): Result<CartSummaryDto> {
        return try {
            val response = api.getCart()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val summary = response.body()!!.data!!
                _cartItems.value = summary.items.map { it.toDomainCartItem() }
                Result.success(summary)
            } else {
                Result.failure(Exception("Failed to fetch cart"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addToCart(productId: String, quantity: Int = 1): Result<CartSummaryDto> {
        return try {
            val response = api.addToCart(AddToCartRequest(productId, quantity))
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val summary = response.body()!!.data!!
                _cartItems.value = summary.items.map { it.toDomainCartItem() }
                Result.success(summary)
            } else {
                Result.failure(Exception("Failed to add item to cart"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateQuantity(productId: String, quantity: Int): Result<CartSummaryDto> {
        return try {
            val response = api.updateCartQuantity(productId, UpdateCartQuantityRequest(quantity))
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val summary = response.body()!!.data!!
                _cartItems.value = summary.items.map { it.toDomainCartItem() }
                Result.success(summary)
            } else {
                Result.failure(Exception("Failed to update cart quantity"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(productId: String): Result<CartSummaryDto> {
        return try {
            val response = api.removeFromCart(productId)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val summary = response.body()!!.data!!
                _cartItems.value = summary.items.map { it.toDomainCartItem() }
                Result.success(summary)
            } else {
                Result.failure(Exception("Failed to remove item from cart"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCart(): Result<CartSummaryDto> {
        return try {
            val response = api.clearCart()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val summary = response.body()!!.data!!
                _cartItems.value = emptyList()
                Result.success(summary)
            } else {
                Result.failure(Exception("Failed to clear cart"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CartItemDto.toDomainCartItem(): CartItem {
        return CartItem(
            product = Product(
                id = product.id,
                title = product.title,
                description = product.description,
                imageUrl = product.imageUrl,
                category = product.category,
                rating = product.rating,
                reviewCount = product.reviewCount,
                deliveryEstimate = product.deliveryEstimate,
                availability = product.availability,
                brand = product.brand,
                countryId = product.countryId,
                countryName = product.countryName,
                countryCode = product.countryCode,
                currencyCode = product.currencyCode,
                currencySymbol = product.currencySymbol,
                localPrice = product.localPrice,
                priceInINR = product.priceInINR,
                originalPriceInINR = product.originalPriceInINR,
                affiliateUrl = product.affiliateUrl
            ),
            quantity = quantity
        )
    }
}
