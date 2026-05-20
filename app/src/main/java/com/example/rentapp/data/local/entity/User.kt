package com.example.rentapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val company: String = "",
    val rfc: String = "",
    val address: String = "",
    val avatarUrl: String = "",
    @Deprecated("The app is now landlord-only. This field is kept for schema compatibility but defaults to 'Landlord'.")
    val userType: String = "Landlord", // "Landlord" or "Tenant"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null
)
