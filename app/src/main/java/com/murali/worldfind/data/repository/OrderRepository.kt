package com.murali.worldfind.data.repository

import com.murali.worldfind.data.models.*
import com.murali.worldfind.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderRepository(
    private val api: WorldFindApi = ApiClient.api
) {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    suspend fun fetchOrders(): Result<List<Order>> {
        return try {
            val response = api.getOrders()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomainOrder() }
                _orders.value = list
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to fetch orders"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrder(
        addressId: String? = null,
        items: List<CheckoutItemReq>? = null,
        paymentMethod: String,
        paymentId: String? = null,
        razorpayOrderId: String? = null,
        razorpaySignature: String? = null
    ): Result<Order> {
        return try {
            val req = CreateOrderRequestDto(
                addressId = addressId,
                items = items,
                paymentMethod = paymentMethod,
                paymentId = paymentId,
                razorpayOrderId = razorpayOrderId,
                razorpaySignature = razorpaySignature
            )
            val response = api.createOrder(req)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val order = response.body()!!.data!!.toDomainOrder()
                fetchOrders()
                Result.success(order)
            } else {
                Result.failure(Exception("Failed to create order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderById(orderId: String): Result<Order?> {
        return try {
            val response = api.getOrderById(orderId)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.toDomainOrder())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelOrder(orderId: String): Result<Order> {
        return try {
            val response = api.cancelOrder(orderId)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val updated = response.body()!!.data!!.toDomainOrder()
                fetchOrders()
                Result.success(updated)
            } else {
                Result.failure(Exception("Failed to cancel order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun previewCheckout(
        addressId: String? = null,
        items: List<CheckoutItemReq>? = null
    ): Result<CheckoutPreviewResponseDto> {
        return try {
            val req = CheckoutPreviewRequestDto(addressId = addressId, items = items)
            val response = api.previewCheckout(req)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to preview checkout"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRazorpayOrder(amount: Double): Result<RazorpayOrderDto> {
        return try {
            val response = api.createRazorpayOrder(RazorpayOrderRequestDto(amount = amount))
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to create Razorpay order"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun OrderDto.toDomainOrder(): Order {
        val mappedStatus = try {
            OrderStatus.valueOf(orderStatus)
        } catch (e: Exception) {
            OrderStatus.PENDING
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
