package com.example.rentapp.data.local

import android.content.Context
import android.util.Log
import com.example.rentapp.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class DataSeeder(private val db: AppDatabase) {

    suspend fun seedIfNeeded() {
        withContext(Dispatchers.IO) {
            val propertyCount = db.propertyDao().getPropertyCountSync()
            val hasDelayed = db.paymentDao().getDelayedPaymentsSync().isNotEmpty()
            
            if (propertyCount == 0) {
                Log.d("DataSeeder", "Database is empty, seeding initial data...")
                seedData()
            } else if (!hasDelayed) {
                Log.d("DataSeeder", "No delayed payments found, adding sample delayed data...")
                addDelayedSample()
            } else {
                Log.d("DataSeeder", "Database already has data and delayed payments, skipping seeding.")
            }
        }
    }

    private suspend fun addDelayedSample() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        
        // Buscar un contrato activo para asignarle el pago atrasado
        val contracts = db.contractDao().getAllContractsSync()
        if (contracts.isNotEmpty()) {
            val contract = contracts.first()
            db.paymentDao().insertPayment(Payment(
                contractId = contract.id,
                amount = contract.monthlyRent,
                dueDate = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 15), // Hace 15 días
                status = "DELAYED",
                month = currentMonth,
                year = currentYear,
                notes = "Pago de prueba atrasado forzado"
            ))
            Log.d("DataSeeder", "Forced delayed payment added to contract ${contract.id}")
        }
    }

    private suspend fun seedData() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) // 0-indexed

        // 1. Create Properties
        val property1 = Property(
            name = "Apartamento Vista Mar",
            address = "Av. Costanera 456, Suite 10B",
            type = "Departamento",
            monthlyRent = 1200.0,
            rooms = 3,
            bathrooms = 2,
            area = 95.0,
            status = "RENTED",
            description = "Hermoso departamento con vista panorámica al océano y acabados de lujo."
        )
        val p1Id = db.propertyDao().insertProperty(property1)

        val property2 = Property(
            name = "Local Comercial Centro",
            address = "Calle Real 123",
            type = "Local",
            monthlyRent = 2500.0,
            rooms = 1,
            bathrooms = 1,
            area = 150.0,
            status = "RENTED",
            description = "Local amplio en zona de alto tráfico peatonal."
        )
        val p2Id = db.propertyDao().insertProperty(property2)

        val property3 = Property(
            name = "Casa Familiar Las Lomas",
            address = "Los Olivos 789",
            type = "Casa",
            monthlyRent = 1800.0,
            rooms = 4,
            bathrooms = 3,
            area = 200.0,
            status = "AVAILABLE",
            description = "Espaciosa casa con jardín y garaje para dos autos."
        )
        db.propertyDao().insertProperty(property3)

        // 2. Create Tenants
        val tenant1 = Tenant(
            firstName = "Carlos",
            lastName = "Mendoza",
            email = "carlos.mendoza@example.com",
            phone = "+591 78945612",
            documentId = "1234567 LP",
            occupation = "Ingeniero de Sistemas",
            monthlyIncome = 4500.0,
            status = "ACTIVE"
        )
        val t1Id = db.tenantDao().insertTenant(tenant1)

        val tenant2 = Tenant(
            firstName = "Lucía",
            lastName = "García",
            email = "lucia.garcia@example.com",
            phone = "+591 65432109",
            documentId = "7654321 SC",
            occupation = "Propietaria de Negocio",
            monthlyIncome = 8000.0,
            status = "ACTIVE"
        )
        val t2Id = db.tenantDao().insertTenant(tenant2)

        // 3. Create Contracts
        val contract1 = Contract(
            propertyId = p1Id,
            tenantId = t1Id,
            startDate = calendar.timeInMillis - (1000L * 60 * 60 * 24 * 180), // 6 months ago
            endDate = calendar.timeInMillis + (1000L * 60 * 60 * 24 * 180), // 6 months from now
            monthlyRent = 1200.0,
            deposit = 1200.0,
            status = "ACTIVE"
        )
        val c1Id = db.contractDao().insertContract(contract1)

        val contract2 = Contract(
            propertyId = p2Id,
            tenantId = t2Id,
            startDate = calendar.timeInMillis - (1000L * 60 * 60 * 24 * 300), // 10 months ago
            endDate = calendar.timeInMillis + (1000L * 60 * 60 * 24 * 65), // 2 months from now
            monthlyRent = 2500.0,
            deposit = 5000.0,
            status = "ACTIVE"
        )
        val c2Id = db.contractDao().insertContract(contract2)

        // 4. Create Payments (Annual History)
        // Generar historial para los meses pasados
        for (m in 1 until currentMonth) { // Meses anteriores al actual: PAGADOS
            db.paymentDao().insertPayment(Payment(
                contractId = c1Id,
                amount = 1200.0,
                dueDate = getMonthDate(currentYear, m),
                paidDate = getMonthDate(currentYear, m) + (1000L * 60 * 60 * 24 * 2), // Pagado 2 días tarde
                status = "PAID",
                month = m,
                year = currentYear,
                paymentMethod = "Transferencia"
            ))

            db.paymentDao().insertPayment(Payment(
                contractId = c2Id,
                amount = 2500.0,
                dueDate = getMonthDate(currentYear, m),
                paidDate = getMonthDate(currentYear, m) + (1000L * 60 * 60 * 24 * 1), // Pagado 1 día tarde
                status = "PAID",
                month = m,
                year = currentYear,
                paymentMethod = "Efectivo"
            ))
        }

        // Casos específicos solicitados para el mes actual:
        
        // 1. RECIENTEMENTE FINALIZADO (Pagado en el mes actual)
        db.paymentDao().insertPayment(Payment(
            contractId = c1Id,
            amount = 1200.0,
            dueDate = getMonthDate(currentYear, currentMonth), // Vencía el 5 de este mes
            paidDate = System.currentTimeMillis() - (1000L * 60 * 60 * 24), // Pagado ayer
            status = "PAID",
            month = currentMonth,
            year = currentYear,
            paymentMethod = "Transferencia"
        ))

        // 2. ATRASADO (Delayed)
        // Forzamos una fecha de vencimiento en el pasado (hace 10 días)
        val tenDaysAgo = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 10)
        db.paymentDao().insertPayment(Payment(
            contractId = c2Id,
            amount = 2500.0,
            dueDate = tenDaysAgo, 
            status = "DELAYED",
            month = currentMonth,
            year = currentYear
        ))

        // También forzamos un PENDING vencido para que el Auditor lo detecte
        db.paymentDao().insertPayment(Payment(
            contractId = c1Id,
            amount = 1200.0,
            dueDate = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 2), // Venció hace 2 días
            status = "PENDING", // El worker lo cambiará a DELAYED
            month = currentMonth,
            year = currentYear
        ))

        // 3. PENDIENTE (Siguiente mes o futuro cercano)
        db.paymentDao().insertPayment(Payment(
            contractId = c1Id,
            amount = 1200.0,
            dueDate = getMonthDate(currentYear, currentMonth + 1), // Vence el 5 del próximo mes
            status = "PENDING",
            month = currentMonth + 1,
            year = currentYear
        ))
        
        // Datos para Reporte Anual (Pérdidas y Ganancias)
        // Añadimos algunos gastos simulados o pagos fallidos si el modelo lo permite, 
        // o simplemente variamos los ingresos para que se vean reflejados en las gráficas.
        
        Log.d("DataSeeder", "Data seeding completed successfully with status variations.")
    }

    private fun getMonthDate(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 5)
        cal.set(Calendar.HOUR_OF_DAY, 10)
        cal.set(Calendar.MINUTE, 0)
        return cal.timeInMillis
    }
}
