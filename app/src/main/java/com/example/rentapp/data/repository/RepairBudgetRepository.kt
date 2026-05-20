package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.RepairBudgetDao
import com.example.rentapp.data.local.entity.RepairBudget
import kotlinx.coroutines.flow.Flow

class RepairBudgetRepository(private val repairBudgetDao: RepairBudgetDao) {
    fun getBudgetsByProperty(propertyId: Long): Flow<List<RepairBudget>> = 
        repairBudgetDao.getBudgetsByProperty(propertyId)

    fun getAllBudgets(): Flow<List<RepairBudget>> = 
        repairBudgetDao.getAllBudgets()

    suspend fun insertBudget(budget: RepairBudget) = 
        repairBudgetDao.insertBudget(budget)

    suspend fun updateBudget(budget: RepairBudget) = 
        repairBudgetDao.updateBudget(budget)

    suspend fun deleteBudget(budget: RepairBudget) = 
        repairBudgetDao.deleteBudget(budget)
}
