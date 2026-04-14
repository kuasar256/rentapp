package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.PropertyDao
import com.example.rentapp.data.local.entity.Property
import kotlinx.coroutines.flow.Flow

class PropertyRepository(private val propertyDao: PropertyDao) {
    fun getAllProperties(): Flow<List<Property>> = propertyDao.getAllProperties()
    fun getPropertiesByStatus(status: String): Flow<List<Property>> = propertyDao.getPropertiesByStatus(status)
    fun getAvailableCount(): Flow<Int> = propertyDao.getAvailableCount()
    fun getRentedCount(): Flow<Int> = propertyDao.getRentedCount()
    fun getTotalMonthlyRevenue(): Flow<Double> = propertyDao.getTotalMonthlyRevenue()
    fun getTotalCount(): Flow<Int> = propertyDao.getTotalCount()
    suspend fun getPropertyById(id: Long): Property? = propertyDao.getPropertyById(id)
    suspend fun insertProperty(property: Property): Long = propertyDao.insertProperty(property)
    suspend fun updateProperty(property: Property) = propertyDao.updateProperty(property)
    suspend fun deleteProperty(property: Property) = propertyDao.deleteProperty(property)
}
