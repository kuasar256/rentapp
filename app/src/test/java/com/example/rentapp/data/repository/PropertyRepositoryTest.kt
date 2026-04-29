package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.PropertyDao
import com.example.rentapp.data.local.entity.Property
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PropertyRepositoryTest {

    private lateinit var repository: PropertyRepository
    private val propertyDao: PropertyDao = mockk()

    @Before
    fun setup() {
        repository = PropertyRepository(propertyDao)
    }

    @Test
    fun `getAllProperties should return flow from dao`() = runBlocking {
        val properties = listOf(
            Property(id = 1, name = "Casa 1", address = "Calle 123", type = "Casa", monthlyRent = 500.0, rooms = 2, bathrooms = 1, area = 80.0, status = "AVAILABLE")
        )
        coEvery { propertyDao.getAllProperties() } returns flowOf(properties)

        val result = repository.getAllProperties()

        result.collect {
            assertEquals(properties, it)
        }
    }

    @Test
    fun `insertProperty should call dao`() = runBlocking {
        val property = Property(name = "Casa 2", address = "Calle 456", type = "Casa", monthlyRent = 600.0, rooms = 3, bathrooms = 2, area = 100.0, status = "AVAILABLE")
        coEvery { propertyDao.insertProperty(any()) } returns 1L

        repository.insertProperty(property)

        coVerify { propertyDao.insertProperty(property) }
    }

    @Test
    fun `getPropertiesByStatus should filter correctly`() = runBlocking {
        val availableProperties = listOf(
            Property(id = 1, name = "Casa 1", address = "Calle 123", type = "Casa", monthlyRent = 500.0, rooms = 2, bathrooms = 1, area = 80.0, status = "AVAILABLE")
        )
        coEvery { propertyDao.getPropertiesByStatus("AVAILABLE") } returns flowOf(availableProperties)

        val result = repository.getPropertiesByStatus("AVAILABLE")

        result.collect {
            assertEquals(availableProperties, it)
        }
    }
}
