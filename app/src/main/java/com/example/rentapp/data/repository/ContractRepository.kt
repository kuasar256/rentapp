package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.ContractDao
import com.example.rentapp.data.local.entity.Contract
import kotlinx.coroutines.flow.Flow

class ContractRepository(private val contractDao: ContractDao) {
    fun getAllContracts(): Flow<List<Contract>> = contractDao.getAllContracts()
    fun getContractsByProperty(propertyId: Long): Flow<List<Contract>> = contractDao.getContractsByProperty(propertyId)
    fun getContractsByTenant(tenantId: Long): Flow<List<Contract>> = contractDao.getContractsByTenant(tenantId)
    fun getActiveContracts(): Flow<List<Contract>> = contractDao.getActiveContracts()
    suspend fun getContractById(id: Long): Contract? = contractDao.getContractById(id)
    suspend fun insertContract(contract: Contract): Long = contractDao.insertContract(contract)
    suspend fun updateContract(contract: Contract) = contractDao.updateContract(contract)
    suspend fun deleteContract(contract: Contract) = contractDao.deleteContract(contract)
}
