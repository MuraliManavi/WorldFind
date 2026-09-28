package com.murali.worldfind.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.data.local.PreferenceManager
import com.murali.worldfind.ui.components.WorldFindTopBar
import com.murali.worldfind.utils.Constants
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val themeMode by preferenceManager.themeMode.collectAsState(initial = "dark")
    val isNotificationsEnabled by preferenceManager.isNotificationsEnabled.collectAsState(initial = true)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WorldFindTopBar(
                title = "Settings",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "App Settings", 
                color = MaterialTheme.colorScheme.onBackground, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsSwitchItem(
                title = "Push Notifications",
                description = "Receive updates about your orders",
                checked = isNotificationsEnabled,
                onCheckedChange = { 
                    scope.launch {
                        preferenceManager.setNotificationsEnabled(it)
                    }
                }
            )
            
            SettingsSwitchItem(
                title = "Dark Mode",
                description = "Enable dark theme for the app",
                checked = themeMode == "dark",
                onCheckedChange = { 
                    scope.launch {
                        preferenceManager.setThemeMode(if (it) "dark" else "light")
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "About WorldFind", 
                color = MaterialTheme.colorScheme.onBackground, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsLinkItem(title = "Privacy Policy", onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PRIVACY_POLICY_URL))
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar("Unable to open browser") }
                }
            })
            SettingsLinkItem(title = "Terms of Service", onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.TERMS_OF_SERVICE_URL))
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar("Unable to open browser") }
                }
            })
            SettingsLinkItem(title = "App Version", value = Constants.APP_VERSION)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            TextButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = "LOGOUT", 
                    color = MaterialTheme.colorScheme.error, 
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                color = MaterialTheme.colorScheme.onBackground, 
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingsLinkItem(title: String, value: String? = null, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = MaterialTheme.colorScheme.onBackground)
        if (value != null) {
            Text(
                text = value, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontSize = 14.sp
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
