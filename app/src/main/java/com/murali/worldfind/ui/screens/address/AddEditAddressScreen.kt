package com.murali.worldfind.ui.screens.address

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.murali.worldfind.data.models.Address
import com.murali.worldfind.ui.components.WorldFindButton
import com.murali.worldfind.ui.components.WorldFindTextField
import com.murali.worldfind.ui.components.WorldFindTopBar
import com.murali.worldfind.ui.viewmodel.AuthViewModel
import com.murali.worldfind.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun AddEditAddressScreen(
    addressId: String? = null,
    onBackClick: () -> Unit,
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val addresses by mainViewModel.addresses.collectAsState()
    val firebaseUser by authViewModel.currentUser.collectAsState()
    val existingAddress = addresses.find { it.id == addressId }

    var name by remember { mutableStateOf(existingAddress?.name ?: firebaseUser?.displayName ?: "") }
    var phone by remember { mutableStateOf(existingAddress?.phone ?: "") }
    var addressLine by remember { mutableStateOf(existingAddress?.addressLine ?: "") }
    var city by remember { mutableStateOf(existingAddress?.city ?: "") }
    var state by remember { mutableStateOf(existingAddress?.state ?: "") }
    var postalCode by remember { mutableStateOf(existingAddress?.postalCode ?: "") }
    var isDefault by remember { mutableStateOf(existingAddress?.isDefault ?: false) }
    
    var errorText by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            WorldFindTopBar(
                title = if (addressId == null) "Add Address" else "Edit Address",
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
            if (errorText != null) {
                Text(
                    text = errorText!!, 
                    color = MaterialTheme.colorScheme.error, 
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            WorldFindTextField(
                value = name, 
                onValueChange = { name = it; errorText = null }, 
                label = "Full Name",
                enabled = !isSaving
            )
            Spacer(modifier = Modifier.height(16.dp))
            WorldFindTextField(
                value = phone, 
                onValueChange = { phone = it; errorText = null }, 
                label = "Phone Number (10 Digits)", 
                keyboardType = KeyboardType.Phone,
                enabled = !isSaving
            )
            Spacer(modifier = Modifier.height(16.dp))
            WorldFindTextField(
                value = addressLine, 
                onValueChange = { addressLine = it; errorText = null }, 
                label = "Address Line",
                enabled = !isSaving
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Box(modifier = Modifier.weight(1f)) {
                    WorldFindTextField(
                        value = city, 
                        onValueChange = { city = it; errorText = null }, 
                        label = "City",
                        enabled = !isSaving
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    WorldFindTextField(
                        value = state, 
                        onValueChange = { state = it; errorText = null }, 
                        label = "State",
                        enabled = !isSaving
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            WorldFindTextField(
                value = postalCode, 
                onValueChange = { postalCode = it; errorText = null }, 
                label = "PIN Code (6 Digits)", 
                keyboardType = KeyboardType.Number,
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                    enabled = !isSaving,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary, 
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(text = "Set as default address", color = MaterialTheme.colorScheme.onBackground)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            WorldFindButton(
                text = if (addressId == null) "SAVE ADDRESS" else "UPDATE ADDRESS",
                loading = isSaving,
                enabled = !isSaving,
                onClick = {
                    val cleanPhone = phone.filter { it.isDigit() }
                    val cleanPin = postalCode.filter { it.isDigit() }

                    when {
                        name.isBlank() -> errorText = "Please enter your full name."
                        phone.isBlank() -> errorText = "Please enter your phone number."
                        cleanPhone.length < 10 || !cleanPhone.takeLast(10).matches(Regex("^[6-9][0-9]{9}$")) ->
                            errorText = "Please enter a valid 10-digit Indian mobile number."
                        addressLine.isBlank() -> errorText = "Please enter your address line."
                        city.isBlank() -> errorText = "Please enter your city."
                        state.isBlank() -> errorText = "Please enter your state."
                        postalCode.isBlank() -> errorText = "Please enter your PIN code."
                        !cleanPin.matches(Regex("^[1-9][0-9]{5}$")) ->
                            errorText = "Please enter a valid 6-digit Indian PIN code (e.g. 110001, 400001, 560001)."
                        else -> {
                            isSaving = true
                            errorText = null
                            scope.launch {
                                try {
                                    val newAddress = Address(
                                        id = addressId ?: UUID.randomUUID().toString(),
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        addressLine = addressLine.trim(),
                                        city = city.trim(),
                                        state = state.trim(),
                                        postalCode = cleanPin,
                                        isDefault = isDefault
                                    )
                                    if (addressId == null) {
                                        mainViewModel.addAddress(newAddress)
                                    } else {
                                        mainViewModel.updateAddress(newAddress)
                                    }
                                    isSaving = false
                                    onBackClick()
                                } catch (e: Exception) {
                                    isSaving = false
                                    errorText = e.message ?: "Failed to save address. Please try again."
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
