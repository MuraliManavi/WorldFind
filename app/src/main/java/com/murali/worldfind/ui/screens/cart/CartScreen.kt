package com.murali.worldfind.ui.screens.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.murali.worldfind.data.models.CartItem
import com.murali.worldfind.ui.components.WorldFindButton
import com.murali.worldfind.ui.viewmodel.MainViewModel

@Composable
fun CartScreen(
    onCheckoutClick: () -> Unit,
    onContinueShoppingClick: () -> Unit,
    mainViewModel: MainViewModel
) {
    val cartItems by mainViewModel.cartItems.collectAsState()
    val subtotal = cartItems.sumOf { it.product.price * it.quantity }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Cart",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                CartSummary(
                    subtotal = subtotal,
                    onCheckoutClick = onCheckoutClick
                )
            }
        }
    ) { padding ->
        if (cartItems.isEmpty()) {
            EmptyCartState(onContinueShoppingClick)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(cartItems) { item ->
                    CartItemRow(
                        item = item,
                        onQuantityChange = { newQty ->
                            mainViewModel.updateCartQuantity(item.product.id, newQty)
                        },
                        onRemove = {
                            mainViewModel.removeFromCart(item.product.id)
                        }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant, 
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = item.product.title,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.title, 
                color = MaterialTheme.colorScheme.onSurface, 
                fontWeight = FontWeight.Bold, 
                maxLines = 1
            )
            Text(
                text = "₹${item.product.priceInINR.toInt()}", 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onQuantityChange(item.quantity - 1) },
                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Remove, 
                        contentDescription = "Decrease Quantity", 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "${item.quantity}",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { onQuantityChange(item.quantity + 1) },
                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Increase Quantity", 
                        tint = MaterialTheme.colorScheme.onPrimary, 
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.DeleteOutline, 
                contentDescription = "Remove from Cart", 
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun CartSummary(
    subtotal: Double,
    onCheckoutClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "₹${subtotal.toInt()}", 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Delivery", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "FREE", 
                    color = MaterialTheme.colorScheme.primary, 
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total", 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${subtotal.toInt()}", 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            WorldFindButton(text = "PROCEED TO CHECKOUT", onClick = onCheckoutClick)
        }
    }
}

@Composable
fun EmptyCartState(onContinueShopping: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your cart is empty",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Looks like you haven't added anything yet",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        WorldFindButton(
            text = "CONTINUE SHOPPING",
            onClick = onContinueShopping,
            modifier = Modifier.width(200.dp)
        )
    }
}
