package com.murali.worldfind.data.repository

import com.murali.worldfind.data.models.Product
import com.murali.worldfind.data.remote.*

class ProductRepository(
    private val api: WorldFindApi = ApiClient.api
) {
    suspend fun getProducts(
        keywords: String? = null,
        categoryIds: String? = null,
        minSalePrice: Double? = null,
        maxSalePrice: Double? = null,
        page: Int = 1,
        pageSize: Int = 20,
        sort: String? = null,
        targetCurrency: String = "INR",
        shipToCountry: String = "IN"
    ): Result<ProductListResponse> {
        return try {
            val response = api.getProducts(
                keywords = keywords,
                categoryIds = categoryIds,
                minSalePrice = minSalePrice,
                maxSalePrice = maxSalePrice,
                page = page,
                pageSize = pageSize,
                sort = sort,
                targetCurrency = targetCurrency,
                shipToCountry = shipToCountry
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    val errMsg = body?.error?.message ?: "Failed to fetch products"
                    Result.failure(Exception(errMsg))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductById(
        productId: String,
        shipToCountry: String = "IN",
        targetCurrency: String = "INR"
    ): Result<Product?> {
        return try {
            val response = api.getProductById(productId, shipToCountry, targetCurrency)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val p = body.data.toDomainProduct()
                    Result.success(p)
                } else {
                    Result.success(null)
                }
            } else if (response.code() == 404) {
                Result.success(null)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<CategoryDto>> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.error?.message ?: "Failed to fetch categories"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun BackendProduct.toDomainProduct(): Product {
        return Product(
            id = id,
            title = title,
            description = if (description.isNotBlank()) description else title,
            imageUrl = imageUrl,
            smallImages = smallImages,
            category = categoryName ?: category,
            categoryId = categoryId,
            rating = rating,
            reviewCount = reviewCount,
            deliveryEstimate = deliveryEstimate,
            estimatedDeliveryDays = estimatedDeliveryDays,
            availability = availability,
            brand = brand,
            countryId = countryId,
            countryName = countryName,
            countryCode = countryCode,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            localPrice = if (localPrice > 0) localPrice else priceInINR,
            priceInINR = priceInINR,
            originalPriceInINR = originalPriceInINR,
            discountPercent = discountPercent,
            shopUrl = shopUrl,
            videoUrl = videoUrl,
            affiliateUrl = affiliateUrl,
            source = source
        )
    }
}
