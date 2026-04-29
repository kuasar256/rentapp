package com.example.rentapp.ui.screens.contract

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.data.repository.ContractRepository
import com.example.rentapp.data.repository.PaymentRepository
import com.example.rentapp.data.repository.PropertyRepository
import com.example.rentapp.data.repository.TenantRepository
import com.example.rentapp.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDetailScreen(
    contractId: Long,
    contractRepo: ContractRepository,
    propertyRepo: PropertyRepository,
    tenantRepo: TenantRepository,
    paymentRepo: PaymentRepository,
    onBack: () -> Unit,
    onAddPayment: (Long) -> Unit,
    onViewPayments: () -> Unit = {}
) {
    val context = LocalContext.current
    var contract by remember { mutableStateOf<Contract?>(null) }
    var property by remember { mutableStateOf<Property?>(null) }
    var tenant by remember { mutableStateOf<Tenant?>(null) }
    var payments by remember { mutableStateOf<List<Payment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager
        .getCurrencyFlow(context)
        .collectAsState(initial = "USD")

    val formatter = remember(currentCurrency) {
        val locale = when (currentCurrency) {
            "BOB" -> Locale("es", "BO")
            "MXN" -> Locale("es", "MX")
            else -> Locale("en", "US")
        }
        NumberFormat.getCurrencyInstance(locale).apply {
            try { this.currency = Currency.getInstance(currentCurrency) } catch (e: Exception) {}
        }
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateFormatShort = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LaunchedEffect(contractId) {
        isLoading = true
        val c = contractRepo.getContractById(contractId)
        if (c != null) {
            contract = c
            property = propertyRepo.getPropertyById(c.propertyId)
            tenant = tenantRepo.getTenantById(c.tenantId)
        }
        isLoading = false
    }

    // Collect payments for this contract
    LaunchedEffect(contractId) {
        paymentRepo.getPaymentsByContract(contractId).collect {
            payments = it.sortedByDescending { p -> p.dueDate }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Contrato Digital",
                        color = OnBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share/PDF - future implementation */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            if (contract?.status == "ACTIVE") {
                ExtendedFloatingActionButton(
                    onClick = { contract?.let { onAddPayment(it.id) } },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Registrar Pago", fontWeight = FontWeight.Bold) },
                    containerColor = Tertiary,
                    contentColor = OnBackground
                )
            }
        },
        containerColor = Background
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        if (contract == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Error, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Contrato no encontrado", color = OnSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                }
            }
            return@Scaffold
        }

        val c = contract!!

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Document Header ─────────────────────────────────────────────
            item {
                ContractDocumentHeader(contract = c, dateFormat = dateFormat)
            }

            // ── Parties ─────────────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle(icon = Icons.Default.Group, title = "Partes del Contrato")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PartyCard(
                            role = "Propietario / Inmueble",
                            name = property?.name ?: "N/A",
                            subtext = property?.address ?: "",
                            icon = Icons.Default.HomeWork,
                            accentColor = Primary,
                            modifier = Modifier.weight(1f)
                        )
                        PartyCard(
                            role = "Inquilino",
                            name = tenant?.let { "${it.firstName} ${it.lastName}" } ?: "N/A",
                            subtext = tenant?.email ?: "",
                            icon = Icons.Default.Person,
                            accentColor = Tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Financial Terms ──────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle(icon = Icons.Default.Receipt, title = "Términos Financieros")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Primary.copy(0.1f), RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            FinancialLineItem(
                                label = "Pago Inquilino",
                                value = formatter.format(c.monthlyRent),
                                icon = Icons.Default.Payments,
                                highlight = true
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = OutlineVariant.copy(0.2f))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Distribución (Modelo Mediador)", style = MaterialTheme.typography.labelMedium, color = Primary, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text("• Pago a propietario (98%)", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                    Text("• Comisión RentApp (2%)", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Spacer(Modifier.height(20.dp))
                                    Text(formatter.format(c.monthlyRent * 0.98), style = MaterialTheme.typography.bodySmall, color = OnBackground, fontWeight = FontWeight.SemiBold)
                                    Text(formatter.format(c.monthlyRent * 0.02), style = MaterialTheme.typography.bodySmall, color = OnBackground, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = OutlineVariant.copy(0.2f))
                            FinancialLineItem(
                                label = "Depósito de Garantía",
                                value = formatter.format(c.deposit),
                                icon = Icons.Default.AccountBalanceWallet
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = OutlineVariant.copy(0.2f))
                            FinancialLineItem(
                                label = "Día de Pago",
                                value = "Cada día ${c.paymentDueDay} del mes",
                                icon = Icons.Default.EventAvailable
                            )
                        }
                    }
                }
            }

            // ── Contract Period ───────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle(icon = Icons.Default.CalendarToday, title = "Vigencia del Contrato")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DateColumn(label = "Inicio", date = dateFormat.format(c.startDate), color = Primary)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = OutlineVariant.copy(0.6f)
                                )
                                if (c.endDate == 0L) {
                                    Text(
                                        "∞",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OnSurfaceVariant
                                    )
                                } else {
                                    val totalDays = ((c.endDate - c.startDate) / 86400000).toInt()
                                    Text(
                                        "$totalDays días",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                            val endDateStr = if (c.endDate == 0L) "Indefinida" else dateFormat.format(c.endDate)
                            DateColumn(label = "Fin", date = endDateStr, color = Tertiary)
                        }
                    }
                }
            }

            // ── Legal Terms and Penalties ─────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle(icon = Icons.Default.Security, title = "Términos Legales y Penalidades")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Primary.copy(0.1f), RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Allanamiento Futuro
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (c.hasEvictionClause) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (c.hasEvictionClause) Primary else Error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Cláusula de Allanamiento Futuro", style = MaterialTheme.typography.labelMedium, color = OnBackground, fontWeight = FontWeight.Bold)
                                    Text(
                                        if (c.hasEvictionClause) "El inquilino acepta el desalojo express por incumplimiento." else "No incluida en este contrato.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = OutlineVariant.copy(0.2f))
                            
                            // Penalidades
                            Text("Penalidades", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Por mora / retraso:", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                Text(formatter.format(c.lateFeePenalty), style = MaterialTheme.typography.bodySmall, color = OnBackground, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Por cancelación anticipada:", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                Text(formatter.format(c.earlyTerminationPenalty), style = MaterialTheme.typography.bodySmall, color = OnBackground, fontWeight = FontWeight.SemiBold)
                            }
                            
                            // Aval
                            if (c.guarantorName.isNotBlank()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = OutlineVariant.copy(0.2f))
                                Text("Aval / Fiador", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text("Nombre: ${c.guarantorName}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                if (c.guarantorProperty.isNotBlank()) {
                                    Text("Garantía: ${c.guarantorProperty}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // ── Notes ────────────────────────────────────────────────────────
            if (c.notes.isNotBlank()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle(icon = Icons.Default.Notes, title = "Notas y Condiciones")
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = c.notes,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnBackground,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            // ── Payment History ───────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle(icon = Icons.Default.History, title = "Historial de Pagos")
                    if (payments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceContainer, RoundedCornerShape(16.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sin pagos registrados aún",
                                color = OnSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(payments) { payment ->
                PaymentHistoryItem(payment = payment, formatter = formatter, dateFormat = dateFormatShort)
            }

            // Bottom padding for FAB
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ── Composables ────────────────────────────────────────────────────────────────

@Composable
private fun ContractDocumentHeader(contract: Contract, dateFormat: SimpleDateFormat) {
    val isActive = contract.status == "ACTIVE"
    val statusColor = if (isActive) Primary else Error
    val statusLabel = if (isActive) "ACTIVO" else "VENCIDO"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        statusColor.copy(alpha = 0.18f),
                        SurfaceContainer.copy(alpha = 0.9f)
                    )
                )
            )
            .border(1.dp, statusColor.copy(0.25f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Document icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                color = statusColor.copy(0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    statusLabel,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    color = statusColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "Contrato № ${contract.id}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = OnBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Emitido el ${dateFormat.format(contract.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Primary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}

@Composable
private fun PartyCard(
    role: String,
    name: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Text(
                role,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
            if (subtext.isNotBlank()) {
                Text(
                    subtext,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FinancialLineItem(
    label: String,
    value: String,
    icon: ImageVector,
    highlight: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (highlight) Primary.copy(0.15f) else Tertiary.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (highlight) Primary else Tertiary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
        Text(
            value,
            style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (highlight) Primary else OnBackground
        )
    }
}

@Composable
private fun DateColumn(label: String, date: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(
            date,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun PaymentHistoryItem(
    payment: Payment,
    formatter: NumberFormat,
    dateFormat: SimpleDateFormat
) {
    val statusColor = when (payment.status) {
        "PAID" -> Primary
        "DELAYED" -> Error
        else -> Tertiary
    }
    val statusLabel = when (payment.status) {
        "PAID" -> "Pagado"
        "DELAYED" -> "Atrasado"
        else -> "Pendiente"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(0.15f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (payment.status == "PAID") Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    formatter.format(payment.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Text(
                    if (payment.paidDate != null) dateFormat.format(payment.paidDate) else dateFormat.format(payment.dueDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                if (payment.notes.isNotBlank()) {
                    Text(
                        payment.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Surface(
                color = statusColor.copy(0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    statusLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
