package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.PropertyDao
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.sync.FirestoreSyncManager
import kotlinx.coroutines.flow.Flow

class PropertyRepository(
    private val propertyDao: PropertyDao,
    private val syncManager: FirestoreSyncManager? = null
) {
    fun getAllProperties(): Flow<List<Property>> = propertyDao.getAllProperties()
    fun getPropertiesByStatus(status: String): Flow<List<Property>> = propertyDao.getPropertiesByStatus(status)
    fun getAvailableCount(): Flow<Int> = propertyDao.getAvailableCount()
    fun getRentedCount(): Flow<Int> = propertyDao.getRentedCount()
    fun getTotalMonthlyRevenue(): Flow<Double> = propertyDao.getTotalMonthlyRevenue()
    fun getTotalCount(): Flow<Int> = propertyDao.getTotalCount()
    suspend fun getPropertyById(id: Long): Property? = propertyDao.getPropertyById(id)
    
    suspend fun insertProperty(property: Property): Long {
        val id = propertyDao.insertProperty(property)
        val insertedProperty = property.copy(id = id)
        syncManager?.pushProperty(insertedProperty)
        return id
    }

    suspend fun updateProperty(property: Property) {
        val updatedProperty = property.copy(updatedAt = System.currentTimeMillis())
        propertyDao.updateProperty(updatedProperty)
        syncManager?.pushProperty(updatedProperty)
    }

    suspend fun deleteProperty(property: Property) {
        propertyDao.deleteProperty(property)
        syncManager?.deleteProperty(property)
    }
}
