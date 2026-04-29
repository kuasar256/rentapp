package com.example.rentapp.viewmodel

import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.data.repository.PaymentRepository
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
class PaymentViewModelTest {

    private lateinit var viewModel: PaymentViewModel
    private val repository: PaymentRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { repository.getAllPayments() } returns flowOf(emptyList())
        coEvery { repository.getPaymentsByStatus(any()) } returns flowOf(emptyList())
        coEvery { repository.getDelayedPayments() } returns flowOf(emptyList())
        coEvery { repository.getPendingAndDelayedPayments() } returns flowOf(emptyList())
        coEvery { repository.getDelayedCount() } returns flowOf(2)
        coEvery { repository.getPendingCount() } returns flowOf(5)

        viewModel = PaymentViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `counts should be updated from repository`() = runTest {
        assertEquals(2, viewModel.delayedCount.value)
        assertEquals(5, viewModel.pendingCount.value)
    }

    @Test
    fun `markAsPaid should update payment via repository`() = runTest {
        val payment = Payment(id = 1, contractId = 10, amount = 100.0, dueDate = 1000L, status = "PENDING", month = 1, year = 2024)
        
        viewModel.markAsPaid(payment)
        
        coVerify { repository.updatePayment(match { it.status == "PAID" && it.paidDate != null }) }
    }

    @Test
    fun `deletePayment should call repository delete`() = runTest {
        val payment = Payment(id = 1, contractId = 10, amount = 100.0, dueDate = 1000L, status = "PENDING", month = 1, year = 2024)
        
        viewModel.deletePayment(payment)
        
        coVerify { repository.deletePayment(payment) }
    }
}
