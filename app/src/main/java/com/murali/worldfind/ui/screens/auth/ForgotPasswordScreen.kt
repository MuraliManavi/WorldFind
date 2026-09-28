package com.murali.worldfind.ui.screens.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.ui.components.*
import com.murali.worldfind.ui.viewmodel.AuthState
import com.murali.worldfind.ui.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onBackToLoginClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var localErrorText by remember { mutableStateOf<String?>(null) }
    
    val authState by authViewModel.authState.collectAsState()

    val displayError = (authState as? AuthState.Error)?.message ?: localErrorText

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WorldFindLogo()

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Reset Password",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your email to receive a reset link",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (authState is AuthState.PasswordResetSent) {
            Text(
                text = "Check your email. We sent a password reset link to $email.",
                color = MaterialTheme.colorScheme.primary, // Success color in this theme
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        } else {
            if (displayError != null) {
                Text(
                    text = displayError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            WorldFindTextField(
                value = email,
                onValueChange = { 
                    email = it 
                    localErrorText = null
                    if (authState is AuthState.Error) authViewModel.resetAuthState()
                },
                label = "Email Address",
                placeholder = "example@email.com",
                keyboardType = KeyboardType.Email,
                enabled = authState !is AuthState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            WorldFindButton(
                text = "SEND RESET LINK",
                loading = authState is AuthState.Loading,
                onClick = {
                    if (email.isEmpty()) {
                        localErrorText = "Please enter your email address."
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        localErrorText = "Please enter a valid email address."
                    } else {
                        authViewModel.sendPasswordReset(email)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        WorldFindOutlinedButton(
            text = "BACK TO LOGIN",
            enabled = authState !is AuthState.Loading,
            onClick = {
                authViewModel.resetAuthState()
                onBackToLoginClick()
            }
        )
    }
}
