package com.example.rentapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "property_conditions",
    foreignKeys = [
        ForeignKey(entity = Property::class, parentColumns = ["id"], childColumns = ["propertyId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Contract::class, parentColumns = ["id"], childColumns = ["contractId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("propertyId"), Index("contractId")]
)
data class PropertyCondition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyId: Long = 0,
    val contractId: Long = 0,
    val type: String = "CHECK_IN", // "CHECK_IN", "CHECK_OUT"
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val imageUris: String = "", // Comma-separated list of local URIs
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
