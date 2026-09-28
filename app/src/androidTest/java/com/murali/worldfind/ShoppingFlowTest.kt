package com.murali.worldfind

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShoppingFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAddToCartAndCheckoutFlow() {
        // Wait for splash and skip onboarding
        composeTestRule.mainClock.advanceTimeBy(4000)
        try {
            composeTestRule.onNodeWithText("Skip").performClick()
        } catch (e: Exception) {}

        // Mock Login (assuming we can bypass auth for test or use a test account)
        // If auth is required, we'd need to handle it. 
        // For Phase 1, we can assume the user starts on Home if already "logged in" in mock state
        
        // Wait for Home
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("Discover Worldwide").fetchSemanticsNodes().isNotEmpty()
        }

        // Click on a product (Premium Wireless Headphones)
        composeTestRule.onNodeWithText("Premium Wireless Headphones").performClick()

        // Wait for Product Details
        composeTestRule.onNodeWithText("ADD TO CART").assertExists()

        // Add to cart
        composeTestRule.onNodeWithText("ADD TO CART").performClick()

        // Go to Cart (either via bottom nav or auto-navigation if implemented)
        composeTestRule.onNodeWithText("Cart").performClick()

        // Verify item in cart
        composeTestRule.onNodeWithText("My Cart").assertExists()
        composeTestRule.onNodeWithText("Premium Wireless Headphones").assertExists()

        // Proceed to checkout
        composeTestRule.onNodeWithText("PROCEED TO CHECKOUT").performClick()

        // Verify Checkout screen
        composeTestRule.onNodeWithText("Checkout").assertExists()
        
        // Check if "PLACE ORDER" button is visible
        composeTestRule.onNodeWithText("PLACE ORDER").assertExists()
    }
}
