package com.murali.worldfind.data.repository

import com.murali.worldfind.data.models.Product
import com.murali.worldfind.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WishlistRepository(
    private val api: WorldFindApi = ApiClient.api
) {
    private val _wishlistItems = MutableStateFlow<List<Product>>(emptyList())
    val wishlistItems: StateFlow<List<Product>> = _wishlistItems.asStateFlow()

    suspend fun fetchWishlist(): Result<WishlistSummaryDto> {
        return try {
            val response = api.getWishlist()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val summary = response.body()!!.data!!
                _wishlistItems.value = summary.items.map { it.product.toDomainProduct() }
                Result.success(summary)
            } else {
                Result.failure(Exception("Failed to fetch wishlist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleWishlist(productId: String): Result<WishlistSummaryDto> {
        return try {
            val response = api.toggleWishlist(productId)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val summary = response.body()!!.data!!
                _wishlistItems.value = summary.items.map { it.product.toDomainProduct() }
                Result.success(summary)
            } else {
                Result.failure(Exception("Failed to toggle wishlist item"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromWishlist(productId: String): Result<WishlistSummaryDto> {
        return try {
            val response = api.removeFromWishlist(productId)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val summary = response.body()!!.data!!
                _wishlistItems.value = summary.items.map { it.product.toDomainProduct() }
                Result.success(summary)
            } else {
                Result.failure(Exception("Failed to remove item from wishlist"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isInWishlist(productId: String): Boolean {
        return _wishlistItems.value.any { it.id == productId }
    }

    private fun BackendProduct.toDomainProduct(): Product {
        return Product(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl,
            category = category,
            rating = rating,
            reviewCount = reviewCount,
            deliveryEstimate = deliveryEstimate,
            availability = availability,
            brand = brand,
            countryId = countryId,
            countryName = countryName,
            countryCode = countryCode,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            localPrice = localPrice,
            priceInINR = priceInINR,
            originalPriceInINR = originalPriceInINR,
            affiliateUrl = affiliateUrl
        )
    }
}
