package com.example.rentapp.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.repository.ContractRepository
import com.example.rentapp.data.repository.PropertyRepository
import com.example.rentapp.data.repository.TenantRepository
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PaymentViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelinquencyAlertsScreen(
    viewModel: PaymentViewModel,
    propertyRepo: PropertyRepository,
    contractRepo: ContractRepository,
    tenantRepo: TenantRepository,
    onBack: () -> Unit,
    onPaymentClick: (Long) -> Unit
) {
    val delayedPayments by viewModel.delayedPayments.collectAsState()
    val contracts by contractRepo.getAllContracts().collectAsState(emptyList())
    val allProperties by propertyRepo.getAllProperties().collectAsState(emptyList())
    val allTenants by tenantRepo.getAllTenants().collectAsState(emptyList())
    val currency = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas de Morosidad", color = OnBackground, fontWeight = FontWeight.Bold) },
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
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Summary banner
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(ErrorContainer.copy(0.25f))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("${delayedPayments.size} Pagos Atrasados", color = Error,
                            fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Total: ${currency.format(delayedPayments.sumOf { it.amount })}",
                            color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (delayedPayments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("¡Sin morosos! 🎉", color = Primary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Todos los pagos están al corriente.", color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(delayedPayments) { payment ->
                        val contract = contracts.find { it.id == payment.contractId }
                        val property = allProperties.find { it.id == contract?.propertyId }
                        val tenant = allTenants.find { it.id == contract?.tenantId }
                        val daysDelayed = ((System.currentTimeMillis() - payment.dueDate) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)

                        DelinquencyCard(
                            payment = payment, property = property, tenant = tenant,
                            daysDelayed = daysDelayed, currency = currency,
                            onMarkPaid = { viewModel.markAsPaid(payment) },
                            onClick = { onPaymentClick(payment.id) }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DelinquencyCard(
    payment: Payment, property: Property?, tenant: Tenant?,
    daysDelayed: Int, currency: NumberFormat,
    onMarkPaid: () -> Unit, onClick: () -> Unit
) {
    val severity = when {
        daysDelayed > 60 -> Error
        daysDelayed > 30 -> Color(0xFFFF9800)
        else -> Secondary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = severity, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("$daysDelayed días de retraso", color = severity, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                Text(currency.format(payment.amount), color = Error, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            if (property != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(property.name, color = OnBackground, style = MaterialTheme.typography.bodySmall)
                }
            }
            if (tenant != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Secondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${tenant.firstName} ${tenant.lastName}", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(8.dp))
                    Text(tenant.phone, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onMarkPaid,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Marcar como Pagado", color = OnPrimaryFixed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

private val Color.Companion.orange get() = Color(0xFFFF9800)
