package com.example.rentapp.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.data.repository.ContractRepository
import com.example.rentapp.data.repository.PropertyRepository
import com.example.rentapp.data.repository.TenantRepository
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PaymentViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailScreen(
    paymentId: Long,
    viewModel: PaymentViewModel,
    contractRepo: ContractRepository,
    propertyRepo: PropertyRepository,
    tenantRepo: TenantRepository,
    onBack: () -> Unit
) {
    val allPayments by viewModel.allPayments.collectAsState()
    val payment = allPayments.find { it.id == paymentId }
    val contract by contractRepo.getAllContracts().collectAsState(emptyList())
    val currentContract = contract.find { it.id == payment?.contractId }
    val allProperties by propertyRepo.getAllProperties().collectAsState(emptyList())
    val allTenants by tenantRepo.getAllTenants().collectAsState(emptyList())
    val property = allProperties.find { it.id == currentContract?.propertyId }
    val tenant = allTenants.find { it.id == currentContract?.tenantId }

    val context = androidx.compose.ui.platform.LocalContext.current
    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")
    val currencyFormatter = remember(currentCurrency) {
        val validCurrency = try {
            java.util.Currency.getInstance(currentCurrency)
            currentCurrency
        } catch (e: Exception) {
            "USD"
        }
        NumberFormat.getCurrencyInstance().apply { 
            currency = java.util.Currency.getInstance(validCurrency)
        }
    }
    val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault())
    val monthNames = listOf("Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Pago", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        if (payment == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        val statusColor = when (payment.status) { "PAID" -> Primary; "DELAYED" -> Error; else -> Secondary }
        val statusLabel = when (payment.status) { "PAID" -> "Finalizado"; "DELAYED" -> "Atrasado"; else -> "Pendiente" }
        val statusIcon = when (payment.status) { "PAID" -> Icons.Default.CheckCircle; "DELAYED" -> Icons.Default.Warning; else -> Icons.Default.Schedule }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Status Hero
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().background(statusColor.copy(0.1f), RoundedCornerShape(20.dp)).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(statusLabel, style = MaterialTheme.typography.headlineMedium, color = statusColor, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("${monthNames.getOrElse(payment.month - 1){"?"}} ${payment.year}",
                            style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Text(currencyFormatter.format(payment.amount), style = MaterialTheme.typography.displaySmall,
                            color = OnBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Details card
            item {
                Card(colors = CardDefaults.cardColors(containerColor = SurfaceContainer), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Detalles del Pago", style = MaterialTheme.typography.titleMedium, color = OnBackground, fontWeight = FontWeight.SemiBold)
                        DetailRow("Fecha de Vencimiento", dateFormat.format(payment.dueDate))
                        if (payment.paidDate != null) DetailRow("Fecha de Pago", dateFormat.format(payment.paidDate))
                        if (payment.paymentMethod.isNotBlank()) DetailRow("Método de Pago", payment.paymentMethod)
                        if (payment.receiptNumber.isNotBlank()) DetailRow("Número de Recibo", "#${payment.receiptNumber}")
                    }
                }
            }

            // Property info
            if (property != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = SurfaceContainer), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Propiedad", style = MaterialTheme.typography.titleSmall, color = OnBackground, fontWeight = FontWeight.SemiBold)
                            }
                            Text(property.name, color = Primary, style = MaterialTheme.typography.bodyMedium)
                            Text(property.address, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Tenant info
            if (tenant != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = SurfaceContainer), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Inquilino", style = MaterialTheme.typography.titleSmall, color = OnBackground, fontWeight = FontWeight.SemiBold)
                            }
                            Text("${tenant.firstName} ${tenant.lastName}", color = Secondary, style = MaterialTheme.typography.bodyMedium)
                            Text(tenant.phone, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Actions
            if (payment.status != "PAID") {
                item {
                    Button(
                        onClick = { viewModel.markAsPaid(payment) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OnPrimaryFixed)
                        Spacer(Modifier.width(8.dp))
                        Text("Marcar como Pagado", color = OnPrimaryFixed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            if (payment.status == "PENDING") {
                item {
                    OutlinedButton(
                        onClick = { viewModel.markAsDelayed(payment) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Error.copy(0.4f))
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Error)
                        Spacer(Modifier.width(8.dp))
                        Text("Marcar como Atrasado", color = Error, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(value, color = OnBackground, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
