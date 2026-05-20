package com.example.rentapp.presentation

import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.data.local.entity.Contract

/**
 * Mock data for presentation purposes.
 * This simulates the test cases and functional flow.
 */
object ValidationMocks {
    
    val mockTenant = Tenant(
        id = 101,
        firstName = "Juan",
        lastName = "Pérez",
        email = "juan.perez@example.com",
        phone = "555-0123",
        status = "ACTIVE"
    )

    val mockProperty = Property(
        id = 201,
        name = "Departamento Coyoacán",
        address = "Av. Universidad 1200",
        type = "Apartment",
        monthlyRent = 12000.0,
        status = "AVAILABLE"
    )

    val mockContract = Contract(
        id = 301,
        propertyId = 201,
        tenantId = 101,
        monthlyRent = 12000.0,
        status = "ACTIVE"
    )

    // Simulation of validation logic
    fun validateField(value: String): String? {
        return if (value.isBlank()) "Error: Field Required" else null
    }
}
