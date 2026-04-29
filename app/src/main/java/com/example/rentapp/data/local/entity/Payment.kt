package com.example.rentapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(entity = Contract::class, parentColumns = ["id"], childColumns = ["contractId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("contractId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contractId: Long = 0,
    val amount: Double = 0.0,
    val dueDate: Long = 0,
    val paidDate: Long? = null,
    val status: String = "PENDING", // "PENDING", "DELAYED", "PAID"
    val month: Int = 0, // 1-12
    val year: Int = 0,
    val paymentMethod: String = "", // "Efectivo", "Transferencia", "Cheque"
    val receiptNumber: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null
)
