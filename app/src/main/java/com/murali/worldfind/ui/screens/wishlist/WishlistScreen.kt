package com.murali.worldfind.ui.screens.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.ui.components.ProductCard
import com.murali.worldfind.ui.components.WorldFindButton
import com.murali.worldfind.ui.viewmodel.MainViewModel

@Composable
fun WishlistScreen(
    onProductClick: (String) -> Unit,
    onStartShoppingClick: () -> Unit,
    mainViewModel: MainViewModel
) {
    val wishlistItems by mainViewModel.wishlistItems.collectAsState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Wishlist",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        if (wishlistItems.isEmpty()) {
            EmptyWishlistState(onStartShoppingClick)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(wishlistItems) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = true,
                        onClick = { onProductClick(product.id) },
                        onFavoriteClick = {
                            mainViewModel.toggleWishlist(product)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyWishlistState(onStartShopping: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Favorite, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your wishlist is empty",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Save items you love to find them later",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        WorldFindButton(
            text = "START SHOPPING",
            onClick = onStartShopping,
            modifier = Modifier.width(200.dp)
        )
    }
}
