package com.example.rentapp.ui.screens.property

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.data.repository.ContractRepository
import com.example.rentapp.data.repository.PaymentRepository
import com.example.rentapp.data.repository.TenantRepository
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PropertyViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    propertyId: Long,
    viewModel: PropertyViewModel,
    contractRepo: ContractRepository,
    tenantRepo: TenantRepository,
    paymentRepo: PaymentRepository,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onPaymentClick: (Long) -> Unit
) {
    val property by remember { derivedStateOf { viewModel.allProperties.value.find { it.id == propertyId } } }
    val contracts by contractRepo.getContractsByProperty(propertyId).collectAsState(emptyList())
    val currency = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
    var showDeleteDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    property?.let { viewModel.deleteProperty(it) }
                    showDeleteDialog = false
                    onBack()
                }) { Text("Eliminar", color = Error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } },
            title = { Text("Eliminar Propiedad", color = OnBackground) },
            text = { Text("¿Estás seguro que deseas eliminar esta propiedad?", color = OnSurfaceVariant) },
            containerColor = SurfaceContainer
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Vivienda", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Primary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        if (property == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            val prop = property!!
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Section
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                Brush.linearGradient(listOf(SurfaceContainer, SurfaceContainerHigh)),
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = Primary, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(prop.type, color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        val statusColor = if (prop.status == "AVAILABLE") Primary else if (prop.status == "RENTED") Tertiary else Error
                        val statusLabel = when(prop.status) { "AVAILABLE" -> "Disponible"; "RENTED" -> "Rentado"; else -> "Mantenimiento" }
                        Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                            Surface(color = statusColor.copy(0.2f), shape = RoundedCornerShape(20.dp)) {
                                Text(statusLabel, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = statusColor, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Name and price
                item {
                    Text(prop.name, style = MaterialTheme.typography.headlineSmall, color = OnBackground, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(prop.address, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(currency.format(prop.monthlyRent), style = MaterialTheme.typography.headlineMedium,
                        color = Primary, fontWeight = FontWeight.Bold)
                    Text("/ mes", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }

                // Specs
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = SurfaceContainer), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            SpecItem(icon = Icons.Default.Bed, label = "Recámaras", value = "${prop.rooms}")
                            SpecItem(icon = Icons.Default.Bathroom, label = "Baños", value = "${prop.bathrooms}")
                            SpecItem(icon = Icons.Default.SquareFoot, label = "Área", value = "${prop.area.toInt()} m²")
                        }
                    }
                }

                // Description
                if (prop.description.isNotBlank()) {
                    item {
                        Text("Descripción", style = MaterialTheme.typography.titleSmall, color = OnSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(prop.description, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                    }
                }

                // Contracts
                if (contracts.isNotEmpty()) {
                    item { Text("Contratos Activos", style = MaterialTheme.typography.titleMedium, color = OnBackground, fontWeight = FontWeight.SemiBold) }
                    items(contracts) { contract ->
                        ContractItem(contract = contract, dateFormat = dateFormat, currency = currency)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = OnBackground, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
private fun ContractItem(contract: Contract, dateFormat: SimpleDateFormat, currency: NumberFormat) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, contentDescription = null, tint = Tertiary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Contrato #${contract.id}", color = OnBackground, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text("${dateFormat.format(contract.startDate)} → ${dateFormat.format(contract.endDate)}",
                    color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(currency.format(contract.monthlyRent), color = Primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Surface(color = if(contract.status == "ACTIVE") Primary.copy(0.15f) else OutlineVariant.copy(0.3f),
                    shape = RoundedCornerShape(20.dp)) {
                    Text(if(contract.status == "ACTIVE") "Activo" else contract.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = if(contract.status == "ACTIVE") Primary else OnSurfaceVariant, fontSize = 10.sp)
                }
            }
        }
    }
}
