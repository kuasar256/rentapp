package com.example.rentapp.ui.screens.property

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.ui.theme.*
import com.example.rentapp.ui.components.RentAppBottomBar
import com.example.rentapp.viewmodel.PropertyViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListScreen(
    viewModel: PropertyViewModel,
    navController: NavHostController,
    onPropertyClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onBack: () -> Unit
) {
    val allProperties by viewModel.allProperties.collectAsState()
    var selectedFilter by remember { mutableStateOf("TODOS") }

    val filteredProperties = when (selectedFilter) {
        "AVAILABLE"   -> allProperties.filter { it.status == "AVAILABLE" }
        "RENTED"      -> allProperties.filter { it.status == "RENTED" }
        "MAINTENANCE" -> allProperties.filter { it.status == "MAINTENANCE" }
        else          -> allProperties
    }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var propertyToDelete by remember { mutableStateOf<Property?>(null) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceContainer,
                    contentColor = OnBackground,
                    actionColor = Primary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.properties_list_title), 
                        color = OnBackground, 
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cancel), tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.AddHome, contentDescription = "Agregar Propiedad", tint = Primary)
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
            // Tactical Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("TODOS", "AVAILABLE", "RENTED", "MAINTENANCE")
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val label = when (filter) {
                        "TODOS"       -> stringResource(R.string.all_filter)
                        "AVAILABLE"   -> stringResource(R.string.available_filter)
                        "RENTED"      -> stringResource(R.string.rented_filter)
                        "MAINTENANCE" -> stringResource(R.string.maintenance_filter)
                        else          -> filter
                    }
                    
                    Surface(
                        onClick = { selectedFilter = filter },
                        color = if (isSelected) Primary.copy(alpha = 0.2f) else SurfaceContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp, 
                            if (isSelected) Primary else Color.Transparent
                        )
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (isSelected) Primary else OnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            if (filteredProperties.isEmpty()) {
                EmptyState(
                    message = stringResource(R.string.no_properties_msg),
                    icon = Icons.Default.HomeWork
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredProperties, key = { it.id }) { property ->
                        SwipeToDeletePropertyCard(
                            property = property,
                            onClick = { onPropertyClick(property.id) },
                            onDeleteRequested = { propertyToDelete = property }
                        )
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }

    // ── Confirm deletion dialog ──────────────────────────────────────────────
    propertyToDelete?.let { property ->
        AlertDialog(
            onDismissRequest = { propertyToDelete = null },
            containerColor = SurfaceContainer,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            icon = {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    stringResource(R.string.delete_property_title),
                    color = OnBackground,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    stringResource(R.string.delete_property_confirm, property.name),
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deleted = property
                        propertyToDelete = null
                        viewModel.deleteProperty(deleted)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = context.getString(R.string.property_deleted_msg, deleted.name),
                                actionLabel = context.getString(R.string.undo),
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.insertProperty(deleted.copy(id = 0L))
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.delete), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { propertyToDelete = null }) {
                    Text(stringResource(R.string.cancel), color = Primary)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeletePropertyCard(
    property: Property,
    onClick: () -> Unit,
    onDeleteRequested: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequested()
            }
            false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            PropertyDeleteBackground(dismissState.targetValue)
        }
    ) {
        PropertyCard(property = property, onClick = onClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyDeleteBackground(targetValue: SwipeToDismissBoxValue) {
    val active = targetValue == SwipeToDismissBoxValue.EndToStart
    val bgColor = if (active) Error else Error.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            modifier = Modifier.padding(end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.delete),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun PropertyCard(property: Property, onClick: () -> Unit) {
    val context = LocalContext.current
    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")
    val currencyFormatter = remember(currentCurrency) {
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

    val (statusColor, statusLabel) = when (property.status) {
        "AVAILABLE"   -> Primary to stringResource(R.string.available_filter)
        "RENTED"      -> Tertiary to stringResource(R.string.rented_filter)
        "MAINTENANCE" -> Error to stringResource(R.string.maintenance_filter)
        else          -> OnSurfaceVariant to property.status
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box {
                if (property.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(property.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Property Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(SurfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image, 
                            contentDescription = null, 
                            modifier = Modifier.size(48.dp),
                            tint = OnSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }
                
                // Status Badge Overlay
                Surface(
                    color = statusColor.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        statusLabel.uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Background,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            property.name, 
                            style = MaterialTheme.typography.titleLarge, 
                            color = OnBackground, 
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Tertiary, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                property.address, 
                                style = MaterialTheme.typography.bodySmall, 
                                color = OnSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    Text(
                        currencyFormatter.format(property.monthlyRent),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Primary,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    PropertyStat(icon = Icons.Default.Bed, value = "${property.rooms}", label = "Beds")
                    PropertyStat(icon = Icons.Default.Shower, value = "${property.bathrooms}", label = "Baths")
                    PropertyStat(icon = Icons.Default.SquareFoot, value = "${property.area.toInt()} m²", label = "Area")
                }

                Spacer(Modifier.height(16.dp))
                
                // Tactical Action Button
                Surface(
                    onClick = {
                        val gmmIntentUri = android.net.Uri.parse("geo:${property.latitude},${property.longitude}?q=${property.latitude},${property.longitude}(${property.name})")
                        val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.view_location_btn), 
                            style = MaterialTheme.typography.labelLarge, 
                            color = Primary, 
                            fontWeight = FontWeight.ExtraBold, 
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = OnBackground, fontWeight = FontWeight.Bold)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, fontSize = 8.sp)
    }
}

@Composable
fun EmptyState(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Primary.copy(alpha = 0.05f), RoundedCornerShape(60.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Primary.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            message,
            style = MaterialTheme.typography.titleMedium,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
