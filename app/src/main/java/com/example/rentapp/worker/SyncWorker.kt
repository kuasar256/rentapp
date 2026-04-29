package com.example.rentapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rentapp.sync.FirestoreSyncManager
import com.google.firebase.auth.FirebaseAuth

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                return Result.success()
            }

            Log.d("SyncWorker", "Iniciando sincronización periódica en segundo plano...")
            val syncManager = FirestoreSyncManager(applicationContext)
            
            // Realizar una sincronización completa de datos locales no sincronizados
            syncManager.syncAllLocalData()
            
            Log.d("SyncWorker", "Sincronización periódica completada con éxito")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error durante la sincronización periódica: ${e.message}")
            Result.retry()
        }
    }
}
