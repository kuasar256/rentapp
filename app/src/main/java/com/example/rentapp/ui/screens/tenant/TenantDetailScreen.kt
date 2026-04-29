package com.example.rentapp.ui.screens.tenant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.data.repository.ContractRepository
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.TenantViewModel
import com.example.rentapp.viewmodel.UserViewModel
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDetailScreen(
    tenantId: Long,
    viewModel: TenantViewModel,
    userViewModel: UserViewModel,
    contractRepo: ContractRepository,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onContractClick: (Long) -> Unit
) {
    val context = LocalContext.current
    var contracts by remember { mutableStateOf<List<Contract>>(emptyList()) }
    
    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")
    
    val locale = when (currentCurrency) {
        "BOB" -> Locale("es", "BO")
        "MXN" -> Locale("es", "MX")
        "USD" -> Locale.US
        else -> Locale.getDefault()
    }
    val formatter = NumberFormat.getCurrencyInstance(locale).apply {
        try {
            this.currency = java.util.Currency.getInstance(currentCurrency)
        } catch (e: Exception) {}
    }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val tenantDisplay by viewModel.getTenantDisplayFlow(tenantId).collectAsState(initial = null)
    val tenant = tenantDisplay?.tenant
    
    LaunchedEffect(tenantId) {
        contractRepo.getContractsByTenant(tenantId).collect {
            contracts = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tenant_detail_title), color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        tenant?.let { t ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header with photo
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Glowing border behind image
                            Box(
                                modifier = Modifier
                                    .size(136.dp)
                                    .clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(Primary.copy(alpha = 0.5f), Color.Transparent)))
                            )
                            
                            val imageSource = remember(t.photoUrl) {
                                if (t.photoUrl.startsWith("/")) File(t.photoUrl) else t.photoUrl.ifBlank { null }
                            }
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageSource)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Primary, CircleShape),
                                contentScale = ContentScale.Crop,
                                error = androidx.compose.ui.graphics.painter.ColorPainter(SurfaceContainerHigh)
                            )
                            if (t.photoUrl.isBlank()) {
                                Surface(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Primary, CircleShape),
                                    color = SurfaceContainerHigh
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.padding(24.dp).size(48.dp),
                                        tint = Primary
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "${t.firstName} ${t.lastName}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = OnBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        val displayStatus = tenantDisplay?.paymentStatus ?: t.status
                        val statusColor = when (displayStatus) {
                            "ACTIVE", "PAID" -> Primary
                            "DELAYED" -> Error
                            "PENDING" -> Tertiary
                            else -> Error
                        }
                        val statusLabel = when (displayStatus) {
                            "ACTIVE", "PAID" -> stringResource(R.string.status_active)
                            "DELAYED" -> stringResource(R.string.status_delayed)
                            "PENDING" -> stringResource(R.string.status_pending)
                            else -> stringResource(R.string.status_inactive)
                        }

                        Surface(
                            color = statusColor.copy(0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                statusLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Contact Info
                item {
                    Column {
                        SectionHeader(stringResource(R.string.contact_info))
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                InfoRow(Icons.Default.Email, stringResource(R.string.email_label), t.email)
                                InfoRow(Icons.Default.Phone, stringResource(R.string.phone_label), t.phone)
                                InfoRow(Icons.Default.Work, stringResource(R.string.occupation_label), t.occupation)
                                InfoRow(Icons.Default.AttachMoney, "Ingresos Mensuales", formatter.format(t.monthlyIncome)) // Hardcoded fallback if missing stringResource, replacing below
                            }
                        }
                    }
                }

                // Document Info
                item {
                    Column {
                        SectionHeader(stringResource(R.string.documentation))
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                InfoRow(Icons.Default.Badge, t.documentType, t.documentId)
                                InfoRow(Icons.Default.Public, stringResource(R.string.nationality_label), t.nationality)
                            }
                        }
                    }
                }

                // Emergency Contact
                item {
                    Column {
                        SectionHeader(stringResource(R.string.emergency_contact))
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                InfoRow(Icons.Default.ContactPhone, stringResource(R.string.name_label), t.emergencyContact)
                                InfoRow(Icons.Default.Phone, stringResource(R.string.phone_label), t.emergencyPhone)
                            }
                        }
                    }
                }

                // Contracts
                if (contracts.isNotEmpty()) {
                    item { 
                        Column {
                            SectionHeader(stringResource(R.string.contracts_list_title))
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    items(contracts.filter { it.status == "ACTIVE" }) { contract ->
                            ContractItem(
                                contract = contract,
                                dateFormat = dateFormat,
                                formatter = formatter,
                                onClick = { onContractClick(contract.id) }
                            )
                        }

                    if (contracts.any { it.status != "ACTIVE" }) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "HISTORIAL DE CONTRATOS",
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
                                onClick = { onContractClick(contract.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = Tertiary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    )
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Tertiary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Tertiary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text(value.ifBlank { stringResource(R.string.not_specified) }, style = MaterialTheme.typography.bodyLarge, color = OnBackground, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ContractItem(
    contract: Contract, 
    dateFormat: SimpleDateFormat, 
    formatter: NumberFormat,
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
            }
        }
    }
}
