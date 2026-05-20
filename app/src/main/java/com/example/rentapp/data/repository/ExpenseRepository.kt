package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.ExpenseDao
import com.example.rentapp.data.local.entity.Expense
import com.example.rentapp.sync.FirestoreSyncManager
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val syncManager: FirestoreSyncManager? = null
) {
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()
    
    fun getExpensesByProperty(propertyId: Long): Flow<List<Expense>> = 
        expenseDao.getExpensesByProperty(propertyId)

    fun getTotalExpensesInPeriod(startTime: Long, endTime: Long): Flow<Double> = 
        expenseDao.getTotalExpensesInPeriod(startTime, endTime)

    suspend fun insertExpense(expense: Expense): Long {
        val id = expenseDao.insertExpense(expense)
        val insertedExpense = expense.copy(id = id)
        syncManager?.pushExpense(insertedExpense)
        return id
    }

    suspend fun updateExpense(expense: Expense) {
        val updatedExpense = expense.copy(updatedAt = System.currentTimeMillis())
        expenseDao.updateExpense(updatedExpense)
        syncManager?.pushExpense(updatedExpense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
        syncManager?.deleteExpense(expense)
    }
}
