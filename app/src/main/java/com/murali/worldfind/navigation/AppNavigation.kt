package com.murali.worldfind.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.murali.worldfind.data.local.PreferenceManager
import com.murali.worldfind.ui.components.WorldFindBottomBar
import com.murali.worldfind.ui.screens.*
import com.murali.worldfind.ui.screens.address.AddEditAddressScreen
import com.murali.worldfind.ui.screens.address.AddressesScreen
import com.murali.worldfind.ui.screens.auth.ForgotPasswordScreen
import com.murali.worldfind.ui.screens.auth.LoginScreen
import com.murali.worldfind.ui.screens.auth.RegisterScreen
import com.murali.worldfind.ui.screens.cart.CartScreen
import com.murali.worldfind.ui.screens.checkout.CheckoutScreen
import com.murali.worldfind.ui.screens.explore.ExploreScreen
import com.murali.worldfind.ui.screens.home.HomeScreen
import com.murali.worldfind.ui.screens.notifications.NotificationsScreen
import com.murali.worldfind.ui.screens.onboarding.OnboardingScreen
import com.murali.worldfind.ui.screens.orders.OrderDetailsScreen
import com.murali.worldfind.ui.screens.orders.OrdersScreen
import com.murali.worldfind.ui.screens.payment.PaymentMethodsScreen
import com.murali.worldfind.ui.screens.product.ProductDetailsScreen
import com.murali.worldfind.ui.screens.profile.ProfileScreen
import com.murali.worldfind.ui.screens.profile.edit.EditProfileScreen
import com.murali.worldfind.ui.screens.settings.SettingsScreen
import com.murali.worldfind.ui.screens.support.HelpSupportScreen
import com.murali.worldfind.ui.screens.wishlist.WishlistScreen
import com.murali.worldfind.ui.viewmodel.AuthViewModel
import com.murali.worldfind.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(preferenceManager = PreferenceManager(context)) as T
            }
        }
    )

    val preferenceManager = remember { PreferenceManager(context) }
    val isOnboardingCompleted by preferenceManager.isOnboardingCompleted.collectAsState(initial = null)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUser by authViewModel.currentUser.collectAsState()

    val bottomBarRoutes = setOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.Cart.route,
        Screen.Profile.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                WorldFindBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onFinished = {
                        val destination = when {
                            currentUser != null -> Screen.Home.route
                            isOnboardingCompleted == false -> Screen.Onboarding.route
                            else -> Screen.Home.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                val scope = rememberCoroutineScope()
                OnboardingScreen(
                    onGetStarted = {
                        scope.launch {
                            preferenceManager.setOnboardingCompleted(true)
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Screen.Register.route)
                    },
                    onForgotPasswordClick = {
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                    authViewModel = authViewModel
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onLoginClick = {
                        navController.popBackStack()
                    },
                    authViewModel = authViewModel
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onBackToLoginClick = {
                        navController.popBackStack()
                    },
                    authViewModel = authViewModel
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetails.createRoute(productId))
                    },
                    onCategoryClick = { category ->
                        mainViewModel.setExploreCategoryFilter(category)
                        navController.navigate(Screen.Explore.route) {
                            launchSingleTop = true
                        }
                    },
                    onNotificationClick = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    mainViewModel = mainViewModel,
                    authViewModel = authViewModel
                )
            }

            composable(Screen.Explore.route) {
                ExploreScreen(
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetails.createRoute(productId))
                    },
                    mainViewModel = mainViewModel
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onCheckoutClick = {
                        mainViewModel.setDirectCheckout(null)
                        navController.navigate(Screen.Checkout.route)
                    },
                    onContinueShoppingClick = {
                        navController.navigate(Screen.Explore.route) {
                            popUpTo(Screen.Home.route)
                            launchSingleTop = true
                        }
                    },
                    mainViewModel = mainViewModel
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onOrdersClick = {
                        navController.navigate(Screen.Orders.route)
                    },
                    onWishlistClick = {
                        navController.navigate(Screen.Wishlist.route)
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onAddressesClick = {
                        navController.navigate(Screen.Addresses.route)
                    },
                    onPaymentMethodsClick = {
                        navController.navigate(Screen.PaymentMethods.route)
                    },
                    onHelpSupportClick = {
                        navController.navigate(Screen.HelpSupport.route)
                    },
                    onEditProfileClick = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    authViewModel = authViewModel
                )
            }

            composable(
                route = Screen.ProductDetails.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailsScreen(
                    productId = productId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onBuyNowClick = { product ->
                        mainViewModel.setDirectCheckout(product)
                        navController.navigate(Screen.Checkout.route)
                    },
                    mainViewModel = mainViewModel
                )
            }

            composable(Screen.Wishlist.route) {
                WishlistScreen(
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetails.createRoute(productId))
                    },
                    onStartShoppingClick = {
                        navController.navigate(Screen.Explore.route) {
                            popUpTo(Screen.Home.route)
                            launchSingleTop = true
                        }
                    },
                    mainViewModel = mainViewModel
                )
            }

            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    onBackClick = {
                        mainViewModel.setDirectCheckout(null)
                        navController.popBackStack()
                    },
                    onPlaceOrder = { orderId ->
                        navController.navigate(Screen.OrderDetails.createRoute(orderId)) {
                            popUpTo(Screen.Checkout.route) { inclusive = true }
                        }
                    },
                    onAddAddressClick = {
                        navController.navigate(Screen.Addresses.route)
                    },
                    mainViewModel = mainViewModel
                )
            }

            composable(Screen.Orders.route) {
                OrdersScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onOrderClick = { orderId ->
                        navController.navigate(Screen.OrderDetails.createRoute(orderId))
                    },
                    onDiscoverClick = {
                        navController.navigate(Screen.Explore.route) {
                            popUpTo(Screen.Home.route)
                            launchSingleTop = true
                        }
                    },
                    mainViewModel = mainViewModel
                )
            }

            composable(
                route = Screen.OrderDetails.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                OrderDetailsScreen(
                    orderId = orderId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    mainViewModel = mainViewModel
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Addresses.route) {
                AddressesScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddAddressClick = { navController.navigate(Screen.AddAddress.route) },
                    onEditAddressClick = { id -> navController.navigate(Screen.EditAddress.createRoute(id)) },
                    onAddressSelected = { address ->
                        navController.popBackStack()
                    },
                    mainViewModel = mainViewModel
                )
            }

            composable(Screen.AddAddress.route) {
                AddEditAddressScreen(
                    onBackClick = { navController.popBackStack() },
                    mainViewModel = mainViewModel
                )
            }

            composable(
                route = Screen.EditAddress.route,
                arguments = listOf(navArgument("addressId") { type = NavType.StringType })
            ) { backStackEntry ->
                val addressId = backStackEntry.arguments?.getString("addressId") ?: ""
                AddEditAddressScreen(
                    addressId = addressId,
                    onBackClick = { navController.popBackStack() },
                    mainViewModel = mainViewModel
                )
            }

            composable(Screen.PaymentMethods.route) {
                PaymentMethodsScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Screen.HelpSupport.route) {
                HelpSupportScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    authViewModel = authViewModel
                )
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}
