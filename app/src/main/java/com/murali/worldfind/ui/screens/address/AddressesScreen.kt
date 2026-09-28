package com.murali.worldfind.ui.screens.address

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.murali.worldfind.data.models.Address
import com.murali.worldfind.ui.components.WorldFindTopBar
import com.murali.worldfind.ui.viewmodel.MainViewModel

@Composable
fun AddressesScreen(
    onBackClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onEditAddressClick: (String) -> Unit,
    onAddressSelected: (Address) -> Unit,
    mainViewModel: MainViewModel
) {
    val addresses by mainViewModel.addresses.collectAsState()
    var addressToDelete by remember { mutableStateOf<Address?>(null) }

    if (addressToDelete != null) {
        AlertDialog(
            onDismissRequest = { addressToDelete = null },
            title = { Text("Delete Address") },
            text = { Text("Are you sure you want to delete this address?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        addressToDelete?.let { mainViewModel.deleteAddress(it.id) }
                        addressToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { addressToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            WorldFindTopBar(
                title = "My Addresses",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAddressClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Address")
            }
        }
    ) { padding ->
        if (addresses.isEmpty()) {
            EmptyAddressesState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(addresses) { address ->
                    AddressItemCard(
                        address = address,
                        onClick = { 
                            mainViewModel.selectCheckoutAddress(address)
                            onAddressSelected(address) 
                        },
                        onEditClick = { onEditAddressClick(address.id) },
                        onDeleteClick = { addressToDelete = address },
                        onSetDefaultClick = { mainViewModel.setDefaultAddress(address.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AddressItemCard(
    address: Address,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSetDefaultClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = if (address.isDefault) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = address.name, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontWeight = FontWeight.Bold
                )
                if (address.isDefault) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = " DEFAULT ",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                } else {
                    TextButton(onClick = onSetDefaultClick) {
                        Text(
                            text = "Set as Default", 
                            color = MaterialTheme.colorScheme.primary, 
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(text = address.addressLine, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text(text = "${address.city}, ${address.state} - ${address.postalCode}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text(text = "Phone: ${address.phone}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = "Edit Address", 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.DeleteOutline, 
                        contentDescription = "Delete Address", 
                        tint = MaterialTheme.colorScheme.error, 
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyAddressesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No addresses saved",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your delivery addresses for faster checkout",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}
