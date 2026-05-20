package com.example.rentapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rentapp.data.local.AppDatabase
import com.example.rentapp.notification.NotificationHelper
import java.util.Calendar

/**
 * Worker responsible for auditing payments and sending reminders.
 * This worker is designed specifically for landlords to track their tenants' payments
 * and receive alerts about overdue or upcoming collections.
 */
class PaymentReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val paymentDao = database.paymentDao()
        
        val currentTime = System.currentTimeMillis()
        
        // 1. Marcar pagos como atrasados si la fecha de vencimiento ya pasó
        val updatedCount = paymentDao.markOverduePayments(currentTime)
        
        // 2. Obtener pagos atrasados
        val delayedPayments = paymentDao.getDelayedPaymentsSync()
        
        // 3. Obtener pagos pendientes para hoy o mañana
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val threshold = calendar.timeInMillis
        
        val pendingPayments = paymentDao.getPendingPaymentsSync().filter { 
            it.dueDate <= threshold 
        }

        if (delayedPayments.isNotEmpty()) {
            NotificationHelper.showPaymentNotification(
                applicationContext,
                "Pagos Atrasados",
                "Tienes ${delayedPayments.size} pagos vencidos que requieren tu atención."
            )
        } else if (pendingPayments.isNotEmpty()) {
            NotificationHelper.showPaymentNotification(
                applicationContext,
                "Pagos Próximos",
                "Tienes ${pendingPayments.size} pagos que vencen pronto. ¡Recuérdale a tus inquilinos!"
            )
        } else if (updatedCount > 0) {
             NotificationHelper.showPaymentNotification(
                applicationContext,
                "Actualización de Cobros",
                "Se han detectado $updatedCount nuevos pagos como atrasados."
            )
        }

        return Result.success()
    }
}
