package com.example.rentapp.data.local.dao

import androidx.room.*
import com.example.rentapp.data.local.entity.RepairBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairBudgetDao {
    @Query("SELECT * FROM repair_budgets WHERE propertyId = :propertyId ORDER BY createdAt DESC")
    fun getBudgetsByProperty(propertyId: Long): Flow<List<RepairBudget>>

    @Query("SELECT * FROM repair_budgets ORDER BY createdAt DESC")
    fun getAllBudgets(): Flow<List<RepairBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: RepairBudget): Long

    @Update
    suspend fun updateBudget(budget: RepairBudget)

    @Delete
    suspend fun deleteBudget(budget: RepairBudget)

    @Query("SELECT * FROM repair_budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): RepairBudget?
}
