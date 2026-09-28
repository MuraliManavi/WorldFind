package com.murali.worldfind.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.ui.components.WorldFindTopBar

@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit
) {
    val systemNotifications = listOf(
        NotificationItem("Welcome to WorldFind", "Explore global products with INR currency advantage.", "Just now"),
        NotificationItem("Shipping Information", "International shipping typically takes 10-15 business days.", "1d ago"),
        NotificationItem("Premium Black & White", "Enjoy our new premium design identity.", "2d ago")
    )

    Scaffold(
        topBar = {
            WorldFindTopBar(
                title = "Notifications",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (systemNotifications.isEmpty()) {
                item {
                    EmptyNotificationsState()
                }
            } else {
                item {
                    Text(
                        text = "System Updates",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(systemNotifications) { notification ->
                    NotificationRow(notification)
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant, 
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationRow(notification: NotificationItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Notifications, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title, 
                color = MaterialTheme.colorScheme.onSurface, 
                fontWeight = FontWeight.Bold
            )
            Text(
                text = notification.message, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.time, 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), 
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun EmptyNotificationsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 100.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsOff, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No notifications",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We'll notify you when something important happens",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

data class NotificationItem(
    val title: String,
    val message: String,
    val time: String
)
