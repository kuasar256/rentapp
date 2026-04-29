package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.PaymentDao
import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.sync.FirestoreSyncManager
import kotlinx.coroutines.flow.Flow

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val syncManager: FirestoreSyncManager? = null
) {
    fun getAllPayments(): Flow<List<Payment>> = paymentDao.getAllPayments()
    fun getPaymentsByContract(contractId: Long): Flow<List<Payment>> = paymentDao.getPaymentsByContract(contractId)
    fun getPaymentsByStatus(status: String): Flow<List<Payment>> = paymentDao.getPaymentsByStatus(status)
    fun getPendingAndDelayedPayments(): Flow<List<Payment>> = paymentDao.getPendingAndDelayedPayments()
    fun getPaymentsByYear(year: Int): Flow<List<Payment>> = paymentDao.getPaymentsByYear(year)
    fun getPaymentsByYearMonth(year: Int, month: Int): Flow<List<Payment>> = paymentDao.getPaymentsByYearMonth(year, month)
    fun getTotalCollectedByYear(year: Int): Flow<Double> = paymentDao.getTotalCollectedByYear(year)
    fun getMonthlyEarningsByYear(year: Int): Flow<List<com.example.rentapp.data.local.dao.MonthlyEarning>> = paymentDao.getMonthlyEarningsByYear(year)
    fun getPaidCountByYear(year: Int): Flow<Int> = paymentDao.getPaidCountByYear(year)
    fun getPendingCountByYear(year: Int): Flow<Int> = paymentDao.getPendingCountByYear(year)
    fun getDelayedCountByYear(year: Int): Flow<Int> = paymentDao.getDelayedCountByYear(year)
    fun getDelayedCount(): Flow<Int> = paymentDao.getDelayedCount()
    fun getPendingCount(): Flow<Int> = paymentDao.getPendingCount()
    fun getDelayedPayments(): Flow<List<Payment>> = paymentDao.getDelayedPayments()
    suspend fun getPaymentById(id: Long): Payment? = paymentDao.getPaymentById(id)
    
    suspend fun insertPayment(payment: Payment): Long {
        val id = paymentDao.insertPayment(payment)
        val insertedPayment = payment.copy(id = id)
        syncManager?.pushPayment(insertedPayment)
        return id
    }

    suspend fun updatePayment(payment: Payment) {
        val updatedPayment = payment.copy(updatedAt = System.currentTimeMillis())
        paymentDao.updatePayment(updatedPayment)
        syncManager?.pushPayment(updatedPayment)
    }

    suspend fun deletePayment(payment: Payment) {
        paymentDao.deletePayment(payment)
    }
}
