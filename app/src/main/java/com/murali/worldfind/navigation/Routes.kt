package com.murali.worldfind.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")
    data object Home : Screen("home")
    data object Explore : Screen("explore")
    data object Cart : Screen("cart")
    data object Profile : Screen("profile")
    data object ProductDetails : Screen("product_details/{productId}") {
        fun createRoute(productId: String) = "product_details/$productId"
    }
    data object Wishlist : Screen("wishlist")
    data object Checkout : Screen("checkout")
    data object Orders : Screen("orders")
    data object OrderDetails : Screen("order_details/{orderId}") {
        fun createRoute(orderId: String) = "order_details/$orderId"
    }
    data object Settings : Screen("settings")
    data object Addresses : Screen("addresses")
    data object PaymentMethods : Screen("payment_methods")
    data object HelpSupport : Screen("help_support")
    data object EditProfile : Screen("edit_profile")
    data object Notifications : Screen("notifications")
    data object AddAddress : Screen("add_address")
    data object EditAddress : Screen("edit_address/{addressId}") {
        fun createRoute(addressId: String) = "edit_address/$addressId"
    }
}
