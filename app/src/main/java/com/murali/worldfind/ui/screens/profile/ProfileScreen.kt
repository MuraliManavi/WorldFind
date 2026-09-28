package com.murali.worldfind.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    onOrdersClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit,
    onHelpSupportClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    val firebaseUser by authViewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "My Profile",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Profile Header
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onEditProfileClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = firebaseUser?.displayName ?: "Guest User", 
                    color = MaterialTheme.colorScheme.onBackground, 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = firebaseUser?.email ?: "Sign in to access your profile", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontSize = 14.sp
                )
                Text(
                    text = "Edit Profile", 
                    color = MaterialTheme.colorScheme.primary, 
                    fontSize = 12.sp, 
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))

        // Profile Menu
        ProfileMenuItem(title = "My Orders", icon = Icons.Default.ShoppingBag, onClick = onOrdersClick)
        ProfileMenuItem(title = "My Wishlist", icon = Icons.Default.Favorite, onClick = onWishlistClick)
        ProfileMenuItem(title = "Shipping Addresses", icon = Icons.Default.LocationOn, onClick = onAddressesClick)
        ProfileMenuItem(title = "Payment Methods", icon = Icons.Default.Payment, onClick = onPaymentMethodsClick)
        
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(24.dp))
        
        ProfileMenuItem(title = "Settings", icon = Icons.Default.Settings, onClick = onSettingsClick)
        ProfileMenuItem(title = "Help & Support", icon = Icons.AutoMirrored.Filled.HelpOutline, onClick = onHelpSupportClick)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ProfileMenuItem(
            title = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
            onClick = onLogoutClick,
            textColor = MaterialTheme.colorScheme.error,
            iconColor = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = iconColor, 
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title, 
                color = textColor, 
                modifier = Modifier.weight(1f), 
                fontSize = 16.sp
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
