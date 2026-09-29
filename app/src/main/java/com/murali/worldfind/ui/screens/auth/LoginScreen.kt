package com.murali.worldfind.ui.screens.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.ui.components.*
import com.murali.worldfind.ui.theme.*
import com.murali.worldfind.ui.viewmodel.AuthState
import com.murali.worldfind.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localErrorText by remember { mutableStateOf<String?>(null) }
    
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginClick()
            authViewModel.resetAuthState()
        }
    }

    val displayError = (authState as? AuthState.Error)?.message ?: localErrorText

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WorldFindLogo(showTagline = true)

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Welcome Back",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Login to continue to WorldFind",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        WorldFindTextField(
            value = password,
            onValueChange = { 
                password = it 
                localErrorText = null
                if (authState is AuthState.Error) authViewModel.resetAuthState()
            },
            label = "Password",
            isPassword = true,
            keyboardType = KeyboardType.Password,
            enabled = authState !is AuthState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Forgot Password?",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = authState !is AuthState.Loading) { onForgotPasswordClick() }
                .padding(vertical = 10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        WorldFindButton(
            text = "LOGIN",
            loading = authState is AuthState.Loading,
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    localErrorText = "Please enter both email and password."
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    localErrorText = "Please enter a valid email address."
                } else {
                    authViewModel.login(email, password)
                }
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Don't have an account?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        WorldFindOutlinedButton(
            text = "CREATE ACCOUNT! IF YOU DON'T HAVE ACCOUNT",
            enabled = authState !is AuthState.Loading,
            onClick = onRegisterClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onLoginClick,
            enabled = authState !is AuthState.Loading
        ) {
            Text("Continue as Guest", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}
