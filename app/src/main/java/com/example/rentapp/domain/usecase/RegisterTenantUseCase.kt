package com.example.rentapp.domain.usecase

import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.data.repository.TenantRepository

/**
 * Business logic to register a new tenant.
 * Demonstrates the use of domain layer for the presentation.
 */
class RegisterTenantUseCase(private val repository: TenantRepository) {
    
    suspend fun execute(tenant: Tenant): Result<Long> {
        return try {
            if (tenant.firstName.isBlank() || tenant.lastName.isBlank()) {
                Result.failure(Exception("Missing required fields"))
            } else {
                val id = repository.insertTenant(tenant)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
