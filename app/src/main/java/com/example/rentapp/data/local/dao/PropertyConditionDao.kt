package com.example.rentapp.data.local.dao

import androidx.room.*
import com.example.rentapp.data.local.entity.PropertyCondition
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyConditionDao {
    @Query("SELECT * FROM property_conditions WHERE propertyId = :propertyId ORDER BY date DESC")
    fun getConditionsByProperty(propertyId: Long): Flow<List<PropertyCondition>>

    @Query("SELECT * FROM property_conditions WHERE contractId = :contractId")
    fun getConditionsByContract(contractId: Long): Flow<List<PropertyCondition>>

    @Query("SELECT * FROM property_conditions WHERE id = :id")
    suspend fun getConditionById(id: Long): PropertyCondition?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCondition(condition: PropertyCondition): Long

    @Update
    suspend fun updateCondition(condition: PropertyCondition)

    @Delete
    suspend fun deleteCondition(condition: PropertyCondition)
}
