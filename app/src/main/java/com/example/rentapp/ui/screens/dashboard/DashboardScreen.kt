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
import com.example.rentapp.ui.navigation.Screen
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PaymentViewModel
import com.example.rentapp.viewmodel.PropertyViewModel
import com.example.rentapp.viewmodel.TenantViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    propertyViewModel: PropertyViewModel,
    paymentViewModel: PaymentViewModel,
    tenantViewModel: TenantViewModel,
    navController: NavHostController
) {
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
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel de Control", color = OnBackground, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge)
                        Text("Gestión de Propiedades", color = OnSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.UserProfile.route) }) {
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(Primary, PrimaryDim)),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Text("Ingresos Mensuales", style = MaterialTheme.typography.labelMedium,
                                color = OnPrimaryFixed.copy(alpha = 0.8f))
                            Spacer(Modifier.height(8.dp))
                            val baseRevenue = monthlyRevenue ?: 0.0
                            val bobRate = exchangeRates["BOB"] ?: 6.96 // Default fallback
                            
                            Text(
                                currencyUsd.format(baseRevenue),
                                style = MaterialTheme.typography.headlineLarge,
                                color = OnPrimaryFixed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "≈ ${currencyBob.format(baseRevenue * bobRate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnPrimaryFixed.copy(alpha = 0.9f)
                            )
                            
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null,
                                    tint = OnPrimaryFixed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("$rentedCount propiedades rentadas", style = MaterialTheme.typography.bodySmall,
                                    color = OnPrimaryFixed.copy(alpha = 0.8f))
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
                        label = "Propiedades",
                        value = totalCount.toString(),
                        icon = Icons.Default.Home,
                        accent = Primary
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Inquilinos",
                        value = activeTenantsCount.toString(),
                        icon = Icons.Default.People,
                        accent = Secondary
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Disponibles",
                        value = availableCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        accent = Tertiary,
                        showPulse = availableCount > 0,
                        pulseAlpha = pulseAlpha
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Atrasados",
                        value = (delayedCount + pendingCount).toString(),
                        icon = Icons.Default.Warning,
                        accent = Error
                    )
                }
            }

            // Quick Actions
            item {
                Text("Acciones Rápidas", style = MaterialTheme.typography.titleMedium,
                    color = OnBackground, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f), label = "Agregar\nVivienda",
                        icon = Icons.Default.AddHome, onClick = { navController.navigate(Screen.AddProperty.route) }
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f), label = "Agregar\nInquilino",
                        icon = Icons.Default.PersonAdd, onClick = { navController.navigate(Screen.AddTenant.route) }
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f), label = "Ver\nReportes",
                        icon = Icons.Default.BarChart, onClick = { navController.navigate(Screen.AnnualReports.route) }
                    )
                }
            }

            // Delayed payments alert
            if (delayedPayments.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Screen.DelinquencyAlerts.route) },
                        colors = CardDefaults.cardColors(containerColor = ErrorContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Alertas de Morosidad", color = Error, fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleSmall)
                                Text("${delayedPayments.size} pagos con retraso requieren atención",
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
    showPulse: Boolean = false,
    pulseAlpha: Float = 1f
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                if (showPulse) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(accent.copy(alpha = pulseAlpha), RoundedCornerShape(50))
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
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnBackground,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun RentAppBottomBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    NavigationBar(containerColor = SurfaceContainer) {
        NavigationBarItem(
            selected = currentRoute == Screen.Dashboard.route,
            onClick = { navController.navigate(Screen.Dashboard.route) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OnPrimaryFixed,
                indicatorColor = Primary,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnSurfaceVariant,
                selectedTextColor = Primary
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.PropertyList.route,
            onClick = { navController.navigate(Screen.PropertyList.route) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Domain, contentDescription = null) },
            label = { Text("Propiedades") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OnPrimaryFixed,
                indicatorColor = Primary,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnSurfaceVariant,
                selectedTextColor = Primary
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.TenantList.route,
            onClick = { navController.navigate(Screen.TenantList.route) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.People, contentDescription = null) },
            label = { Text("Inquilinos") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OnPrimaryFixed,
                indicatorColor = Primary,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnSurfaceVariant,
                selectedTextColor = Primary
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.PaymentList.route,
            onClick = { navController.navigate(Screen.PaymentList.route) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Payments, contentDescription = null) },
            label = { Text("Pagos") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OnPrimaryFixed,
                indicatorColor = Primary,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnSurfaceVariant,
                selectedTextColor = Primary
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.AnnualReports.route,
            onClick = { navController.navigate(Screen.AnnualReports.route) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text("Reportes") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OnPrimaryFixed,
                indicatorColor = Primary,
                unselectedIconColor = OnSurfaceVariant,
                unselectedTextColor = OnSurfaceVariant,
                selectedTextColor = Primary
            )
        )
    }
}
