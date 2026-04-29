package com.example.rentapp.sync

import android.content.Context
import android.util.Log
import com.example.rentapp.data.local.AppDatabase
import com.example.rentapp.data.local.entity.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val db = AppDatabase.getDatabase(context)
    private val propertyDao = db.propertyDao()
    private val tenantDao = db.tenantDao()
    private val contractDao = db.contractDao()
    private val paymentDao = db.paymentDao()
    private val userDao = db.userDao()

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Inicia los escuchas en tiempo real para todas las colecciones.
     * Cualquier cambio en Firebase se reflejará en la base de datos SQL local.
     */
    fun startSyncListeners() {
        val uid = auth.currentUser?.uid ?: return
        Log.d("SyncManager", "Iniciando escuchas de sincronización para el usuario: $uid")

        val userDoc = firestore.collection("users").document(uid)

        // Escuchar Propiedades
        userDoc.collection("properties").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("SyncManager", "Error en listener de propiedades: ${e.message}")
                return@addSnapshotListener
            }
            scope.launch {
                try {
                    snapshots?.forEach { doc ->
                        val remote = doc.toObject(Property::class.java)
                        val local = propertyDao.getPropertyByRemoteId(doc.id)
                        if (local == null) {
                            propertyDao.insertProperty(remote.copy(remoteId = doc.id))
                        } else if (remote.updatedAt > local.updatedAt) {
                            propertyDao.updateProperty(remote.copy(id = local.id, remoteId = doc.id))
                        }
                    }
                } catch (ex: Exception) {
                    Log.e("SyncManager", "Error procesando propiedades: ${ex.message}")
                }
            }
        }

        // Escuchar Inquilinos
        userDoc.collection("tenants").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("SyncManager", "Error en listener de inquilinos: ${e.message}")
                return@addSnapshotListener
            }
            scope.launch {
                try {
                    snapshots?.forEach { doc ->
                        val remote = doc.toObject(Tenant::class.java)
                        val local = tenantDao.getTenantByRemoteId(doc.id)
                        if (local == null) {
                            tenantDao.insertTenant(remote.copy(remoteId = doc.id))
                        } else if (remote.updatedAt > local.updatedAt) {
                            tenantDao.updateTenant(remote.copy(id = local.id, remoteId = doc.id))
                        }
                    }
                } catch (ex: Exception) {
                    Log.e("SyncManager", "Error procesando inquilinos: ${ex.message}")
                }
            }
        }

        // Escuchar Contratos
        userDoc.collection("contracts").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("SyncManager", "Error en listener de contratos: ${e.message}")
                return@addSnapshotListener
            }
            scope.launch {
                try {
                    snapshots?.forEach { doc ->
                        val remote = doc.toObject(Contract::class.java)
                        val local = contractDao.getContractByRemoteId(doc.id)
                        if (local == null) {
                            contractDao.insertContract(remote.copy(remoteId = doc.id))
                        } else if (remote.updatedAt > local.updatedAt) {
                            contractDao.updateContract(remote.copy(id = local.id, remoteId = doc.id))
                        }
                    }
                } catch (ex: Exception) {
                    Log.e("SyncManager", "Error procesando contratos: ${ex.message}")
                }
            }
        }

        // Escuchar Pagos
        userDoc.collection("payments").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e("SyncManager", "Error en listener de pagos: ${e.message}")
                return@addSnapshotListener
            }
            scope.launch {
                try {
                    snapshots?.forEach { doc ->
                        val remote = doc.toObject(Payment::class.java)
                        val local = paymentDao.getPaymentByRemoteId(doc.id)
                        if (local == null) {
                            paymentDao.insertPayment(remote.copy(remoteId = doc.id))
                        } else if (remote.updatedAt > local.updatedAt) {
                            paymentDao.updatePayment(remote.copy(id = local.id, remoteId = doc.id))
                        }
                    }
                } catch (ex: Exception) {
                    Log.e("SyncManager", "Error procesando pagos: ${ex.message}")
                }
            }
        }
    }

    /**
     * Sincroniza datos locales que aún no tienen remoteId (migración o nuevos registros offline)
     */
    suspend fun syncAllLocalData() {
        val uid = auth.currentUser?.uid ?: return
        val userDoc = firestore.collection("users").document(uid)

        try {
            // Sincronizar Propiedades
            propertyDao.getUnsyncedProperties().forEach { item ->
                val docRef = userDoc.collection("properties").document()
                val syncedItem = item.copy(remoteId = docRef.id, updatedAt = System.currentTimeMillis())
                docRef.set(syncedItem).await()
                propertyDao.updateProperty(syncedItem)
            }

            // Sincronizar Inquilinos
            tenantDao.getUnsyncedTenants().forEach { item ->
                val docRef = userDoc.collection("tenants").document()
                val syncedItem = item.copy(remoteId = docRef.id, updatedAt = System.currentTimeMillis())
                docRef.set(syncedItem).await()
                tenantDao.updateTenant(syncedItem)
            }

            // Sincronizar Contratos
            contractDao.getUnsyncedContracts().forEach { item ->
                val docRef = userDoc.collection("contracts").document()
                val syncedItem = item.copy(remoteId = docRef.id, updatedAt = System.currentTimeMillis())
                docRef.set(syncedItem).await()
                contractDao.updateContract(syncedItem)
            }

            // Sincronizar Pagos
            paymentDao.getUnsyncedPayments().forEach { item ->
                val docRef = userDoc.collection("payments").document()
                val syncedItem = item.copy(remoteId = docRef.id, updatedAt = System.currentTimeMillis())
                docRef.set(syncedItem).await()
                paymentDao.updatePayment(syncedItem)
            }

            Log.d("SyncManager", "Sincronización inicial completada")
        } catch (e: Exception) {
            Log.e("SyncManager", "Error en syncAllLocalData: ${e.message}")
        }
    }

    /**
     * Sincroniza un objeto individual (usado por los repositorios al insertar/actualizar)
     */
    suspend fun <T> pushEntity(entity: T, collection: String) {
        val uid = auth.currentUser?.uid ?: return
        val userDoc = firestore.collection("users").document(uid)
        
        // Esta es una versión simplificada, se usaría reflexión o sobrecarga para manejar tipos específicos
        // Para este MVP, los repositorios llamarán a métodos específicos si es necesario
    }

    // Métodos específicos para los repositorios

    suspend fun deleteProperty(property: Property) {
        try {
            val uid = auth.currentUser?.uid ?: return
            val remoteId = property.remoteId ?: return
            firestore.collection("users").document(uid)
                .collection("properties").document(remoteId)
                .delete().await()
            Log.d("SyncManager", "Propiedad eliminada de Firestore: $remoteId")
        } catch (e: Exception) {
            Log.e("SyncManager", "Error al eliminar propiedad de Firestore: ${e.message}")
        }
    }

    suspend fun deleteTenant(tenant: Tenant) {
        try {
            val uid = auth.currentUser?.uid ?: return
            val remoteId = tenant.remoteId ?: return
            firestore.collection("users").document(uid)
                .collection("tenants").document(remoteId)
                .delete().await()
            Log.d("SyncManager", "Inquilino eliminado de Firestore: $remoteId")
        } catch (e: Exception) {
            Log.e("SyncManager", "Error al eliminar inquilino de Firestore: ${e.message}")
        }
    }

    suspend fun pushProperty(property: Property) {
        try {
            val uid = auth.currentUser?.uid ?: return
            val docId = property.remoteId ?: firestore.collection("users").document(uid).collection("properties").document().id
            val updated = property.copy(remoteId = docId, updatedAt = System.currentTimeMillis())
            firestore.collection("users").document(uid).collection("properties").document(docId).set(updated).await()
            propertyDao.updateProperty(updated)
        } catch (e: Exception) {
            Log.e("SyncManager", "Error al subir propiedad: ${e.message}")
        }
    }

    suspend fun pushTenant(tenant: Tenant) {
        try {
            val uid = auth.currentUser?.uid ?: return
            val docId = tenant.remoteId ?: firestore.collection("users").document(uid).collection("tenants").document().id
            val updated = tenant.copy(remoteId = docId, updatedAt = System.currentTimeMillis())
            firestore.collection("users").document(uid).collection("tenants").document(docId).set(updated).await()
            tenantDao.updateTenant(updated)
        } catch (e: Exception) {
            Log.e("SyncManager", "Error al subir inquilino: ${e.message}")
        }
    }

    suspend fun pushContract(contract: Contract) {
        try {
            val uid = auth.currentUser?.uid ?: return
            val docId = contract.remoteId ?: firestore.collection("users").document(uid).collection("contracts").document().id
            val updated = contract.copy(remoteId = docId, updatedAt = System.currentTimeMillis())
            firestore.collection("users").document(uid).collection("contracts").document(docId).set(updated).await()
            contractDao.updateContract(updated)
        } catch (e: Exception) {
            Log.e("SyncManager", "Error al subir contrato: ${e.message}")
        }
    }

    suspend fun pushPayment(payment: Payment) {
        try {
            val uid = auth.currentUser?.uid ?: return
            val docId = payment.remoteId ?: firestore.collection("users").document(uid).collection("payments").document().id
            val updated = payment.copy(remoteId = docId, updatedAt = System.currentTimeMillis())
            firestore.collection("users").document(uid).collection("payments").document(docId).set(updated).await()
            paymentDao.updatePayment(updated)
        } catch (e: Exception) {
            Log.e("SyncManager", "Error al subir pago: ${e.message}")
        }
    }
}
