package com.murali.worldfind.data.repository

import com.murali.worldfind.data.models.Address
import com.murali.worldfind.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AddressRepository(
    private val api: WorldFindApi = ApiClient.api
) {
    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: StateFlow<List<Address>> = _addresses.asStateFlow()

    suspend fun fetchAddresses(): Result<List<Address>> {
        return try {
            val response = api.getAddresses()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val dtoList = response.body()!!.data!!
                val list = dtoList.map { it.toDomainAddress() }
                _addresses.value = list
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to fetch addresses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAddress(address: Address): Result<Address> {
        return try {
            val response = api.addAddress(address.toDto())
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val created = response.body()!!.data!!.toDomainAddress()
                fetchAddresses()
                Result.success(created)
            } else {
                Result.failure(Exception("Failed to add address"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAddress(address: Address): Result<Address> {
        return try {
            val response = api.updateAddress(address.id, address.toDto())
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val updated = response.body()!!.data!!.toDomainAddress()
                fetchAddresses()
                Result.success(updated)
            } else {
                Result.failure(Exception("Failed to update address"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAddress(addressId: String): Result<Unit> {
        return try {
            val response = api.deleteAddress(addressId)
            if (response.isSuccessful) {
                fetchAddresses()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete address"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setDefaultAddress(addressId: String): Result<List<Address>> {
        return try {
            val response = api.setDefaultAddress(addressId)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomainAddress() }
                _addresses.value = list
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to set default address"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun AddressDto.toDomainAddress(): Address {
        return Address(
            id = id,
            name = name,
            phone = phone,
            addressLine = addressLine,
            city = city,
            state = state,
            postalCode = postalCode,
            country = country,
            isDefault = isDefault
        )
    }

    private fun Address.toDto(): AddressDto {
        return AddressDto(
            id = id,
            name = name,
            phone = phone,
            addressLine = addressLine,
            city = city,
            state = state,
            postalCode = postalCode,
            country = country,
            isDefault = isDefault
        )
    }
}
