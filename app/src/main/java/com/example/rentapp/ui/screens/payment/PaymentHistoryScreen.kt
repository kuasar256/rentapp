package com.example.rentapp.ui.screens.payment

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PaymentViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    viewModel: PaymentViewModel,
    onBack: () -> Unit,
    onPaymentClick: (Long) -> Unit
) {
    val payments by viewModel.allPayments.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val yearPayments by viewModel.paymentsByYear.collectAsState()
    val totalCollected by viewModel.totalCollectedByYear.collectAsState()
    val currency = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    val years = (2022..2027).toList().reversed()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Pagos", color = OnBackground, fontWeight = FontWeight.Bold) },
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
            // Year selector
            ScrollableTabRow(
                selectedTabIndex = years.indexOf(selectedYear).coerceAtLeast(0),
                containerColor = SurfaceContainer,
                contentColor = Primary,
                edgePadding = 16.dp
            ) {
                years.forEachIndexed { index, year ->
                    Tab(
                        selected = year == selectedYear,
                        onClick = { viewModel.setSelectedYear(year) },
                        text = {
                            Text("$year", color = if (year == selectedYear) Primary else OnSurfaceVariant,
                                fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal)
                        }
                    )
                }
            }

            // Annual total
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Total Recaudado $selectedYear", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        Text(currency.format(totalCollected ?: 0.0), style = MaterialTheme.typography.headlineSmall,
                            color = Primary, fontWeight = FontWeight.Bold)
                        Text("${yearPayments.size} transacciones", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
            }

            if (yearPayments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Sin pagos en $selectedYear", color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(yearPayments) { payment ->
                        PaymentCard(payment = payment, onClick = { onPaymentClick(payment.id) })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}
