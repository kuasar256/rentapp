package com.example.rentapp.data.local.dao

import androidx.room.*
import com.example.rentapp.data.local.entity.Property
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY createdAt DESC")
    fun getAllProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE status = :status ORDER BY createdAt DESC")
    fun getPropertiesByStatus(status: String): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyById(id: Long): Property?

    @Query("SELECT COUNT(*) FROM properties WHERE status = 'AVAILABLE'")
    fun getAvailableCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM properties WHERE status = 'RENTED'")
    fun getRentedCount(): Flow<Int>

    @Query("SELECT IFNULL(SUM(monthlyRent), 0.0) FROM properties WHERE status = 'RENTED'")
    fun getTotalMonthlyRevenue(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: Property): Long

    @Update
    suspend fun updateProperty(property: Property)

    @Delete
    suspend fun deleteProperty(property: Property)

    @Query("SELECT COUNT(*) FROM properties")
    fun getTotalCount(): Flow<Int>
}
