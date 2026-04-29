package com.example.rentapp.notification

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class RentAppMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
    }

    /**
     * Se ejecuta cuando llega un mensaje FCM.
     * Funciona tanto en foreground como en background (data messages).
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Mensaje FCM recibido de: ${remoteMessage.from}")

        val title   = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "RentApp"
        val body    = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: ""
        val channel = remoteMessage.data["channel"] ?: NotificationHelper.CHANNEL_GENERAL

        if (channel == NotificationHelper.CHANNEL_PAYMENTS) {
            NotificationHelper.showPaymentNotification(applicationContext, title, body)
        } else {
            NotificationHelper.showGeneralNotification(applicationContext, title, body)
        }
    }

    /**
     * Se llama cuando FCM genera un nuevo token para este dispositivo.
     * Guardamos el token en Firestore bajo el documento del usuario autenticado.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener { Log.d(TAG, "Token FCM guardado en Firestore") }
            .addOnFailureListener { e -> Log.e(TAG, "Error guardando token: ${e.message}") }
    }
}
