package com.example.rentapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rentapp.R
import com.example.rentapp.data.local.AppDatabase

class PaymentAuditWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(context)
            val paymentDao = db.paymentDao()

            val pendingPayments = paymentDao.getPendingPaymentsSync()
            val currentTime = System.currentTimeMillis()

            var delayedCount = 0

            for (payment in pendingPayments) {
                if (payment.dueDate < currentTime) {
                    // Update status to DELAYED
                    paymentDao.updatePayment(payment.copy(status = "DELAYED"))
                    delayedCount++
                }
            }

            if (delayedCount > 0) {
                showNotification(
                    title = "Pagos Atrasados Detectados",
                    message = "Se encontraron $delayedCount pagos que han vencido. Revise la aplicación para los detalles."
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "rentapp_notifications"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones de Pagos",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            // Use standard icon since custom might not be present yet
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
