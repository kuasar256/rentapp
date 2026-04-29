package com.example.rentapp.ui.screens.property

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.repository.ContractRepository
import com.example.rentapp.data.repository.PaymentRepository
import com.example.rentapp.data.repository.TenantRepository
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PropertyViewModel
import java.io.File
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
    onRentProperty: (Long) -> Unit,
    onAddContractClick: () -> Unit,
    onContractClick: (Long) -> Unit,
    onAddPaymentClick: (Long) -> Unit,
    onPaymentClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")
    val formatter = remember(currentCurrency) {
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
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Get the property from the ViewModel
    val properties by viewModel.allProperties.collectAsState(initial = emptyList())
    val property = remember(properties, propertyId) { properties.find { it.id == propertyId } }
    var contracts by remember { mutableStateOf<List<Contract>>(emptyList()) }

    LaunchedEffect(propertyId) {
        contractRepo.getContractsByProperty(propertyId).collect {
            contracts = it
        }
    }

    if (showDeleteDialog && property != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    property.let { viewModel.deleteProperty(it) }
                    showDeleteDialog = false
                    onBack()
                }) { Text(stringResource(R.string.delete), color = Error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.cancel), color = Primary) } },
            title = { Text(stringResource(R.string.delete_property_title), color = OnBackground, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_property_confirm, property.name), color = OnSurfaceVariant) },
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.property_detail_title), color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = Primary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Error)
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
            val prop = property
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Hero Section
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(
                                Brush.linearGradient(listOf(SurfaceContainer, SurfaceContainerHigh)),
                                RoundedCornerShape(24.dp)
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!prop.imageUrl.isNullOrEmpty()) {
                            val imageSource = remember(prop.imageUrl) {
                                if (prop.imageUrl.startsWith("/")) File(prop.imageUrl) else prop.imageUrl
                            }
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageSource)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Overlay gradient for better text visibility
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f)))))
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.HomeWork, contentDescription = null, tint = Primary.copy(0.5f), modifier = Modifier.size(72.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(prop.type, color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        
                        val statusColor = when(prop.status) {
                            "AVAILABLE" -> Primary
                            "RENTED" -> Tertiary
                            else -> Error
                        }
                        val statusLabel = when(prop.status) {
                            "AVAILABLE" -> stringResource(R.string.status_available)
                            "RENTED" -> stringResource(R.string.status_rented)
                            else -> stringResource(R.string.status_maintenance)
                        }
                        
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                            color = statusColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(statusColor))
                                Text(statusLabel, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        
                        // Property Basic Info over Image
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(prop.name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(prop.address, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.9f))
                            }
                        }
                    }
                }

                // Price and Location Button
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(stringResource(R.string.monthly_rent, currentCurrency), style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Text(formatter.format(prop.monthlyRent), style = MaterialTheme.typography.headlineLarge, color = Primary, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        Button(
                            onClick = {
                                val gmmIntentUri = android.net.Uri.parse("geo:${prop.latitude},${prop.longitude}?q=${prop.latitude},${prop.longitude}(${prop.name})")
                                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri))
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.horizontalGradient(listOf(SurfaceContainerHigh, SurfaceContainerLow)))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Map, contentDescription = null, tint = OnBackground, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.view_on_map), color = OnBackground, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (prop.status == "AVAILABLE") {
                            Button(
                                onClick = { onRentProperty(prop.id) },
                                modifier = Modifier.fillMaxWidth().height(64.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.horizontalGradient(listOf(NeonGradientStart, NeonGradientEnd)))
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Key, contentDescription = null, tint = OnPrimaryFixed, modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            stringResource(R.string.rent_property_action).uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = OnPrimaryFixed,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Specs Tactical Grid
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            SpecItem(icon = Icons.Default.Bed, label = stringResource(R.string.rooms), value = "${prop.rooms}")
                            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = OutlineVariant.copy(0.2f))
                            SpecItem(icon = Icons.Default.Bathroom, label = stringResource(R.string.bathrooms), value = "${prop.bathrooms}")
                            Divider(modifier = Modifier.height(40.dp).width(1.dp), color = OutlineVariant.copy(0.2f))
                            SpecItem(icon = Icons.Default.SquareFoot, label = stringResource(R.string.area), value = "${prop.area.toInt()} m²")
                        }
                    }
                }

                // Description
                if (prop.description.isNotBlank()) {
                    item {
                        Column {
                            Text(stringResource(R.string.property_description).uppercase(), style = MaterialTheme.typography.labelMedium, color = Primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(prop.description, style = MaterialTheme.typography.bodyLarge, color = OnBackground, lineHeight = 24.sp)
                        }
                    }
                }

                // Rules
                if (!prop.rules.isNullOrBlank()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceContainerHigh, RoundedCornerShape(16.dp))
                                .border(1.dp, OutlineVariant.copy(0.5f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("REGLAS DE LA PROPIEDAD", style = MaterialTheme.typography.labelMedium, color = Primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(prop.rules!!, style = MaterialTheme.typography.bodyMedium, color = OnBackground, lineHeight = 22.sp)
                        }
                    }
                }

                // Contracts Header
                item {
                    Text(
                        stringResource(R.string.contracts_list_title).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                items(contracts.filter { it.status == "ACTIVE" }) { contract ->
                    ContractItem(
                        contract = contract,
                        dateFormat = dateFormat,
                        formatter = formatter,
                        onAddPaymentClick = { onAddPaymentClick(contract.id) },
                        onClick = { onContractClick(contract.id) }
                    )
                }

                if (contracts.any { it.status != "ACTIVE" }) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.past_contracts).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    items(contracts.filter { it.status != "ACTIVE" }) { contract ->
                        ContractItem(
                            contract = contract,
                            dateFormat = dateFormat,
                            formatter = formatter,
                            onAddPaymentClick = { onAddPaymentClick(contract.id) },
                            onClick = { onContractClick(contract.id) }
                        )
                    }
                }

                if (contracts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceContainer, RoundedCornerShape(12.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_contracts_msg),
                                color = OnSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = Tertiary, modifier = Modifier.size(28.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = OnBackground, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun ContractItem(
    contract: Contract, 
    dateFormat: SimpleDateFormat, 
    formatter: NumberFormat,
    onAddPaymentClick: () -> Unit,
    onClick: () -> Unit
) {
    val isPast = contract.status != "ACTIVE"
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPast) SurfaceContainer else SurfaceContainerHigh
        ), 
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isPast) 0.6f else 1f)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.contract_number, contract.id), color = OnBackground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("${dateFormat.format(contract.startDate)} → ${dateFormat.format(contract.endDate)}",
                    color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatter.format(contract.monthlyRent), color = Tertiary, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = if(contract.status == "ACTIVE") Primary.copy(0.15f) else OutlineVariant.copy(0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if(contract.status == "ACTIVE") stringResource(R.string.status_active) else contract.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if(contract.status == "ACTIVE") Primary else OnSurfaceVariant, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                if (contract.status == "ACTIVE") {
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = onAddPaymentClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = Tertiary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.add_payment), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

