package com.murali.worldfind.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        title = "Home",
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        route = Screen.Explore.route,
        title = "Explore",
        icon = Icons.Default.Search
    ),
    BottomNavItem(
        route = Screen.Cart.route,
        title = "Cart",
        icon = Icons.Default.ShoppingCart
    ),
    BottomNavItem(
        route = Screen.Profile.route,
        title = "Profile",
        icon = Icons.Default.Person
    )
)