package com.murali.worldfind.data.repository

import com.murali.worldfind.data.models.CartItem
import com.murali.worldfind.data.models.Order
import com.murali.worldfind.data.models.OrderStatus
import com.murali.worldfind.data.models.Product
import com.murali.worldfind.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderRepository(
    private val api: WorldFindApi = ApiClient.api
) {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    suspend fun createOrder(
        addressId: String? = null,
        items: List<CheckoutItemReq>? = null,
        paymentMethod: String,
        paymentId: String? = null,
        razorpayOrderId: String? = null,
        razorpaySignature: String? = null
    ): Result<Order> {
        return try {
            val response = api.createOrder(
                CreateOrderRequestDto(
                    addressId = addressId,
                    items = items,
                    paymentMethod = paymentMethod,
                    paymentId = paymentId,
                    razorpayOrderId = razorpayOrderId,
                    razorpaySignature = razorpaySignature
                )
            )

            if (response.isSuccessful && response.body() != null && response.body()!!.success && response.body()!!.data != null) {
                val created = response.body()!!.data!!.toDomainOrder()
                fetchOrders()
                Result.success(created)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to create order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchOrders(): Result<List<Order>> {
        return try {
            val response = api.getOrders()
            if (response.isSuccessful && response.body() != null && response.body()!!.success && response.body()!!.data != null) {
                val ordersList = response.body()!!.data!!.map { it.toDomainOrder() }
                _orders.value = ordersList
                Result.success(ordersList)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to fetch orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val response = api.getOrderById(orderId)
            if (response.isSuccessful && response.body() != null && response.body()!!.success && response.body()!!.data != null) {
                Result.success(response.body()!!.data!!.toDomainOrder())
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to fetch order details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelOrder(orderId: String): Result<Order> {
        return try {
            val response = api.cancelOrder(orderId)
            if (response.isSuccessful && response.body() != null && response.body()!!.success && response.body()!!.data != null) {
                val cancelled = response.body()!!.data!!.toDomainOrder()
                fetchOrders()
                Result.success(cancelled)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to cancel order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun OrderDto.toDomainOrder(): Order {
        val mappedStatus = when (orderStatus.uppercase()) {
            "CONFIRMED", "PROCESSING" -> OrderStatus.CONFIRMED
            "SHIPPED" -> OrderStatus.SHIPPED
            "DELIVERED" -> OrderStatus.DELIVERED
            "CANCELLED" -> OrderStatus.CANCELLED
            else -> OrderStatus.PENDING
        }

        return Order(
            id = id,
            timestamp = createdAt,
            items = items.map { it.toDomainCartItem() },
            totalAmount = totalAmount,
            status = mappedStatus,
            deliveryAddress = deliveryAddressFormatted,
            paymentMethod = paymentMethod
        )
    }

    private fun CartItemDto.toDomainCartItem(): CartItem {
        return CartItem(
            product = Product(
                id = product.id,
                title = product.title,
                description = product.description,
                imageUrl = product.imageUrl,
                category = product.categoryName ?: product.category?.name ?: "All",
                rating = product.rating ?: 0f,
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
