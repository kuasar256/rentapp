package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.PaymentDao
import com.example.rentapp.data.local.entity.Payment
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PaymentRepositoryTest {

    private lateinit var repository: PaymentRepository
    private val paymentDao: PaymentDao = mockk()

    @Before
    fun setup() {
        repository = PaymentRepository(paymentDao)
    }

    @Test
    fun `getPendingAndDelayedPayments should return combined list from dao`() = runBlocking {
        val payments = listOf(
            Payment(id = 1, contractId = 10, amount = 100.0, dueDate = 1000L, status = "PENDING", month = 1, year = 2024),
            Payment(id = 2, contractId = 10, amount = 100.0, dueDate = 500L, status = "DELAYED", month = 1, year = 2024)
        )
        coEvery { paymentDao.getPendingAndDelayedPayments() } returns flowOf(payments)

        val result = repository.getPendingAndDelayedPayments()

        result.collect {
            assertEquals(2, it.size)
            assertEquals("PENDING", it[0].status)
            assertEquals("DELAYED", it[1].status)
        }
    }

    @Test
    fun `getDelayedCount should return count from dao`() = runBlocking {
        coEvery { paymentDao.getDelayedCount() } returns flowOf(3)

        val result = repository.getDelayedCount()

        result.collect {
            assertEquals(3, it)
        }
    }

    @Test
    fun `updatePayment should call dao update`() = runBlocking {
        val payment = Payment(id = 1, contractId = 10, amount = 100.0, dueDate = 1000L, status = "PAID", month = 1, year = 2024)
        coEvery { paymentDao.updatePayment(any()) } returns Unit

        repository.updatePayment(payment)

        coVerify { paymentDao.updatePayment(payment) }
    }
}
