package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.TenantDao
import com.example.rentapp.data.local.entity.Tenant
import kotlinx.coroutines.flow.Flow

class TenantRepository(private val tenantDao: TenantDao) {
    fun getAllTenants(): Flow<List<Tenant>> = tenantDao.getAllTenants()
    fun getTenantsByStatus(status: String): Flow<List<Tenant>> = tenantDao.getTenantsByStatus(status)
    fun searchTenants(query: String): Flow<List<Tenant>> = tenantDao.searchTenants(query)
    fun getActiveCount(): Flow<Int> = tenantDao.getActiveCount()
    suspend fun getTenantById(id: Long): Tenant? = tenantDao.getTenantById(id)
    suspend fun insertTenant(tenant: Tenant): Long = tenantDao.insertTenant(tenant)
    suspend fun updateTenant(tenant: Tenant) = tenantDao.updateTenant(tenant)
    suspend fun deleteTenant(tenant: Tenant) = tenantDao.deleteTenant(tenant)
}
