package com.murali.worldfind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.murali.worldfind.data.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object PasswordResetSent : AuthState()
    object ProfileUpdated : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLoggedIn: Boolean
        get() = repository.isUserLoggedIn()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signInWithEmailPassword(email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull())
            } else {
                _authState.value = AuthState.Error(mapFirebaseError(result.exceptionOrNull()))
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.registerWithEmailPassword(name, email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull())
            } else {
                _authState.value = AuthState.Error(mapFirebaseError(result.exceptionOrNull()))
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                _authState.value = AuthState.PasswordResetSent
            } else {
                _authState.value = AuthState.Error(mapFirebaseError(result.exceptionOrNull()))
            }
        }
    }

    fun updateProfile(name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.updateProfile(name)
            if (result.isSuccess) {
                _authState.value = AuthState.ProfileUpdated
            } else {
                _authState.value = AuthState.Error(mapFirebaseError(result.exceptionOrNull()))
            }
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    private fun mapFirebaseError(exception: Throwable?): String {
        if (exception is FirebaseAuthException) {
            return when (exception.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
                "ERROR_USER_NOT_FOUND" -> "No WorldFind account exists with this email."
                "ERROR_WRONG_PASSWORD" -> "Incorrect email or password."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "An account already exists with this email."
                "ERROR_WEAK_PASSWORD" -> "Password is too weak. Minimum 6 characters required."
                "ERROR_INVALID_CREDENTIAL" -> "Invalid credentials provided."
                "ERROR_OPERATION_NOT_ALLOWED" -> "Email/Password login is not enabled."
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
                "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Check your internet connection."
                else -> exception.message ?: "Authentication failed. Please try again."
            }
        }
        return exception?.message ?: "An unexpected error occurred."
    }
}
