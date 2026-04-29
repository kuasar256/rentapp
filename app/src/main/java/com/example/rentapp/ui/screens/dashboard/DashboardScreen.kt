package com.example.rentapp.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import androidx.compose.ui.res.stringResource
import com.example.rentapp.R
import com.example.rentapp.ui.navigation.Screen
import com.example.rentapp.ui.theme.*
import com.example.rentapp.ui.components.RentAppBottomBar
import com.example.rentapp.viewmodel.PaymentViewModel
import com.example.rentapp.viewmodel.PropertyViewModel
import com.example.rentapp.viewmodel.TenantViewModel
import com.example.rentapp.viewmodel.UserViewModel
import com.example.rentapp.viewmodel.ContractViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    propertyViewModel: PropertyViewModel,
    paymentViewModel: PaymentViewModel,
    tenantViewModel: TenantViewModel,
    contractViewModel: ContractViewModel,
    userViewModel: UserViewModel,
    navController: NavHostController
) {
    val userState by userViewModel.user.collectAsState()
    val userName = userState?.name ?: "Usuario"

    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "¡Buenos días"
            hour < 19 -> "¡Buenas tardes"
            else -> "¡Buenas noches"
        }
    }

    val totalCount by propertyViewModel.totalCount.collectAsState()
    val availableCount by propertyViewModel.availableCount.collectAsState()
    val rentedCount by propertyViewModel.rentedCount.collectAsState()
    val monthlyRevenue by propertyViewModel.totalMonthlyRevenue.collectAsState()
    val activeTenantsCount by tenantViewModel.activeCount.collectAsState()
    val delayedCount by paymentViewModel.delayedCount.collectAsState()
    val pendingCount by paymentViewModel.pendingCount.collectAsState()
    val delayedPayments by paymentViewModel.delayedPayments.collectAsState()
    val exchangeRates by propertyViewModel.exchangeRates.collectAsState()
    
    Log.d("RentAppDebug", "DashboardScreen: Composition started")
    
    // Remember formatters to avoid re-creation and add safety
    val currencyUsd = remember { NumberFormat.getCurrencyInstance(Locale("en", "US")) }
    val currencyBob = remember { 
        try {
            NumberFormat.getCurrencyInstance(Locale("es", "BO")).apply { 
                currency = java.util.Currency.getInstance("BOB") 
            }
        } catch (e: Exception) {
            Log.e("RentAppDebug", "Error initializing currencyBob: ${e.message}")
            NumberFormat.getCurrencyInstance(Locale("es", "BO")) // Fallback
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("$greeting, $userName!", color = OnBackground, fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.dashboard_subtitle), color = OnSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Screen.UserProfile.route) },
                        modifier = Modifier.background(SurfaceContainer, androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = Primary)
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Revenue Hero Card
            item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(listOf(Primary.copy(alpha = 0.5f), Color.Transparent)),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Primary.copy(alpha = 0.25f), Primary.copy(alpha = 0.05f), Color.Transparent)
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        stringResource(R.string.dashboard_monthly_revenue),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Primary,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 2.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Primary.copy(alpha = pulseAlpha), RoundedCornerShape(50))
                                    )
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                val baseRevenue = monthlyRevenue ?: 0.0
                                val bobRate = exchangeRates["BOB"] ?: 6.96
                                
                                Text(
                                    currencyUsd.format(baseRevenue),
                                    style = MaterialTheme.typography.displayMedium,
                                    color = OnBackground,
                                    fontWeight = FontWeight.Black
                                )
                                
                                Surface(
                                    modifier = Modifier.padding(top = 4.dp),
                                    color = Primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        " ≈ ${currencyBob.format(baseRevenue * bobRate)} ",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                
                                Spacer(Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null,
                                            tint = Primary, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("$rentedCount " + stringResource(R.string.dashboard_properties_rented), 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = OnSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

            // Stats Row
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.dashboard_properties),
                            value = totalCount.toString(),
                            icon = Icons.Default.Home,
                            accent = Primary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.dashboard_tenants),
                            value = activeTenantsCount.toString(),
                            icon = Icons.Default.People,
                            accent = Secondary
                        )
                    }
                }

            item {
                    val currentYearPaid by paymentViewModel.paidCountByYear.collectAsState()
                    val currentYearPending by paymentViewModel.pendingCountByYear.collectAsState()
                    val currentYearDelayed by paymentViewModel.delayedCountByYear.collectAsState()
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.dashboard_available),
                                value = availableCount.toString(),
                                icon = Icons.Default.CheckCircle,
                                accent = Tertiary,
                                showPulse = availableCount > 0
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.status_paid),
                                value = currentYearPaid.toString(),
                                icon = Icons.Default.Payments,
                                accent = Color(0xFF4CAF50) // Green
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.status_pending),
                                value = currentYearPending.toString(),
                                icon = Icons.Default.Schedule,
                                accent = Color(0xFFFFC107) // Amber
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                label = stringResource(R.string.status_delayed),
                                value = currentYearDelayed.toString(),
                                icon = Icons.Default.Warning,
                                accent = Error
                            )
                        }
                    }
                }

            // Acciones Rápidas
            item {
                Column {
                    Text(stringResource(R.string.dashboard_quick_actions), style = MaterialTheme.typography.titleMedium,
                        color = OnBackground, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                QuickActionButton(
                                    modifier = Modifier.weight(1f), label = stringResource(R.string.add_property),
                                    icon = Icons.Default.AddHome, onClick = { navController.navigate(Screen.AddProperty.route) }
                                )
                                QuickActionButton(
                                    modifier = Modifier.weight(1f), label = stringResource(R.string.add_tenant),
                                    icon = Icons.Default.PersonAdd, onClick = { navController.navigate(Screen.AddTenant.route) }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

            // Delayed payments alert
            if (delayedPayments.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = Error.copy(alpha = pulseAlpha),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { navController.navigate(Screen.DelinquencyAlerts.route) },
                        colors = CardDefaults.cardColors(containerColor = ErrorContainer.copy(alpha = 0.2f * pulseAlpha + 0.1f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.dashboard_delinquency_alerts), color = Error, fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleSmall)
                                Text(stringResource(R.string.dashboard_overdue_payments_warning, delayedPayments.size),
                                    color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Error)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    showPulse: Boolean = false
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                if (showPulse) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(accent.copy(alpha = 0.6f), RoundedCornerShape(50))
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = accent, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = OnBackground,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
        }
    }
}
