package com.murali.worldfind.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorDetail? = null
)

@Serializable
data class ApiErrorDetail(
    val code: String? = null,
    val message: String? = null
)

@Serializable
data class PaginationDto(
    val currentPage: Int = 1,
    val pageSize: Int = 20,
    val currentRecordCount: Int = 0,
    val totalPages: Int = 1,
    val totalCount: Int = 0,
    val hasNextPage: Boolean = false
)

@Serializable
data class ProductListResponse(
    val products: List<BackendProduct> = emptyList(),
    val pagination: PaginationDto? = null
)

@Serializable
data class BackendProduct(
    val id: String,
    val title: String,
    val description: String = "",
    val imageUrl: String = "",
    val smallImages: List<String> = emptyList(),
    val category: String = "All",
    val categoryId: String? = null,
    val categoryName: String? = null,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val deliveryEstimate: String = "Estimated delivery unavailable",
    val estimatedDeliveryDays: Int? = null,
    val availability: Boolean = true,
    val brand: String? = null,
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
)

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val icon: String? = null
)

@Serializable
data class AffiliateLinkRequest(
    val urls: List<String>,
    val promotionLinkType: Int = 0,
    val trackingId: String? = null
)

@Serializable
data class AffiliateLinkDto(
    val promotionUrl: String,
    val sourceUrl: String
)

@Serializable
data class CartItemDto(
    val id: String,
    val productId: String,
    val quantity: Int,
    val product: BackendProduct,
    val addedAt: Long = 0L
)

@Serializable
data class CartSummaryDto(
    val userId: String = "",
    val items: List<CartItemDto> = emptyList(),
    val itemCount: Int = 0,
    val subtotal: Double = 0.0,
    val shipping: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val currency: String = "INR"
)

@Serializable
data class AddToCartRequest(
    val productId: String,
    val quantity: Int = 1
)

@Serializable
data class UpdateCartQuantityRequest(
    val quantity: Int
)

@Serializable
data class WishlistItemDto(
    val productId: String,
    val addedAt: Long = 0L,
    val product: BackendProduct
)

@Serializable
data class WishlistSummaryDto(
    val userId: String = "",
    val items: List<WishlistItemDto> = emptyList()
)

@Serializable
data class AddressDto(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val addressLine: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = "India",
    val isDefault: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class CheckoutItemReq(
    val productId: String,
    val quantity: Int
)

@Serializable
data class CheckoutPreviewRequestDto(
    val addressId: String? = null,
    val items: List<CheckoutItemReq>? = null,
    val promoCode: String? = null
)

@Serializable
data class CheckoutPreviewResponseDto(
    val valid: Boolean = false,
    val items: List<CartItemDto> = emptyList(),
    val address: AddressDto? = null,
    val subtotal: Double = 0.0,
    val shipping: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val currency: String = "INR",
    val allowedPaymentMethods: List<String> = emptyList()
)

@Serializable
data class RazorpayOrderRequestDto(
    val amount: Double,
    val receipt: String? = null
)

@Serializable
data class RazorpayOrderDto(
    val id: String,
    val entity: String = "order",
    val amount: Long,
    val amount_paid: Long = 0L,
    val amount_due: Long,
    val currency: String = "INR",
    val receipt: String = "",
    val status: String = "created",
    val created_at: Long = 0L
)

@Serializable
data class RazorpayVerifyRequestDto(
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val razorpaySignature: String
)

@Serializable
data class CreateOrderRequestDto(
    val addressId: String? = null,
    val items: List<CheckoutItemReq>? = null,
    val paymentMethod: String,
    val paymentId: String? = null,
    val razorpayOrderId: String? = null,
    val razorpaySignature: String? = null
)

@Serializable
data class OrderDto(
    val id: String,
    val orderNumber: String = "",
    val userId: String = "",
    val items: List<CartItemDto> = emptyList(),
    val subtotal: Double = 0.0,
    val shipping: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val totalAmount: Double = 0.0,
    val currency: String = "INR",
    val paymentMethod: String = "COD",
    val paymentStatus: String = "PENDING",
    val orderStatus: String = "PENDING",
    val deliveryAddressFormatted: String = "",
    val trackingNumber: String? = null,
    val courierName: String? = null,
    val estimatedDelivery: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
