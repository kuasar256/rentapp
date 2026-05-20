package com.example.rentapp.ui.screens.budget

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.RepairBudget
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.RepairBudgetViewModel
import com.example.rentapp.viewmodel.PaymentViewModel
import com.example.rentapp.ui.components.RevenueDecayChart
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairBudgetListScreen(
    viewModel: RepairBudgetViewModel,
    paymentViewModel: PaymentViewModel,
    onBack: () -> Unit,
    onAddBudget: () -> Unit,
    onEditBudget: (Long, Long) -> Unit
) {
    val budgets by viewModel.allBudgets.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")
    val selectedYear by paymentViewModel.selectedYear.collectAsState()
    val totalCollected by paymentViewModel.totalCollectedByYear.collectAsState(initial = 0.0)

    val currencyFormatter = remember(currentCurrency) {
        val locale = when (currentCurrency) {
            "BOB" -> java.util.Locale("es", "BO")
            "MXN" -> java.util.Locale("es", "MX")
            else -> java.util.Locale.US
        }
        java.text.NumberFormat.getCurrencyInstance(locale).apply {
            try {
                this.currency = java.util.Currency.getInstance(currentCurrency)
            } catch (e: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.budget_manager), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBudget, containerColor = Primary, contentColor = OnPrimaryFixed) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        containerColor = Background
    ) { paddingValues ->
        if (budgets.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_contracts_msg), color = OnSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val totalBudgeted = budgets.sumOf { it.estimatedCost }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Tertiary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                "RESUMEN FINANCIERO ($selectedYear)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            RevenueDecayChart(
                                totalRevenue = totalCollected ?: 0.0,
                                repairCosts = totalBudgeted,
                                currencyFormatter = currencyFormatter
                            )
                        }
                    }
                }

                item {
                    Text(
                        stringResource(R.string.budget_manager),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(budgets) { budget ->
                    BudgetCard(budget, currencyFormatter, onClick = { onEditBudget(budget.propertyId, budget.id) })
                }
            }
        }
    }
}

@Composable
fun BudgetCard(budget: RepairBudget, formatter: NumberFormat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineVariant.copy(0.3f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(budget.description, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
                StatusBadge(budget.status)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.estimated_cost), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Text(formatter.format(budget.estimatedCost), style = MaterialTheme.typography.bodyMedium, color = Primary, fontWeight = FontWeight.Bold)
                }
                if (budget.actualCost != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.actual_cost), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Text(formatter.format(budget.actualCost), style = MaterialTheme.typography.bodyMedium, color = Tertiary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status) {
        "PENDING" -> Color(0xFFFFC107)
        "APPROVED" -> Color(0xFF4CAF50)
        "IN_PROGRESS" -> Primary
        "COMPLETED" -> Tertiary
        else -> OnSurfaceVariant
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
