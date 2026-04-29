package com.example.rentapp

import android.app.Application
import android.util.Log
import com.example.rentapp.notification.NotificationHelper

class RentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("RentAppDebug", "RentApp.onCreate called")
        // Crear canales de notificación al iniciar la app
        NotificationHelper.createChannels(this)
    }
}
