package com.example.rentapp.ui.screens.property

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PropertyViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListScreen(
    viewModel: PropertyViewModel,
    onPropertyClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onBack: () -> Unit
) {
    val allProperties by viewModel.allProperties.collectAsState()
    var selectedFilter by remember { mutableStateOf("TODOS") }

    val filteredProperties = when (selectedFilter) {
        "AVAILABLE" -> allProperties.filter { it.status == "AVAILABLE" }
        "RENTED" -> allProperties.filter { it.status == "RENTED" }
        "MAINTENANCE" -> allProperties.filter { it.status == "MAINTENANCE" }
        else -> allProperties
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Propiedades", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick, containerColor = Primary) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Propiedad", tint = OnPrimaryFixed)
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Filter chips
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("TODOS", "AVAILABLE", "RENTED", "MAINTENANCE").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(when(filter) {
                                "TODOS" -> "Todos"
                                "AVAILABLE" -> "Disponibles"
                                "RENTED" -> "Rentados"
                                "MAINTENANCE" -> "Mantenimiento"
                                else -> filter
                            }, fontSize = 12.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = OnPrimaryFixed,
                            containerColor = SurfaceContainerHighest,
                            labelColor = OnSurfaceVariant
                        )
                    )
                }
            }

            if (filteredProperties.isEmpty()) {
                EmptyState(message = "No hay propiedades registradas.\nToca + para agregar una.", icon = Icons.Default.Home)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProperties) { property ->
                        PropertyCard(property = property, onClick = { onPropertyClick(property.id) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun PropertyCard(property: Property, onClick: () -> Unit) {
    val currency = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val statusColor = when (property.status) {
        "AVAILABLE" -> Primary
        "RENTED" -> Tertiary
        "MAINTENANCE" -> Error
        else -> OnSurfaceVariant
    }
    val statusLabel = when (property.status) {
        "AVAILABLE" -> "Disponible"
        "RENTED" -> "Rentado"
        "MAINTENANCE" -> "Mantenimiento"
        else -> property.status
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(property.type, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                }
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(property.name, style = MaterialTheme.typography.titleMedium, color = OnBackground, fontWeight = FontWeight.SemiBold)
            Text(property.address, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(currency.format(property.monthlyRent), style = MaterialTheme.typography.titleMedium,
                    color = Primary, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PropertyStat(icon = Icons.Default.Bed, value = "${property.rooms}")
                    PropertyStat(icon = Icons.Default.Bathroom, value = "${property.bathrooms}")
                    PropertyStat(icon = Icons.Default.SquareFoot, value = "${property.area.toInt()}m²")
                }
            }
        }
    }
}

@Composable
private fun PropertyStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
        Text(value, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
fun EmptyState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = OutlineVariant, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
