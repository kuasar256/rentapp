package com.example.rentapp.data.local.dao

import androidx.room.*
import com.example.rentapp.data.local.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY dueDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE contractId = :contractId ORDER BY dueDate DESC")
    fun getPaymentsByContract(contractId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE status = :status ORDER BY dueDate DESC")
    fun getPaymentsByStatus(status: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE status = 'PENDING' OR status = 'DELAYED' ORDER BY dueDate ASC")
    fun getPendingAndDelayedPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): Payment?

    @Query("SELECT * FROM payments WHERE year = :year ORDER BY month ASC")
    fun getPaymentsByYear(year: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE year = :year AND month = :month")
    fun getPaymentsByYearMonth(year: Int, month: Int): Flow<List<Payment>>

    @Query("SELECT IFNULL(SUM(amount), 0.0) FROM payments WHERE status = 'PAID' AND year = :year")
    fun getTotalCollectedByYear(year: Int): Flow<Double>

    @Query("SELECT COUNT(*) FROM payments WHERE status = 'DELAYED'")
    fun getDelayedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM payments WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM payments WHERE status = 'DELAYED' ORDER BY dueDate ASC")
    fun getDelayedPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE status = 'PENDING'")
    suspend fun getPendingPaymentsSync(): List<Payment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)
}
