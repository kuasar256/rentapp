package com.example.rentapp.data.local.dao

import androidx.room.*
import com.example.rentapp.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE propertyId = :propertyId ORDER BY date DESC")
    fun getExpensesByProperty(propertyId: Long): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startTime AND date <= :endTime")
    fun getTotalExpensesInPeriod(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT * FROM expenses WHERE remoteId = :remoteId")
    suspend fun getExpenseByRemoteId(remoteId: String): Expense?

    @Query("SELECT * FROM expenses WHERE remoteId IS NULL")
    suspend fun getUnsyncedExpenses(): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
