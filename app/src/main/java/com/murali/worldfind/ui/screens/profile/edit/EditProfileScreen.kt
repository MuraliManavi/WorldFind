package com.murali.worldfind.ui.screens.profile.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.ui.components.WorldFindButton
import com.murali.worldfind.ui.components.WorldFindTextField
import com.murali.worldfind.ui.components.WorldFindTopBar
import com.murali.worldfind.ui.viewmodel.AuthState
import com.murali.worldfind.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    val firebaseUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    
    var name by remember { mutableStateOf(firebaseUser?.displayName ?: "") }
    var email by remember { mutableStateOf(firebaseUser?.email ?: "") }
    var localErrorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authState) {
        if (authState is AuthState.ProfileUpdated) {
            onBackClick()
            authViewModel.resetAuthState()
        }
    }

    val displayError = (authState as? AuthState.Error)?.message ?: localErrorText

    Scaffold(
        topBar = {
            WorldFindTopBar(
                title = "Edit Profile",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar (Read-only in Phase 1)
            Box {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            if (displayError != null) {
                Text(
                    text = displayError, 
                    color = MaterialTheme.colorScheme.error, 
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            WorldFindTextField(
                value = name, 
                onValueChange = { 
                    name = it 
                    localErrorText = null
                    if (authState is AuthState.Error) authViewModel.resetAuthState()
                }, 
                label = "Full Name",
                enabled = authState !is AuthState.Loading
            )
            Spacer(modifier = Modifier.height(16.dp))
            WorldFindTextField(
                value = email, 
                onValueChange = { email = it }, 
                label = "Email Address", 
                enabled = false
            )
            Text(
                text = "Email cannot be changed currently", 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), 
                fontSize = 12.sp, 
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            WorldFindTextField(
                value = firebaseUser?.phoneNumber ?: "Not provided", 
                onValueChange = {}, 
                label = "Phone Number", 
                enabled = false
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            WorldFindButton(
                text = "SAVE CHANGES",
                loading = authState is AuthState.Loading,
                onClick = {
                    if (name.isBlank()) {
                        localErrorText = "Name cannot be empty"
                    } else {
                        authViewModel.updateProfile(name)
                    }
                }
            )
        }
    }
}
