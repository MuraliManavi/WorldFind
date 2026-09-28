package com.murali.worldfind.ui.screens.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.data.models.Order
import com.murali.worldfind.data.models.OrderStatus
import com.murali.worldfind.ui.components.WorldFindButton
import com.murali.worldfind.ui.components.WorldFindTopBar
import com.murali.worldfind.ui.theme.*
import com.murali.worldfind.ui.viewmodel.MainViewModel
import com.murali.worldfind.utils.CurrencyConverter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersScreen(
    onBackClick: () -> Unit,
    onOrderClick: (String) -> Unit,
    onDiscoverClick: () -> Unit,
    mainViewModel: MainViewModel
) {
    val orders by mainViewModel.orders.collectAsState()

    Scaffold(
        topBar = {
            WorldFindTopBar(
                title = "My Orders",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            EmptyOrdersState(onDiscoverClick, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(orders) { order ->
                    OrderItemCard(order = order, onClick = { onOrderClick(order.id) })
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(order: Order, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id}", 
                        color = MaterialTheme.colorScheme.onSurface, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat.format(Date(order.timestamp)), 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 12.sp
                    )
                }
                OrderStatusBadge(status = order.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.items.size} items", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyConverter.formatINR(order.totalAmount), 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "View Details", 
                    color = MaterialTheme.colorScheme.primary, 
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus) {
    val color = when (status) {
        OrderStatus.DELIVERED -> WorldSuccess
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
        OrderStatus.PENDING -> WorldWarning
        else -> MaterialTheme.colorScheme.primary
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status.name,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyOrdersState(onDiscover: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Inventory2, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No orders yet",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your purchase history will appear here",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        WorldFindButton(
            text = "DISCOVER PRODUCTS",
            onClick = onDiscover,
            modifier = Modifier.width(220.dp)
        )
    }
}
