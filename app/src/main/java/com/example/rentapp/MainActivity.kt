package com.example.rentapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rentapp.ui.navigation.RentAppNavGraph
import com.example.rentapp.ui.theme.RentAppTheme
import com.example.rentapp.worker.PaymentAuditWorker
import java.util.concurrent.TimeUnit

import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("RentAppDebug", "MainActivity.onCreate called")
        
        // Setup WorkManager for Background Audit with try-catch
        try {
            val auditWorkRequest = PeriodicWorkRequestBuilder<PaymentAuditWorker>(24, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "PaymentAuditWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                auditWorkRequest
            )
            Log.d("RentAppDebug", "WorkManager task enqueued successfully")
        } catch (e: Exception) {
            Log.e("RentAppDebug", "Error setting up WorkManager: ${e.message}")
            e.printStackTrace()
        }

        setContent {
            RentAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    RentAppNavGraph(navController = navController)
                }
            }
        }
    }
}
