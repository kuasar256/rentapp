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
    val propertyId: Long = 0,
    val tenantId: Long = 0,
    val startDate: Long = 0,
    val endDate: Long = 0,
    val monthlyRent: Double = 0.0,
    val deposit: Double = 0.0,
    val paymentDueDay: Int = 5, // Day of month payment is due
    val status: String = "ACTIVE", // "ACTIVE", "EXPIRED", "TERMINATED"
    val notes: String = "",
    val hasEvictionClause: Boolean = true,
    val lateFeePenalty: Double = 0.0,
    val earlyTerminationPenalty: Double = 0.0,
    val guarantorName: String = "",
    val guarantorProperty: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null
)
