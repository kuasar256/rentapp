package com.example.rentapp.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rentapp.data.local.AppDatabase
import com.example.rentapp.data.local.entity.Property
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PropertyDaoTest {

    private lateinit var propertyDao: PropertyDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        propertyDao = db.propertyDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writePropertyAndReadList() = runBlocking {
        val property = Property(
            id = 1,
            name = "Casa de Prueba",
            address = "Av. Siempre Viva 123",
            type = "Casa",
            monthlyRent = 1200.0,
            rooms = 4,
            bathrooms = 2,
            area = 150.0,
            status = "AVAILABLE"
        )
        propertyDao.insertProperty(property)
        
        val properties = propertyDao.getAllProperties().first()
        assertEquals(1, properties.size)
        assertEquals("Casa de Prueba", properties[0].name)
    }

    @Test
    fun getAvailableCount_isCorrect() = runBlocking {
        propertyDao.insertProperty(Property(name = "P1", address = "A1", type = "Casa", monthlyRent = 100.0, rooms = 1, bathrooms = 1, area = 10.0, status = "AVAILABLE"))
        propertyDao.insertProperty(Property(name = "P2", address = "A2", type = "Casa", monthlyRent = 100.0, rooms = 1, bathrooms = 1, area = 10.0, status = "RENTED"))
        propertyDao.insertProperty(Property(name = "P3", address = "A3", type = "Casa", monthlyRent = 100.0, rooms = 1, bathrooms = 1, area = 10.0, status = "AVAILABLE"))

        val availableCount = propertyDao.getAvailableCount().first()
        assertEquals(2, availableCount)
    }

    @Test
    fun updateProperty_updatesCorrectly() = runBlocking {
        val id = propertyDao.insertProperty(Property(name = "Original", address = "A1", type = "Casa", monthlyRent = 100.0, rooms = 1, bathrooms = 1, area = 10.0, status = "AVAILABLE"))
        val property = propertyDao.getPropertyById(id)!!
        
        val updated = property.copy(name = "Updated")
        propertyDao.updateProperty(updated)
        
        val result = propertyDao.getPropertyById(id)
        assertEquals("Updated", result?.name)
    }
}
