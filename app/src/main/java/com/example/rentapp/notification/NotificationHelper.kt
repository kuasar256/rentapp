package com.example.rentapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.rentapp.MainActivity
import com.example.rentapp.R

object NotificationHelper {

    const val CHANNEL_PAYMENTS = "rentapp_payments"
    const val CHANNEL_GENERAL  = "rentapp_general"

    private var notificationId = 2000

    /**
     * Crea todos los canales de notificación al arrancar la app.
     * Debe llamarse desde Application.onCreate().
     */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val paymentsChannel = NotificationChannel(
                CHANNEL_PAYMENTS,
                "Pagos y Alertas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios de pagos pendientes, vencidos y próximos"
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "Notificaciones Generales",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Mensajes generales de RentApp"
            }

            manager.createNotificationChannel(paymentsChannel)
            manager.createNotificationChannel(generalChannel)
        }
    }

    /** Obtiene un PendingIntent que abre MainActivity al tocar la notificación. */
    private fun mainActivityIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Muestra una notificación de pago (pagos atrasados / auditoría).
     */
    fun showPaymentNotification(context: Context, title: String, message: String) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_PAYMENTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainActivityIntent(context))
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId++, notification)
    }

    /**
     * Muestra una notificación general (mensajes FCM, avisos).
     */
    fun showGeneralNotification(context: Context, title: String, message: String) {
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(mainActivityIntent(context))
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId++, notification)
    }
}
