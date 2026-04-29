package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.ContractDao
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.sync.FirestoreSyncManager
import kotlinx.coroutines.flow.Flow

class ContractRepository(
    private val contractDao: ContractDao,
    private val syncManager: FirestoreSyncManager? = null
) {
    fun getAllContracts(): Flow<List<Contract>> = contractDao.getAllContracts()
    fun getContractsByProperty(propertyId: Long): Flow<List<Contract>> = contractDao.getContractsByProperty(propertyId)
    fun getContractsByTenant(tenantId: Long): Flow<List<Contract>> = contractDao.getContractsByTenant(tenantId)
    fun getActiveContracts(): Flow<List<Contract>> = contractDao.getActiveContracts()
    suspend fun getContractById(id: Long): Contract? = contractDao.getContractById(id)
    
    suspend fun insertContract(contract: Contract): Long {
        val id = contractDao.insertContract(contract)
        val insertedContract = contract.copy(id = id)
        syncManager?.pushContract(insertedContract)
        return id
    }

    suspend fun updateContract(contract: Contract) {
        val updatedContract = contract.copy(updatedAt = System.currentTimeMillis())
        contractDao.updateContract(updatedContract)
        syncManager?.pushContract(updatedContract)
    }

    suspend fun deleteContract(contract: Contract) {
        contractDao.deleteContract(contract)
    }
}
