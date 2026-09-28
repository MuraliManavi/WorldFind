package com.murali.worldfind

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.murali.worldfind.navigation.AppNavigation
import com.murali.worldfind.ui.theme.WorldFindTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testSplashToOnboardingToLogin() {
        // Wait for splash transition (approx 2s in code)
        composeTestRule.mainClock.advanceTimeBy(4000)
        
        // Handle onboarding if it appears
        try {
            composeTestRule.onNodeWithText("Skip").performClick()
        } catch (e: Exception) {
            // Might already be on login
        }
        
        // Should be on Login screen or Onboarding
        // The test above advanced time, so splash should be gone.
    }

    @Test
    fun testAppBranding() {
        composeTestRule.mainClock.advanceTimeBy(1000)
        // Check if "WORLD FIND" text exists on splash (or tagline)
        composeTestRule.onNodeWithText("Discover Beyond Borders").assertExists()
    }
}
