package com.example.rentapp.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object ReminderManager {

    private const val REMINDER_WORK_NAME = "payment_reminder_work"

    fun scheduleDailyReminder(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val reminderRequest = PeriodicWorkRequestBuilder<PaymentReminderWorker>(
            1, TimeUnit.DAYS // Se ejecuta una vez al día
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Mantiene la existente si ya hay una
            reminderRequest
        )
    }
    
    fun runImmediateReminder(context: Context) {
        val immediateRequest = OneTimeWorkRequestBuilder<PaymentReminderWorker>()
            .build()
        
        WorkManager.getInstance(context).enqueue(immediateRequest)
    }
}
