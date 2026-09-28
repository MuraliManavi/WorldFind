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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.ui.components.*
import com.murali.worldfind.ui.viewmodel.AuthState
import com.murali.worldfind.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localErrorText by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterClick()
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
        WorldFindLogo()

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Create Account",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your WorldFind account",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (displayError != null) {
            Text(
                text = displayError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
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
            placeholder = "Enter your full name",
            enabled = authState !is AuthState.Loading
        )

        Spacer(modifier = Modifier.height(14.dp))

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

        Spacer(modifier = Modifier.height(14.dp))

        WorldFindTextField(
            value = password,
            onValueChange = { 
                password = it 
                localErrorText = null
                if (authState is AuthState.Error) authViewModel.resetAuthState()
            },
            label = "Password",
            isPassword = true,
            placeholder = "Minimum 6 characters",
            keyboardType = KeyboardType.Password,
            enabled = authState !is AuthState.Loading
        )

        Spacer(modifier = Modifier.height(14.dp))

        WorldFindTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it 
                localErrorText = null
                if (authState is AuthState.Error) authViewModel.resetAuthState()
            },
            label = "Confirm Password",
            isPassword = true,
            placeholder = "Re-enter your password",
            keyboardType = KeyboardType.Password,
            enabled = authState !is AuthState.Loading
        )

        Spacer(modifier = Modifier.height(24.dp))

        WorldFindButton(
            text = "CREATE ACCOUNT",
            loading = authState is AuthState.Loading,
            onClick = {
                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    localErrorText = "Please fill in all fields."
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    localErrorText = "Please enter a valid email address."
                } else if (password.length < 6) {
                    localErrorText = "Password must be at least 6 characters."
                } else if (password != confirmPassword) {
                    localErrorText = "Passwords do not match."
                } else {
                    authViewModel.register(name, email, password)
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Already have an account?",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        WorldFindOutlinedButton(
            text = "LOGIN",
            enabled = authState !is AuthState.Loading,
            onClick = onLoginClick
        )
    }
}
