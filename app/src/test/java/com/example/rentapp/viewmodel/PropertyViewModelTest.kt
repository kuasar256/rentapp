package com.example.rentapp.viewmodel

import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.data.repository.PropertyRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PropertyViewModelTest {

    private lateinit var viewModel: PropertyViewModel
    private val repository: PropertyRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        val properties = listOf(
            Property(id = 1, name = "Casa 1", address = "Calle 123", type = "Casa", monthlyRent = 500.0, rooms = 2, bathrooms = 1, area = 80.0, status = "AVAILABLE")
        )
        coEvery { repository.getAllProperties() } returns flowOf(properties)
        coEvery { repository.getPropertiesByStatus(any()) } returns flowOf(emptyList())
        coEvery { repository.getAvailableCount() } returns flowOf(1)
        coEvery { repository.getRentedCount() } returns flowOf(0)
        coEvery { repository.getTotalMonthlyRevenue() } returns flowOf(500.0)
        coEvery { repository.getTotalCount() } returns flowOf(1)

        viewModel = PropertyViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `allProperties should emit properties from repository`() = runTest {
        val properties = viewModel.allProperties.value
        assertEquals(1, properties.size)
        assertEquals("Casa 1", properties[0].name)
    }

    @Test
    fun `insertProperty should call repository`() = runTest {
        val property = Property(name = "New", address = "Addr", type = "Casa", monthlyRent = 400.0, rooms = 1, bathrooms = 1, area = 50.0, status = "AVAILABLE")
        
        viewModel.insertProperty(property)
        
        coVerify { repository.insertProperty(property) }
    }

    @Test
    fun `stats should be correct`() = runTest {
        assertEquals(1, viewModel.availableCount.value)
        assertEquals(500.0, viewModel.totalMonthlyRevenue.value, 0.1)
    }
}
