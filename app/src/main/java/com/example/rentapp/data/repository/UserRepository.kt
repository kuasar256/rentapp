package com.example.rentapp.data.repository

import com.example.rentapp.data.local.dao.UserDao
import com.example.rentapp.data.local.entity.User
import com.example.rentapp.sync.FirestoreSyncManager
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    private val syncManager: FirestoreSyncManager? = null
) {
    fun getUser(): Flow<User?> = userDao.getUser()
    
    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)
    
    suspend fun insertUser(user: User): Long {
        val id = userDao.insertUser(user)
        // No sincronizamos el User de la misma forma que las entidades de datos ya que Firebase Auth maneja el perfil.
        // Pero podríamos sincronizar campos adicionales si fuera necesario.
        return id
    }
    
    suspend fun updateUser(user: User) {
        val updatedUser = user.copy(updatedAt = System.currentTimeMillis())
        userDao.updateUser(updatedUser)
    }
    
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
}
