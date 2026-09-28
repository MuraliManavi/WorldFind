package com.murali.worldfind.ui.screens.checkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.data.models.Address
import com.murali.worldfind.ui.components.WorldFindButton
import com.murali.worldfind.ui.components.WorldFindTopBar
import com.murali.worldfind.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onPlaceOrder: (String) -> Unit,
    onAddAddressClick: () -> Unit,
    mainViewModel: MainViewModel
) {
    val cartItems by mainViewModel.cartItems.collectAsState()
    val directItem by mainViewModel.directCheckoutItem.collectAsState()
    val checkoutItems = if (directItem != null) listOf(directItem!!) else cartItems
    
    val addresses by mainViewModel.addresses.collectAsState()
    val selectedAddressFromVm by mainViewModel.selectedCheckoutAddress.collectAsState()
    
    val selectedAddress = selectedAddressFromVm ?: addresses.find { it.isDefault } ?: addresses.firstOrNull()
    
    val subtotal = checkoutItems.sumOf { it.product.priceInINR * it.quantity }
    var selectedPayment by remember { mutableStateOf("Razorpay") }
    var isPlacingOrder by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    BackHandler {
        mainViewModel.setDirectCheckout(null)
        onBackClick()
    }

    Scaffold(
        topBar = {
            WorldFindTopBar(
                title = "Checkout",
                onBackClick = {
                    mainViewModel.setDirectCheckout(null)
                    onBackClick()
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Amount", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${subtotal.toInt()}", 
                            color = MaterialTheme.colorScheme.onSurface, 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    WorldFindButton(
                        text = "PLACE ORDER",
                        loading = isPlacingOrder,
                        enabled = selectedAddress != null && !isPlacingOrder,
                        onClick = {
                            selectedAddress?.let { addr ->
                                isPlacingOrder = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        val orderId = mainViewModel.placeOrder(addr, selectedPayment)
                                        if (orderId != null) {
                                            isPlacingOrder = false
                                            onPlaceOrder(orderId)
                                        } else {
                                            isPlacingOrder = false
                                            errorMessage = "Failed to place order. Please check your network connection."
                                        }
                                    } catch (e: Exception) {
                                        isPlacingOrder = false
                                        errorMessage = e.message ?: "An error occurred while placing your order."
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            CheckoutSection(
                title = "Delivery Address", 
                icon = Icons.Default.LocationOn,
                actionText = if (addresses.isEmpty()) "ADD" else "CHANGE",
                onActionClick = onAddAddressClick
            ) {
                if (selectedAddress != null) {
                    Text(
                        text = selectedAddress.name, 
                        color = MaterialTheme.colorScheme.onSurface, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedAddress.addressLine, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${selectedAddress.city}, ${selectedAddress.state} - ${selectedAddress.postalCode}", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Phone: ${selectedAddress.phone}", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "No address saved. Please add one to continue.", 
                        color = MaterialTheme.colorScheme.error, 
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CheckoutSection(title = "Payment Method", icon = Icons.Default.Payment) {
                RadioButtonRow(
                    text = "Razorpay (Online Payment)",
                    selected = selectedPayment == "Razorpay",
                    onClick = { selectedPayment = "Razorpay" }
                )
                RadioButtonRow(
                    text = "Cash on Delivery",
                    selected = selectedPayment == "COD",
                    onClick = { selectedPayment = "COD" }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Order Summary", 
                color = MaterialTheme.colorScheme.onSurface, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            checkoutItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.product.title} x ${item.quantity}", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "₹${(item.product.priceInINR * item.quantity).toInt()}", 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow("Subtotal", "₹${subtotal.toInt()}")
            SummaryRow("Delivery Fee", "FREE", MaterialTheme.colorScheme.primary)
            SummaryRow("Discount", "- ₹0")
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CheckoutSection(
    title: String, 
    icon: ImageVector, 
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onBackground, 
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title, 
                    color = MaterialTheme.colorScheme.onBackground, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp
                )
            }
            if (actionText != null && onActionClick != null) {
                TextButton(onClick = onActionClick) {
                    Text(text = actionText, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun RadioButtonRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.outline
            )
        )
        Text(
            text = text, 
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, 
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun SummaryRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, color = valueColor, fontWeight = FontWeight.Bold)
    }
}
