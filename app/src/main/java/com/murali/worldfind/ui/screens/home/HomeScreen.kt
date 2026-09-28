package com.murali.worldfind.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.murali.worldfind.data.models.Product
import com.murali.worldfind.data.remote.CategoryDto
import com.murali.worldfind.ui.components.ProductCard
import com.murali.worldfind.ui.components.WorldFindSearchBar
import com.murali.worldfind.ui.components.WorldFindTopBar
import com.murali.worldfind.ui.state.UiState
import com.murali.worldfind.ui.viewmodel.AuthViewModel
import com.murali.worldfind.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onNotificationClick: () -> Unit,
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val wishlistItems by mainViewModel.wishlistItems.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val homeProductsState by mainViewModel.homeProductsState.collectAsState()
    val categoriesState by mainViewModel.categoriesState.collectAsState()

    val displayName = currentUser?.displayName ?: "Guest"

    val defaultCategoryList = remember {
        listOf(
            CategoryDto("1", "Electronics", "📱"),
            CategoryDto("2", "Fashion", "👕"),
            CategoryDto("3", "Home", "🏠"),
            CategoryDto("4", "Beauty", "💄"),
            CategoryDto("5", "Sports", "⚽"),
            CategoryDto("6", "Gadgets", "⌚")
        )
    }

    val categories: List<CategoryDto> = when (val cState = categoriesState) {
        is UiState.Success -> if (cState.data.isNotEmpty()) cState.data else defaultCategoryList
        else -> defaultCategoryList
    }

    Scaffold(
        topBar = {
            WorldFindTopBar(
                showLogo = true,
                showNotification = true,
                onNotificationClick = onNotificationClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hello, ${displayName.split(" ").firstOrNull() ?: "Guest"}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )

            Text(
                text = "Discover Worldwide",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            WorldFindSearchBar(
                query = searchQuery,
                onQueryChange = { 
                    searchQuery = it 
                    if (it.length > 2) {
                        mainViewModel.searchExploreProducts(keywords = it)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = homeProductsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { mainViewModel.loadHomeProducts() }) {
                                Text("RETRY")
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    val products: List<Product> = if (searchQuery.isEmpty()) {
                        state.data
                    } else {
                        state.data.filter { product ->
                            val query = searchQuery.trim().lowercase()
                            product.title.lowercase().contains(query) ||
                            product.category.lowercase().contains(query) ||
                            product.countryName.lowercase().contains(query) ||
                            product.countryCode.lowercase().contains(query) ||
                            product.currencyCode.lowercase().contains(query) ||
                            product.description.lowercase().contains(query) ||
                            (product.brand?.lowercase()?.contains(query) ?: false)
                        }
                    }

                    if (products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No products found for \"$searchQuery\"" else "No products available",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Categories",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(categories) { category ->
                                    CategoryItem(category.name, category.icon ?: "📦") {
                                        onCategoryClick(category.id)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Text(
                            text = if (searchQuery.isEmpty()) "Featured Products" else "Search Results",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        products.chunked(2).forEach { rowProducts ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowProducts.forEach { product ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProductCard(
                                            product = product,
                                            isFavorite = wishlistItems.any { it.id == product.id },
                                            onClick = { onProductClick(product.id) },
                                            onFavoriteClick = { mainViewModel.toggleWishlist(product) }
                                        )
                                    }
                                }
                                if (rowProducts.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (searchQuery.isEmpty() && products.size >= 3) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Recommended for you",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            products.take(3).forEach { product ->
                                RecommendedProductItem(
                                    title = product.title,
                                    price = "₹${String.format("%.0f", product.priceInINR)}",
                                    rating = if (product.rating > 0) product.rating.toString() else "N/A",
                                    onClick = { onProductClick(product.id) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CategoryItem(name: String, icon: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(vertical = 4.dp).clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
    }
}

@Composable
fun RecommendedProductItem(
    title: String,
    price: String,
    rating: String,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⭐ $rating", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = price, 
                        color = MaterialTheme.colorScheme.onSurface, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
