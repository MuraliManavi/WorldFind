package com.murali.worldfind.data.models

data class User(
    val id: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String? = null,
    val profileImageUrl: String? = null
)