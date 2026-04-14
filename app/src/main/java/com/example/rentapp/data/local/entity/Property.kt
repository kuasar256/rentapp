package com.example.rentapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String,
    val type: String, // "Casa", "Departamento", "Local", "Oficina"
    val monthlyRent: Double,
    val rooms: Int,
    val bathrooms: Int,
    val area: Double, // m²
    val status: String, // "AVAILABLE", "RENTED", "MAINTENANCE"
    val description: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
