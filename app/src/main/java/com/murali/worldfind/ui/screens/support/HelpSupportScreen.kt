package com.murali.worldfind.ui.screens.support

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.ui.components.WorldFindTopBar
import com.murali.worldfind.utils.Constants
import kotlinx.coroutines.launch

@Composable
fun HelpSupportScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WorldFindTopBar(
                title = "Help & Support",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Common Topics", 
                color = MaterialTheme.colorScheme.onBackground, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            SupportItem("Shipping & Delivery", Icons.Default.Info) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${Constants.SUPPORT_EMAIL}")
                    putExtra(Intent.EXTRA_SUBJECT, "Shipping Inquiry - WorldFind")
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar("No email app found") }
                }
            }
            SupportItem("Returns & Refunds", Icons.AutoMirrored.Filled.HelpOutline) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${Constants.SUPPORT_EMAIL}")
                    putExtra(Intent.EXTRA_SUBJECT, "Returns Inquiry - WorldFind")
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar("No email app found") }
                }
            }
            SupportItem("Payment Issues", Icons.Default.QuestionAnswer) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${Constants.SUPPORT_EMAIL}")
                    putExtra(Intent.EXTRA_SUBJECT, "Payment Inquiry - WorldFind")
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    scope.launch { snackbarHostState.showSnackbar("No email app found") }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Contact Us", 
                color = MaterialTheme.colorScheme.onBackground, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().clickable {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${Constants.SUPPORT_EMAIL}")
                        putExtra(Intent.EXTRA_SUBJECT, "Support Inquiry - WorldFind")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        scope.launch { snackbarHostState.showSnackbar("No email app found") }
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Email Support", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Typically responds within 24 hours", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), 
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Version ${Constants.APP_VERSION}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SupportItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                modifier = Modifier.weight(1f)
            )
        }
    }
}
