package com.example.rentapp.ui.screens.payment

import androidx.compose.foundation.*
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
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.ui.screens.property.EmptyState
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PaymentViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentListScreen(
    viewModel: PaymentViewModel,
    onPaymentClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val pendingPayments by viewModel.pendingPayments.collectAsState()
    val delayedPayments by viewModel.delayedPayments.collectAsState()
    val paidPayments by viewModel.paidPayments.collectAsState()
    val delayedCount by viewModel.delayedCount.collectAsState()

    val tabs = listOf("Pendiente", "Atrasado", "Finalizado")
    val currentList = when (selectedTab) { 0 -> pendingPayments; 1 -> delayedPayments; else -> paidPayments }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagos y Facturación", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Primary)
                    }
                },
                actions = {
                    if (delayedCount > 0) {
                        BadgedBox(badge = { Badge(containerColor = Error) { Text("$delayedCount") } }) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Error)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceContainer,
                contentColor = Primary,
                indicator = { tabPositions ->
                    SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = Primary)
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(title, color = if (selectedTab == index) Primary else OnSurfaceVariant, fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    )
                }
            }

            if (currentList.isEmpty()) {
                EmptyState(
                    message = when (selectedTab) {
                        0 -> "No hay pagos pendientes"
                        1 -> "No hay pagos atrasados 🎉"
                        else -> "No hay pagos finalizados"
                    },
                    icon = if (selectedTab == 1) Icons.Default.CheckCircle else Icons.Default.Payments
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList) { payment ->
                        PaymentCard(payment = payment, onClick = { onPaymentClick(payment.id) })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun PaymentCard(payment: Payment, onClick: () -> Unit) {
    val currency = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX"))
    val statusColor = when (payment.status) {
        "PAID" -> Primary; "DELAYED" -> Error; else -> Secondary
    }
    val statusIcon = when (payment.status) {
        "PAID" -> Icons.Default.CheckCircle; "DELAYED" -> Icons.Default.Warning; else -> Icons.Default.Schedule
    }
    val statusLabel = when (payment.status) {
        "PAID" -> "Pagado"; "DELAYED" -> "Atrasado"; else -> "Pendiente"
    }
    val monthNames = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(statusColor.copy(0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${monthNames.getOrElse(payment.month - 1) { "?" }} ${payment.year}",
                    color = OnBackground, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text("Vence: ${dateFormat.format(payment.dueDate)}", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                if (payment.paidDate != null) {
                    Text("Pagado: ${dateFormat.format(payment.paidDate)}", color = Primary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(currency.format(payment.amount), color = statusColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Surface(color = statusColor.copy(0.15f), shape = RoundedCornerShape(20.dp)) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
