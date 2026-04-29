package com.example.rentapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.rentapp.data.preferences.PreferencesManager
import com.example.rentapp.ui.navigation.RentAppNavGraph
import com.example.rentapp.ui.navigation.Screen
import com.example.rentapp.ui.theme.RentAppTheme
import com.example.rentapp.worker.PaymentAuditWorker
import java.util.concurrent.TimeUnit
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() { // Use FragmentActivity for BiometricPrompt
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

            // Setup SyncWorker (Background sync every 4 hours)
            val syncWorkRequest = PeriodicWorkRequestBuilder<com.example.rentapp.worker.SyncWorker>(4, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "SyncWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )
        } catch (e: Exception) {
            Log.e("RentAppDebug", "Error setting up WorkManager: ${e.message}")
            e.printStackTrace()
        }

        // Firebase Offline Persistence Configuration
        val firestoreDb = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        firestoreDb.firestoreSettings = settings

        // Initialize Synchronization Manager
        val syncManager = com.example.rentapp.sync.FirestoreSyncManager(applicationContext)
        
        // Data Seeding for demonstration
        val database = com.example.rentapp.data.local.AppDatabase.getDatabase(this)
        val dataSeeder = com.example.rentapp.data.local.DataSeeder(database)
        
        // Start listeners and initial sync if user is logged in
        lifecycleScope.launch {
            dataSeeder.seedIfNeeded()
            if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
                syncManager.startSyncListeners()
                syncManager.syncAllLocalData()
            }
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

    override fun onPause() {
        super.onPause()
        // No actualizamos LAST_AUTH_TIMESTAMP aquí para que la biometría se pida 
        // solo si han pasado más de 30s DESDE el último éxito.
    }

    fun onBiometricSuccess() {
        Log.d("RentAppDebug", "onBiometricSuccess: Updating auth timestamp")
        lifecycleScope.launch {
            PreferencesManager.setLastAuthTimestamp(this@MainActivity, System.currentTimeMillis())
            Log.d("RentAppDebug", "onBiometricSuccess: Timestamp updated in preferences")
        }
    }
}

