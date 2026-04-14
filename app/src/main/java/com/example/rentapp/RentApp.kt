package com.example.rentapp

import android.app.Application
// import com.google.firebase.FirebaseApp

import android.util.Log

class RentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("RentAppDebug", "RentApp.onCreate called")
        // Initialize Firebase (Comentado hasta tener google-services.json)
        // FirebaseApp.initializeApp(this)
    }
}
