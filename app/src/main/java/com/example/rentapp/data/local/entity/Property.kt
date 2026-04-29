package com.example.rentapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val address: String = "",
    val type: String = "", // "Casa", "Departamento", "Local", "Oficina"
    val monthlyRent: Double = 0.0,
    val rooms: Int = 0,
    val bathrooms: Int = 0,
    val area: Double = 0.0, // m²
    val status: String = "AVAILABLE", // "AVAILABLE", "RENTED", "MAINTENANCE"
    val paymentType: String = "Efectivo", // "Efectivo", "Anticrético"
    val description: String = "",
    val rules: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null
)
