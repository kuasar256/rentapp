package com.example.rentapp.ui.screens.payment

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.data.repository.ContractRepository
import com.example.rentapp.data.repository.PropertyRepository
import com.example.rentapp.data.repository.TenantRepository
import com.example.rentapp.ui.components.RentAppBottomBar
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PaymentViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelinquencyAlertsScreen(
    viewModel: PaymentViewModel,
    propertyRepo: PropertyRepository,
    contractRepo: ContractRepository,
    tenantRepo: TenantRepository,
    navController: NavHostController,
    onBack: () -> Unit,
    onPaymentClick: (Long) -> Unit
) {
    val delayedPayments by viewModel.delayedPayments.collectAsState()
    val contracts by contractRepo.getAllContracts().collectAsState(emptyList())
    val allProperties by propertyRepo.getAllProperties().collectAsState(emptyList())
    val allTenants by tenantRepo.getAllTenants().collectAsState(emptyList())
    val context = LocalContext.current
    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val currencyFormatter = remember(currentCurrency) {
        val locale = when (currentCurrency) {
            "BOB" -> Locale("es", "BO")
            "MXN" -> Locale("es", "MX")
            else -> Locale.US
        }
        java.text.NumberFormat.getCurrencyInstance(locale).apply {
            try {
                this.currency = java.util.Currency.getInstance(currentCurrency ?: "USD")
            } catch (e: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.delinquency_title), color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            RentAppBottomBar(navController = navController)
        },
        containerColor = Background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Summary banner with pulse
            if (delayedPayments.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(com.example.rentapp.ui.theme.Error.copy(alpha = 0.1f * pulseAlpha))
                        .border(1.dp, com.example.rentapp.ui.theme.Error.copy(alpha = 0.3f * pulseAlpha), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(com.example.rentapp.ui.theme.Error.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = com.example.rentapp.ui.theme.Error, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                stringResource(R.string.delinquency_overdue_payments, delayedPayments.size),
                                color = com.example.rentapp.ui.theme.Error,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleMedium,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "${stringResource(R.string.delinquency_total_debt)}: ${currencyFormatter.format(delayedPayments.sumOf { it.amount })}",
                                color = OnSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            if (delayedPayments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(24.dp))
                    Text(stringResource(R.string.delinquency_empty_title), color = Primary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.delinquency_empty_msg), color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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
                            daysDelayed = daysDelayed, currency = currencyFormatter,
                            pulseAlpha = if (daysDelayed > 60) pulseAlpha else 1f,
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
    daysDelayed: Int, currency: java.text.NumberFormat,
    pulseAlpha: Float,
    onMarkPaid: () -> Unit, onClick: () -> Unit
) {
    val severityColor = when {
        daysDelayed > 60 -> com.example.rentapp.ui.theme.Error
        daysDelayed > 30 -> Color(0xFFFF9800)
        else -> Secondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (daysDelayed > 60) 1.dp else 0.dp,
                color = if (daysDelayed > 60) com.example.rentapp.ui.theme.Error.copy(alpha = pulseAlpha) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = severityColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.delinquency_days_overdue, daysDelayed),
                    color = severityColor,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.weight(1f))
                Text(
                    currency.format(payment.amount),
                    color = com.example.rentapp.ui.theme.Error,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = OnSurface.copy(alpha = 0.05f))
            Spacer(Modifier.height(12.dp))

            if (property != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(property.name, color = OnBackground, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(4.dp))
            if (tenant != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Secondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("${tenant.firstName} ${tenant.lastName}", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Text(tenant.phone, color = OnSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onMarkPaid,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(stringResource(R.string.delinquency_mark_paid), color = OnPrimaryFixed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
