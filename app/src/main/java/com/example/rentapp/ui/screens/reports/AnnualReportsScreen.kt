package com.example.rentapp.ui.screens.reports

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.local.entity.Property
import java.util.Calendar
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.example.rentapp.R
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PaymentViewModel
import com.example.rentapp.viewmodel.PropertyViewModel
import com.example.rentapp.viewmodel.ContractViewModel
import com.example.rentapp.ui.components.RentAppBottomBar
import com.example.rentapp.data.local.dao.MonthlyEarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnualReportsScreen(
    viewModel: PaymentViewModel,
    propertyViewModel: PropertyViewModel,
    contractViewModel: ContractViewModel,
    navController: NavHostController,
    onBack: () -> Unit
) {
    val selectedYear by viewModel.selectedYear.collectAsState()
    val totalCollected by viewModel.totalCollectedByYear.collectAsState(initial = 0.0)
    val currentlyRented by propertyViewModel.rentedCount.collectAsState()
    
    // Real data from ViewModel
    val monthlyEarnings by viewModel.monthlyEarningsByYear.collectAsState(initial = emptyList())
    val paidCount by viewModel.paidCountByYear.collectAsState(initial = 0)
    val pendingCount by viewModel.pendingCountByYear.collectAsState(initial = 0)
    val delayedCount by viewModel.delayedCountByYear.collectAsState(initial = 0)

    val allProperties by propertyViewModel.allProperties.collectAsState()
    val allContracts by contractViewModel.allContracts.collectAsState()
    
    val anticreticoCapital = remember(allProperties, allContracts, selectedYear) {
        val yearStart = Calendar.getInstance().apply { set(selectedYear, 0, 1, 0, 0, 0) }.timeInMillis
        val yearEnd = Calendar.getInstance().apply { set(selectedYear, 11, 31, 23, 59, 59) }.timeInMillis

        allContracts
            .filter { contract ->
                val prop = allProperties.find { it.id == contract.propertyId }
                val wasActiveInYear = contract.startDate <= yearEnd && (contract.endDate == 0L || contract.endDate >= yearStart)
                prop?.paymentType == "Anticrético" && wasActiveInYear
            }
            .sumOf { it.deposit }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val currency = remember(currentCurrency) {
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
                title = { Text(stringResource(R.string.annual_report_title), fontWeight = FontWeight.Bold, color = OnBackground) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Primary.copy(alpha = pulseAlpha), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.annual_report_summary).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                currency.format(totalCollected ?: 0.0),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = OnBackground
                            )
                            Text(
                                stringResource(R.string.annual_report_total_collected) + " ($selectedYear)",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                        
                        Surface(
                            color = Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                selectedYear.toString(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    Divider(color = OnSurface.copy(alpha = 0.05f))
                    Spacer(Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusMiniStat(
                            label = stringResource(R.string.status_paid),
                            count = paidCount,
                            color = Color(0xFF4CAF50) // Material Green
                        )
                        StatusMiniStat(
                            label = stringResource(R.string.status_pending),
                            count = pendingCount,
                            color = Color(0xFFFFC107) // Material Amber
                        )
                        StatusMiniStat(
                            label = stringResource(R.string.status_delayed),
                            count = delayedCount,
                            color = Color(0xFFF44336) // Material Red
                        )
                    }

                    if (anticreticoCapital > 0) {
                        Spacer(Modifier.height(20.dp))
                        Surface(
                            color = Tertiary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Tertiary)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Capital Anticrético",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Tertiary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            currency.format(anticreticoCapital),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = OnBackground
                                        )
                                    }
                                }
                                Surface(
                                    color = Tertiary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "ACTIVO",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Tertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Monthly Breakdown Section
            Text(
                stringResource(R.string.annual_report_monthly_breakdown),
                style = MaterialTheme.typography.titleMedium,
                color = OnBackground,
                fontWeight = FontWeight.Bold
            )

            val monthNames = listOf(
                R.string.month_jan, R.string.month_feb, R.string.month_mar,
                R.string.month_apr, R.string.month_may, R.string.month_jun,
                R.string.month_jul, R.string.month_aug, R.string.month_sep,
                R.string.month_oct, R.string.month_nov, R.string.month_dec
            )
            
            // Calculate max earning for progress bar scaling
            val maxEarning = remember(monthlyEarnings) { 
                monthlyEarnings.maxOfOrNull { it.total } ?: 1.0 
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(12) { index ->
                    val monthNumber = index + 1
                    val monthRes = monthNames[index]
                    val monthName = stringResource(monthRes)
                    
                    val earning = monthlyEarnings.find { it.month == monthNumber }?.total ?: 0.0
                    val progress = if (maxEarning > 0) (earning / maxEarning).toFloat() else 0f
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(if (earning > 0) Primary else OnSurfaceVariant.copy(alpha = 0.2f), androidx.compose.foundation.shape.CircleShape)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(monthName, style = MaterialTheme.typography.bodyLarge, color = OnBackground, fontWeight = FontWeight.Medium)
                                }
                                Text(
                                    currency.format(earning),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (earning > 0) Primary else OnSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            if (earning > 0) {
                                Spacer(Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = Primary,
                                    trackColor = Primary.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }

            // Export Button - "One touch report"
            Button(
                onClick = { /* Export PDF Logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = OnPrimaryFixed)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.annual_report_export_button), fontWeight = FontWeight.Bold, color = OnPrimaryFixed)
            }
        }
    }
}

@Composable
private fun StatusMiniStat(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(3.dp)
                .background(color.copy(alpha = 0.3f), RoundedCornerShape(50))
        )
    }
}

