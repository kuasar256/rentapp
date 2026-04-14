package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.PaymentDao
import com.example.rentapp.data.local.entity.Payment
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val paymentDao: PaymentDao) {
    fun getAllPayments(): Flow<List<Payment>> = paymentDao.getAllPayments()
    fun getPaymentsByContract(contractId: Long): Flow<List<Payment>> = paymentDao.getPaymentsByContract(contractId)
    fun getPaymentsByStatus(status: String): Flow<List<Payment>> = paymentDao.getPaymentsByStatus(status)
    fun getPendingAndDelayedPayments(): Flow<List<Payment>> = paymentDao.getPendingAndDelayedPayments()
    fun getPaymentsByYear(year: Int): Flow<List<Payment>> = paymentDao.getPaymentsByYear(year)
    fun getPaymentsByYearMonth(year: Int, month: Int): Flow<List<Payment>> = paymentDao.getPaymentsByYearMonth(year, month)
    fun getTotalCollectedByYear(year: Int): Flow<Double> = paymentDao.getTotalCollectedByYear(year)
    fun getDelayedCount(): Flow<Int> = paymentDao.getDelayedCount()
    fun getPendingCount(): Flow<Int> = paymentDao.getPendingCount()
    fun getDelayedPayments(): Flow<List<Payment>> = paymentDao.getDelayedPayments()
    suspend fun getPaymentById(id: Long): Payment? = paymentDao.getPaymentById(id)
    suspend fun insertPayment(payment: Payment): Long = paymentDao.insertPayment(payment)
    suspend fun updatePayment(payment: Payment) = paymentDao.updatePayment(payment)
    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)
}
