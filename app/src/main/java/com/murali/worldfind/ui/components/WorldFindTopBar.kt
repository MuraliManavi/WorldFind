package com.murali.worldfind.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldFindTopBar(
    title: String? = null,
    showLogo: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    showNotification: Boolean = false,
    onNotificationClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (showLogo) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "WorldFind Logo",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = title ?: "WORLD FIND",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },

        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },

        actions = {
            if (showNotification) {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "View Notifications"
                    )
                }
            }
        },

        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}
