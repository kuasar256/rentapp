package com.example.rentapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repair_budgets")
data class RepairBudget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyId: Long,
    val description: String,
    val estimatedCost: Double,
    val actualCost: Double? = null,
    val status: String = "PENDING", // PENDING, APPROVED, IN_PROGRESS, COMPLETED
    val startDate: Long? = null,
    val completionDate: Long? = null,
    val provider: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
