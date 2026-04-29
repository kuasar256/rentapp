package com.example.rentapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val documentId: String = "", // INE, Pasaporte, etc.
    val documentType: String = "INE",
    val nationality: String = "",
    val occupation: String = "",
    val monthlyIncome: Double = 0.0,
    val emergencyContact: String = "",
    val emergencyPhone: String = "",
    val monthlyRentDueDate: Int = 1, // Día del mes (1-31) cuando vence el pago del alquiler
    val photoUrl: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null
)
