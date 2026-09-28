package com.murali.worldfind.ui.screens.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.ui.components.WorldFindTopBar
import com.murali.worldfind.ui.theme.*
import com.murali.worldfind.ui.viewmodel.MainViewModel
import com.murali.worldfind.utils.CurrencyConverter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderDetailsScreen(
    orderId: String,
    onBackClick: () -> Unit,
    mainViewModel: MainViewModel
) {
    val orders by mainViewModel.orders.collectAsState()
    val order = remember(orderId, orders) {
        orders.find { it.id == orderId }
    }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            WorldFindTopBar(
                title = "Order Details",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        if (order == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding), 
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Order not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Order ID", 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = order.id, 
                                    color = MaterialTheme.colorScheme.onSurface, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            OrderStatusBadge(status = order.status)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Placed on", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            fontSize = 12.sp
                        )
                        Text(
                            text = dateFormat.format(Date(order.timestamp)), 
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Items", 
                    color = MaterialTheme.colorScheme.onBackground, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                             Text(text = "🛍️", fontSize = 20.sp) 
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.product.title, 
                                color = MaterialTheme.colorScheme.onSurface, 
                                maxLines = 1
                            )
                            Text(
                                text = "${item.quantity} x ${CurrencyConverter.formatINR(item.product.priceInINR)}", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = CurrencyConverter.formatINR(item.product.priceInINR * item.quantity), 
                            color = MaterialTheme.colorScheme.onSurface, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Shipping Address", 
                    color = MaterialTheme.colorScheme.onBackground, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = order.deliveryAddress, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Payment Summary", 
                    color = MaterialTheme.colorScheme.onBackground, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                DetailSummaryRow("Subtotal", CurrencyConverter.formatINR(order.totalAmount))
                DetailSummaryRow("Delivery", "FREE", WorldSuccess)
                DetailSummaryRow("Total Amount", CurrencyConverter.formatINR(order.totalAmount))
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun DetailSummaryRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, color = valueColor, fontWeight = FontWeight.Bold)
    }
}
