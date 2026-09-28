package com.murali.worldfind.data.mock

import com.murali.worldfind.data.models.*

object MockData {
    val categories = listOf(
        Category("1", "Electronics", "📱"),
        Category("2", "Fashion", "👕"),
        Category("3", "Home", "🏠"),
        Category("4", "Beauty", "💄"),
        Category("5", "Sports", "⚽"),
        Category("6", "Gadgets", "⌚")
    )

    val products = listOf(
        Product(
            id = "p1",
            title = "Premium Wireless Headphones",
            description = "High-quality wireless headphones with noise cancellation and 30-hour battery life. Experience studio-quality sound in a sleek, comfortable design.",
            imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80",
            category = "Electronics",
            rating = 4.5f,
            reviewCount = 120,
            countryId = "1",
            countryName = "India",
            countryCode = "IN",
            currencyCode = "INR",
            currencySymbol = "₹",
            localPrice = 4999.0,
            priceInINR = 4999.0,
            originalPriceInINR = 7999.0
        ),
        Product(
            id = "p2",
            title = "Minimalist Quartz Watch",
            description = "Elegant quartz watch with a genuine leather strap and scratch-resistant sapphire glass. A timeless accessory for the modern individual.",
            imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80",
            category = "Fashion",
            rating = 4.8f,
            reviewCount = 85,
            countryId = "2",
            countryName = "Vietnam",
            countryCode = "VN",
            currencyCode = "VND",
            currencySymbol = "₫",
            localPrice = 750000.0,
            priceInINR = 2760.0,
            originalPriceInINR = 3999.0
        ),
        Product(
            id = "p3",
            title = "Smart Fitness Tracker",
            description = "Track your steps, heart rate, and sleep with this sleek fitness band. Features a vibrant AMOLED display and week-long battery life.",
            imageUrl = "https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?auto=format&fit=crop&w=800&q=80",
            category = "Gadgets",
            rating = 4.2f,
            reviewCount = 210,
            countryId = "4",
            countryName = "Indonesia",
            countryCode = "ID",
            currencyCode = "IDR",
            currencySymbol = "Rp",
            localPrice = 350000.0,
            priceInINR = 1875.0,
            originalPriceInINR = 2999.0
        ),
        Product(
            id = "p4",
            title = "Leather Laptop Sleeve",
            description = "Protect your laptop with this premium handcrafted leather sleeve. Padded interior provides excellent protection against bumps and scratches.",
            imageUrl = "https://images.unsplash.com/photo-1512331283953-19967202267a?auto=format&fit=crop&w=800&q=80",
            category = "Fashion",
            rating = 4.7f,
            reviewCount = 45,
            countryId = "10",
            countryName = "Nepal",
            countryCode = "NP",
            currencyCode = "NPR",
            currencySymbol = "Rs",
            localPrice = 2400.0,
            priceInINR = 1500.0,
            originalPriceInINR = 1999.0
        ),
        Product(
            id = "p5",
            title = "Portable Bluetooth Speaker",
            description = "Compact speaker with powerful 360-degree sound and IPX7 water resistance. Perfect for outdoor adventures and home use.",
            imageUrl = "https://images.unsplash.com/photo-1608156639585-b3a032ef9689?auto=format&fit=crop&w=800&q=80",
            category = "Electronics",
            rating = 4.4f,
            reviewCount = 150,
            countryId = "8",
            countryName = "Sri Lanka",
            countryCode = "LK",
            currencyCode = "LKR",
            currencySymbol = "Rs",
            localPrice = 10000.0,
            priceInINR = 2898.0,
            originalPriceInINR = 4499.0
        ),
        Product(
            id = "p6",
            title = "Cotton Crew Neck T-Shirt",
            description = "Soft and breathable organic cotton t-shirt. Features a classic fit that stays comfortable all day long.",
            imageUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=800&q=80",
            category = "Fashion",
            rating = 4.6f,
            reviewCount = 300,
            countryId = "1",
            countryName = "India",
            countryCode = "IN",
            currencyCode = "INR",
            currencySymbol = "₹",
            localPrice = 799.0,
            priceInINR = 799.0,
            originalPriceInINR = 1299.0
        ),
        Product(
            id = "p7",
            title = "Silk Scarf from Hanoi",
            description = "Beautifully handcrafted 100% natural silk scarf from traditional artisans in Hanoi.",
            imageUrl = "https://images.unsplash.com/photo-1584030373081-f37b7bb4fa8e?auto=format&fit=crop&w=800&q=80",
            category = "Fashion",
            rating = 4.9f,
            reviewCount = 56,
            countryId = "2",
            countryName = "Vietnam",
            countryCode = "VN",
            currencyCode = "VND",
            currencySymbol = "₫",
            localPrice = 300000.0,
            priceInINR = 1104.0,
            originalPriceInINR = 1500.0
        ),
        Product(
            id = "p8",
            title = "Ceramic Tea Set",
            description = "Traditional ceramic tea set with intricate hand-painted designs.",
            imageUrl = "https://images.unsplash.com/photo-1576092729250-19c13731488a?auto=format&fit=crop&w=800&q=80",
            category = "Home",
            rating = 4.7f,
            reviewCount = 28,
            countryId = "11",
            countryName = "Japan",
            countryCode = "JP",
            currencyCode = "JPY",
            currencySymbol = "¥",
            localPrice = 4500.0,
            priceInINR = 2500.0,
            originalPriceInINR = 3500.0
        )
    )

    val cartItems = mutableListOf<CartItem>()

    val wishlist = mutableListOf<Product>()

    val orders = mutableListOf<Order>()

    val currentUser = User(
        id = "u1",
        fullName = "Murali Krishna",
        phoneNumber = "+91 9876543210",
        email = "murali@example.com"
    )
}
