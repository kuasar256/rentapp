package com.example.rentapp.ui.screens.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.example.rentapp.ui.theme.*
import com.example.rentapp.ui.components.RentAppBottomBar
import com.example.rentapp.viewmodel.PaymentViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    viewModel: PaymentViewModel,
    navController: NavHostController,
    onBack: () -> Unit,
    onPaymentClick: (Long) -> Unit
) {
    val payments by viewModel.allPayments.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val yearPayments by viewModel.paymentsByYear.collectAsState()
    val totalCollected by viewModel.totalCollectedByYear.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")
    val currency = remember(currentCurrency) {
        val locale = when (currentCurrency) {
            "BOB" -> Locale("es", "BO")
            "MXN" -> Locale("es", "MX")
            else -> Locale("en", "US")
        }
        NumberFormat.getCurrencyInstance(locale).apply {
            try {
                this.currency = java.util.Currency.getInstance(currentCurrency)
            } catch (e: Exception) {}
        }
    }

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
        bottomBar = {
            RentAppBottomBar(navController = navController)
        },
        containerColor = Background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Horizontal Year Selector
            LazyRow(
                modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow).padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items<Int>(years) { year ->
                    val isSelected = year == selectedYear
                    Surface(
                        modifier = Modifier.clickable { viewModel.setSelectedYear(year) },
                        color = if (isSelected) Primary else SurfaceContainerHighest,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) null else BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = year.toString(),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            color = if (isSelected) OnPrimaryFixed else OnSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // High-tech Annual summary card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
            ) {
                Box(modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Primary.copy(alpha = 0.05f), Color.Transparent)))) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).background(Primary.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("TOTAL RECAUDADO EN $selectedYear", style = MaterialTheme.typography.labelSmall, 
                                color = Primary, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            Text(currency.format(totalCollected ?: 0.0), style = MaterialTheme.typography.headlineMedium,
                                color = OnBackground, fontWeight = FontWeight.Black)
                            Text("${yearPayments.size} Transacciones registradas", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                }
            }

            Text(
                "TRANSACCIONES RECIENTES",
                style = MaterialTheme.typography.labelLarge,
                color = OnSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (yearPayments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, contentDescription = null, tint = OnSurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Sin registros en $selectedYear", color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(yearPayments) { payment ->
                        PaymentCard(
                            payment = payment, 
                            onClick = { onPaymentClick(payment.id) }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}
