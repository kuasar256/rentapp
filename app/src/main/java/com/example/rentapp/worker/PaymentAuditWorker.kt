package com.example.rentapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rentapp.data.local.AppDatabase
import com.example.rentapp.notification.NotificationHelper

class PaymentAuditWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(context)
            val paymentDao = db.paymentDao()

            val currentTime = System.currentTimeMillis()
            val delayedCount = paymentDao.markOverduePayments(currentTime)

            if (delayedCount > 0) {
                NotificationHelper.showPaymentNotification(
                    context = context,
                    title = "Alerta de Morosidad",
                    message = "¡Atención! $delayedCount pago(s) han pasado a estado ATRASADO."
                )
                Log.d("PaymentAuditWorker", "Auditoría completada: $delayedCount pagos marcados como atrasados")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("PaymentAuditWorker", "Error en auditoría: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }
}

