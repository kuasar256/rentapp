package com.example.rentapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rentapp.data.local.AppDatabase
import com.example.rentapp.data.local.entity.Payment
import java.util.Calendar

class AutoPaymentGeneratorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val contractDao = db.contractDao()
            val paymentDao = db.paymentDao()
            
            val activeContracts = contractDao.getAllContractsSync().filter { it.status == "ACTIVE" }
            val now = Calendar.getInstance()
            val currentMonth = now.get(Calendar.MONTH) + 1
            val currentYear = now.get(Calendar.YEAR)

            activeContracts.forEach { contract ->
                val payments = paymentDao.getAllPaymentsByYearMonthSync(currentYear, currentMonth)
                if (payments.none { it.contractId == contract.id }) {
                    val dueDate = Calendar.getInstance().apply {
                        set(Calendar.YEAR, currentYear)
                        set(Calendar.MONTH, currentMonth - 1)
                        set(Calendar.DAY_OF_MONTH, contract.paymentDueDay)
                        set(Calendar.HOUR_OF_DAY, 9)
                        set(Calendar.MINUTE, 0)
                    }.timeInMillis

                    paymentDao.insertPayment(
                        Payment(
                            contractId = contract.id,
                            amount = contract.monthlyRent,
                            dueDate = dueDate,
                            status = "PENDING",
                            month = currentMonth,
                            year = currentYear
                        )
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("AutoPaymentWorker", "Error generating payments: ${e.message}")
            Result.retry()
        }
    }
}
