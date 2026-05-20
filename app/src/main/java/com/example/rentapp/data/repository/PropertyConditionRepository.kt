package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.PropertyConditionDao
import com.example.rentapp.data.local.entity.PropertyCondition
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropertyConditionRepository @Inject constructor(
    private val propertyConditionDao: PropertyConditionDao
) {
    fun getConditionsByProperty(propertyId: Long): Flow<List<PropertyCondition>> =
        propertyConditionDao.getConditionsByProperty(propertyId)

    fun getConditionsByContract(contractId: Long): Flow<List<PropertyCondition>> =
        propertyConditionDao.getConditionsByContract(contractId)

    suspend fun getConditionById(id: Long): PropertyCondition? =
        propertyConditionDao.getConditionById(id)

    suspend fun insertCondition(condition: PropertyCondition): Long =
        propertyConditionDao.insertCondition(condition)

    suspend fun updateCondition(condition: PropertyCondition) =
        propertyConditionDao.updateCondition(condition)

    suspend fun deleteCondition(condition: PropertyCondition) =
        propertyConditionDao.deleteCondition(condition)
}
