package com.example.rentapp.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rentapp.data.local.AppDatabase
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.data.local.entity.Tenant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PaymentDaoTest {

    private lateinit var paymentDao: PaymentDao
    private lateinit var contractDao: ContractDao
    private lateinit var propertyDao: PropertyDao
    private lateinit var tenantDao: TenantDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        paymentDao = db.paymentDao()
        contractDao = db.contractDao()
        propertyDao = db.propertyDao()
        tenantDao = db.tenantDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetPayments() = runBlocking {
        // Prepare required entities due to Foreign Keys
        val propId = propertyDao.insertProperty(Property(name = "P1", address = "A1", type = "Casa", monthlyRent = 1000.0, rooms = 1, bathrooms = 1, area = 1.0, status = "AVAILABLE"))
        val tenantId = tenantDao.insertTenant(Tenant(name = "T1", email = "t@t.com", phone = "123"))
        val contractId = contractDao.insertContract(Contract(propertyId = propId, tenantId = tenantId, startDate = 0L, endDate = 1000L, monthlyRent = 1000.0, deposit = 1000.0))

        val payment = Payment(
            contractId = contractId,
            amount = 1000.0,
            dueDate = 2000L,
            status = "PENDING",
            month = 5,
            year = 2024
        )
        paymentDao.insertPayment(payment)

        val payments = paymentDao.getAllPayments().first()
        assertEquals(1, payments.size)
        assertEquals(1000.0, payments[0].amount, 0.1)
        assertEquals("PENDING", payments[0].status)
    }

    @Test
    fun getPendingPaymentsSync_returnsOnlyPending() = runBlocking {
        val pId = propertyDao.insertProperty(Property(name = "P", address = "A", type = "Casa", monthlyRent = 100.0, rooms = 1, bathrooms = 1, area = 1.0, status = "AVAILABLE"))
        val tId = tenantDao.insertTenant(Tenant(name = "T", email = "e", phone = "p"))
        val cId = contractDao.insertContract(Contract(propertyId = pId, tenantId = tId, startDate = 0, endDate = 100, monthlyRent = 100.0, deposit = 100.0))

        paymentDao.insertPayment(Payment(contractId = cId, amount = 10.0, dueDate = 1, status = "PENDING", month = 1, year = 2024))
        paymentDao.insertPayment(Payment(contractId = cId, amount = 10.0, dueDate = 1, status = "PAID", month = 2, year = 2024))
        paymentDao.insertPayment(Payment(contractId = cId, amount = 10.0, dueDate = 1, status = "DELAYED", month = 3, year = 2024))

        val pending = paymentDao.getPendingPaymentsSync()
        // PENDING y DELAYED suelen considerarse no pagados, pero la query específica en DAO suele ser por string.
        // Asumiendo que getPendingPaymentsSync busca status == "PENDING"
        assertEquals(1, pending.size)
        assertEquals(1, pending[0].month)
    }
}
