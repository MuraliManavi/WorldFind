package com.murali.worldfind.data.remote

import retrofit2.Response
import retrofit2.http.*

interface WorldFindApi {

    // Products & Categories
    @GET("api/products")
    suspend fun getProducts(
        @Query("keywords") keywords: String? = null,
        @Query("categoryIds") categoryIds: String? = null,
        @Query("minSalePrice") minSalePrice: Double? = null,
        @Query("maxSalePrice") maxSalePrice: Double? = null,
        @Query("page") page: Int? = 1,
        @Query("pageSize") pageSize: Int? = 20,
        @Query("sort") sort: String? = null,
        @Query("targetCurrency") targetCurrency: String? = "INR",
        @Query("targetLanguage") targetLanguage: String? = "EN",
        @Query("shipToCountry") shipToCountry: String? = "IN"
    ): Response<ApiResponse<ProductListResponse>>

    @GET("api/products/{productId}")
    suspend fun getProductById(
        @Path("productId") productId: String,
        @Query("shipToCountry") shipToCountry: String? = "IN",
        @Query("targetCurrency") targetCurrency: String? = "INR"
    ): Response<ApiResponse<BackendProduct>>

    @GET("api/categories")
    suspend fun getCategories(
        @Query("categoryId") categoryId: String? = null
    ): Response<ApiResponse<List<CategoryDto>>>

    @POST("api/products/affiliate-link")
    suspend fun generateAffiliateLinks(
        @Body request: AffiliateLinkRequest
    ): Response<ApiResponse<List<AffiliateLinkDto>>>

    // Cart API
    @GET("api/cart")
    suspend fun getCart(): Response<ApiResponse<CartSummaryDto>>

    @POST("api/cart/items")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<ApiResponse<CartSummaryDto>>

    @PATCH("api/cart/items/{productId}")
    suspend fun updateCartQuantity(
        @Path("productId") productId: String,
        @Body request: UpdateCartQuantityRequest
    ): Response<ApiResponse<CartSummaryDto>>

    @DELETE("api/cart/items/{productId}")
    suspend fun removeFromCart(@Path("productId") productId: String): Response<ApiResponse<CartSummaryDto>>

    @DELETE("api/cart")
    suspend fun clearCart(): Response<ApiResponse<CartSummaryDto>>

    // Wishlist API
    @GET("api/wishlist")
    suspend fun getWishlist(): Response<ApiResponse<WishlistSummaryDto>>

    @POST("api/wishlist/{productId}")
    suspend fun toggleWishlist(@Path("productId") productId: String): Response<ApiResponse<WishlistSummaryDto>>

    @DELETE("api/wishlist/{productId}")
    suspend fun removeFromWishlist(@Path("productId") productId: String): Response<ApiResponse<WishlistSummaryDto>>

    // Address API
    @GET("api/addresses")
    suspend fun getAddresses(): Response<ApiResponse<List<AddressDto>>>

    @POST("api/addresses")
    suspend fun addAddress(@Body address: AddressDto): Response<ApiResponse<AddressDto>>

    @PATCH("api/addresses/{addressId}")
    suspend fun updateAddress(
        @Path("addressId") addressId: String,
        @Body address: AddressDto
    ): Response<ApiResponse<AddressDto>>

    @DELETE("api/addresses/{addressId}")
    suspend fun deleteAddress(@Path("addressId") addressId: String): Response<ApiResponse<Unit>>

    @PATCH("api/addresses/{addressId}/default")
    suspend fun setDefaultAddress(@Path("addressId") addressId: String): Response<ApiResponse<List<AddressDto>>>

    // Checkout & Payment API
    @POST("api/checkout/preview")
    suspend fun previewCheckout(@Body request: CheckoutPreviewRequestDto): Response<ApiResponse<CheckoutPreviewResponseDto>>

    @POST("api/payments/razorpay/create-order")
    suspend fun createRazorpayOrder(@Body request: RazorpayOrderRequestDto): Response<ApiResponse<RazorpayOrderDto>>

    @POST("api/payments/razorpay/verify")
    suspend fun verifyRazorpayPayment(@Body request: RazorpayVerifyRequestDto): Response<ApiResponse<Unit>>

    // Order API
    @POST("api/orders")
    suspend fun createOrder(@Body request: CreateOrderRequestDto): Response<ApiResponse<OrderDto>>

    @GET("api/orders")
    suspend fun getOrders(): Response<ApiResponse<List<OrderDto>>>

    @GET("api/orders/{orderId}")
    suspend fun getOrderById(@Path("orderId") orderId: String): Response<ApiResponse<OrderDto>>

    @POST("api/orders/{orderId}/cancel")
    suspend fun cancelOrder(@Path("orderId") orderId: String): Response<ApiResponse<OrderDto>>
}
