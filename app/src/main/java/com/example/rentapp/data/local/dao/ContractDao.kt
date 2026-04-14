package com.example.rentapp.data.local.dao

import androidx.room.*
import com.example.rentapp.data.local.entity.Contract
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractDao {
    @Query("SELECT * FROM contracts ORDER BY createdAt DESC")
    fun getAllContracts(): Flow<List<Contract>>

    @Query("SELECT * FROM contracts WHERE propertyId = :propertyId")
    fun getContractsByProperty(propertyId: Long): Flow<List<Contract>>

    @Query("SELECT * FROM contracts WHERE tenantId = :tenantId")
    fun getContractsByTenant(tenantId: Long): Flow<List<Contract>>

    @Query("SELECT * FROM contracts WHERE id = :id")
    suspend fun getContractById(id: Long): Contract?

    @Query("SELECT * FROM contracts WHERE status = 'ACTIVE'")
    fun getActiveContracts(): Flow<List<Contract>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: Contract): Long

    @Update
    suspend fun updateContract(contract: Contract)

    @Delete
    suspend fun deleteContract(contract: Contract)
}
