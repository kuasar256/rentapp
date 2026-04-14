package com.example.rentapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contracts",
    foreignKeys = [
        ForeignKey(entity = Property::class, parentColumns = ["id"], childColumns = ["propertyId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tenant::class, parentColumns = ["id"], childColumns = ["tenantId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("propertyId"), Index("tenantId")]
)
data class Contract(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyId: Long,
    val tenantId: Long,
    val startDate: Long,
    val endDate: Long,
    val monthlyRent: Double,
    val deposit: Double,
    val paymentDueDay: Int = 5, // Day of month payment is due
    val status: String = "ACTIVE", // "ACTIVE", "EXPIRED", "TERMINATED"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
