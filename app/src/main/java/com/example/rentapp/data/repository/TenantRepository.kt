package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.TenantDao
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.sync.FirestoreSyncManager
import kotlinx.coroutines.flow.Flow

class TenantRepository(
    private val tenantDao: TenantDao,
    private val syncManager: FirestoreSyncManager? = null
) {
    fun getAllTenants(): Flow<List<Tenant>> = tenantDao.getAllTenants()
    fun getTenantsByStatus(status: String): Flow<List<Tenant>> = tenantDao.getTenantsByStatus(status)
    fun searchTenants(query: String): Flow<List<Tenant>> = tenantDao.searchTenants(query)
    fun getActiveCount(): Flow<Int> = tenantDao.getActiveCount()
    suspend fun getTenantById(id: Long): Tenant? = tenantDao.getTenantById(id)
    
    suspend fun insertTenant(tenant: Tenant): Long {
        val id = tenantDao.insertTenant(tenant)
        val insertedTenant = tenant.copy(id = id)
        syncManager?.pushTenant(insertedTenant)
        return id
    }

    suspend fun updateTenant(tenant: Tenant) {
        val updatedTenant = tenant.copy(updatedAt = System.currentTimeMillis())
        tenantDao.updateTenant(updatedTenant)
        syncManager?.pushTenant(updatedTenant)
    }

    suspend fun deleteTenant(tenant: Tenant) {
        tenantDao.deleteTenant(tenant)
        syncManager?.deleteTenant(tenant)
    }
}
