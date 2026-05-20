package com.example.rentapp.ui.screens.property

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.Expense
import com.example.rentapp.ui.components.EmptyState
import com.example.rentapp.ui.components.RentAppBottomBar
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit,
    onAddExpense: () -> Unit,
    navController: androidx.navigation.NavHostController
) {
    val expenses by viewModel.allExpenses.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")
    
    val currencyFormatter = remember(currentCurrency) {
        val locale = when (currentCurrency) {
            "BOB" -> Locale("es", "BO")
            "MXN" -> Locale("es", "MX")
            else -> Locale.US
        }
        NumberFormat.getCurrencyInstance(locale).apply {
            try {
                this.currency = Currency.getInstance(currentCurrency)
            } catch (e: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.expenses_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = Primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        bottomBar = {
            RentAppBottomBar(navController = navController)
        },
        containerColor = Background
    ) { paddingValues ->
        if (expenses.isEmpty()) {
            EmptyState(
                message = stringResource(R.string.empty_expenses_title),
                description = stringResource(R.string.empty_expenses_desc),
                icon = Icons.Default.ReceiptLong,
                actionLabel = stringResource(R.string.add_expense),
                onActionClick = onAddExpense,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(expenses) { expense ->
                    ExpenseCard(expense, currencyFormatter)
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ExpenseCard(expense: Expense, formatter: NumberFormat) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(expense.category) {
                        "Mantenimiento" -> Icons.Default.Build
                        "Impuestos" -> Icons.Default.Description
                        "Servicios" -> Icons.Default.FlashOn
                        "Seguro" -> Icons.Default.Shield
                        else -> Icons.Default.Category
                    },
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    expense.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Text(
                    dateFormat.format(Date(expense.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            
            Text(
                formatter.format(expense.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Black,
                color = Error
            )
        }
    }
}
