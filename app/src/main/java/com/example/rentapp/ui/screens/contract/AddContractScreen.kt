package com.example.rentapp.ui.screens.contract

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.Contract
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.data.repository.ContractRepository
import com.example.rentapp.ui.screens.auth.NeonTextField
import com.example.rentapp.ui.screens.property.SectionLabel
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PropertyViewModel
import com.example.rentapp.viewmodel.TenantViewModel
import com.example.rentapp.notification.NotificationHelper
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContractScreen(
    propertyId: Long,
    tenantId: Long? = null,
    tenantViewModel: TenantViewModel,
    propertyViewModel: PropertyViewModel,
    contractRepo: ContractRepository,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val properties by propertyViewModel.allProperties.collectAsState(initial = emptyList())
    val property = remember(properties, propertyId) { properties.find { it.id == propertyId } }

    val tenantListState by tenantViewModel.tenantDisplayList.collectAsState()
    val tenants = tenantListState.map { it.tenant }

    // ── Tenant mode ──────────────────────────────────────────────────────────
    // 0 = Existing Tenant   |   1 = New Tenant
    var tenantMode by remember { mutableIntStateOf(if (tenantId != null) 0 else 0) }

    // Existing Tenant State
    var selectedTenantId by remember { mutableStateOf(tenantId) }
    var tenantDropdownExpanded by remember { mutableStateOf(false) }

    // New Tenant State
    var newFirstName by remember { mutableStateOf("") }
    var newLastName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newDocumentId by remember { mutableStateOf("") }
    var newOccupation by remember { mutableStateOf("") }

    // ── Contract State ───────────────────────────────────────────────────────
    var monthlyRent by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }
    var paymentDueDay by remember { mutableStateOf("5") }
    var hasEvictionClause by remember { mutableStateOf(true) }
    var earlyTerminationPenalty by remember { mutableStateOf("") }
    var guarantorName by remember { mutableStateOf("") }
    var guarantorProperty by remember { mutableStateOf("") }
    var isGuarantorRequired by remember { mutableStateOf(false) }

    var showTermsDialog by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    // Pre-fill monthly rent from property
    LaunchedEffect(property) {
        property?.let {
            if (monthlyRent.isEmpty()) {
                monthlyRent = if (it.paymentType == "Anticrético") "0" else it.monthlyRent.toString()
            }
            if (deposit.isEmpty() && it.paymentType == "Anticrético") {
                deposit = it.monthlyRent.toString() // For Anticrético, the "rent" field in property might store the total amount
            }
        }
    }

    // Pre-fill payment due day from selected tenant
    LaunchedEffect(selectedTenantId, tenants) {
        if (selectedTenantId != null && selectedTenantId != -1L) {
            tenants.find { it.id == selectedTenantId }?.let {
                paymentDueDay = it.monthlyRentDueDate.toString()
            }
        }
    }

    // ── Dates ────────────────────────────────────────────────────────────────
    var startDate by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf<Long?>(System.currentTimeMillis() + 31536000000L) }
    var isIndefinite by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
    val endDatePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
    
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Validation
    val existingTenantValid = tenantMode == 0 && selectedTenantId != null
    val newTenantValid = tenantMode == 1 && newFirstName.isNotBlank() && newLastName.isNotBlank() && newEmail.isNotBlank()
    
    val monthlyRentValue = monthlyRent.toDoubleOrNull()
    val depositValue = deposit.toDoubleOrNull()
    val isAnticretico = property?.paymentType == "Anticrético"
    
    val contractFieldsValid = monthlyRentValue != null && 
                             depositValue != null && 
                             paymentDueDay.isNotBlank() && 
                             startDate != null && 
                             (isIndefinite || (endDate != null && endDate!! > startDate!!)) &&
                             (!isAnticretico || depositValue > 0) // Anticrético must have a deposit/amount

    val isFormValid = (existingTenantValid || newTenantValid) && contractFieldsValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.add_contract),
                            color = OnBackground,
                            fontWeight = FontWeight.Bold
                        )
                        property?.name?.let {
                            Text(it, style = MaterialTheme.typography.labelMedium, color = Primary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Fraud Prevention Banner ──────────────────────────────────────
            Surface(
                color = WarningContainer.copy(alpha = 0.7f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = OnWarningContainer)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.fraud_prevention_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnWarningContainer
                    )
                }
            }

            // ── Tenant Section ───────────────────────────────────────────────
            SectionLabel(stringResource(R.string.tenant))

            // Toggle: Existing / New Tenant
            if (tenantId == null) {
                TenantModeToggle(
                    selectedMode = tenantMode,
                    onModeChange = { tenantMode = it }
                )
            }

            // ── Existing Tenant Picker ───────────────────────────────────────
            AnimatedVisibility(
                visible = tenantMode == 0,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                ExposedDropdownMenuBox(
                    expanded = tenantDropdownExpanded && tenantId == null,
                    onExpandedChange = { if (tenantId == null) tenantDropdownExpanded = it }
                ) {
                    val selectedTenantName = tenants.find { it.id == selectedTenantId }
                        ?.let { "${it.firstName} ${it.lastName}" }
                        ?: stringResource(R.string.select_tenant)

                    OutlinedTextField(
                        value = selectedTenantName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.select_tenant), color = OnSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Primary) },
                        trailingIcon = {
                            if (tenantId == null) ExposedDropdownMenuDefaults.TrailingIcon(expanded = tenantDropdownExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                            focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                            focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = tenantId == null
                    )

                    if (tenantId == null) {
                        ExposedDropdownMenu(
                            expanded = tenantDropdownExpanded,
                            onDismissRequest = { tenantDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceContainerHigh)
                        ) {
                            if (tenants.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay inquilinos registrados", color = OnSurfaceVariant) },
                                    onClick = {}
                                )
                            }
                            tenants.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text("${t.firstName} ${t.lastName}", color = OnBackground) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Primary) },
                                    onClick = {
                                        selectedTenantId = t.id
                                        tenantDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── New Tenant Form ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = tenantMode == 1,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Primary.copy(0.3f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Datos del Nuevo Inquilino",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                NeonTextField(
                                    value = newFirstName,
                                    onValueChange = { newFirstName = it },
                                    label = stringResource(R.string.first_name),
                                    leadingIcon = Icons.Default.Person,
                                    modifier = Modifier.weight(1f)
                                )
                                NeonTextField(
                                    value = newLastName,
                                    onValueChange = { newLastName = it },
                                    label = stringResource(R.string.last_name),
                                    leadingIcon = Icons.Default.Person,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            NeonTextField(
                                value = newEmail,
                                onValueChange = { newEmail = it },
                                label = stringResource(R.string.email_label),
                                leadingIcon = Icons.Default.Email,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            NeonTextField(
                                value = newPhone,
                                onValueChange = { newPhone = it },
                                label = stringResource(R.string.phone_label),
                                leadingIcon = Icons.Default.Phone,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                            NeonTextField(
                                value = newDocumentId,
                                onValueChange = { newDocumentId = it },
                                label = stringResource(R.string.id_number),
                                leadingIcon = Icons.Default.Badge
                            )
                            NeonTextField(
                                value = newOccupation,
                                onValueChange = { newOccupation = it },
                                label = stringResource(R.string.occupation_label),
                                leadingIcon = Icons.Default.Work
                            )
                        }
                    }
                }
            }

            // ── Contract Details ─────────────────────────────────────────────
            SectionLabel("Términos del Contrato")

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Primary.copy(0.15f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Indefinite Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHigh)
                            .clickable { isIndefinite = !isIndefinite }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Vigencia Indefinida", color = OnBackground, fontWeight = FontWeight.Bold)
                            Text("Sin fecha de fin predefinida", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                        Switch(
                            checked = isIndefinite,
                            onCheckedChange = { isIndefinite = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = OnPrimaryFixed, checkedTrackColor = Primary)
                        )
                    }

                    // Start Date
                    OutlinedTextField(
                        value = startDate?.let { dateFormatter.format(Date(it)) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.start_date), color = OnSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Primary) },
                        modifier = Modifier.fillMaxWidth().clickable { showStartDatePicker = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = OnBackground,
                            disabledBorderColor = OutlineVariant,
                            disabledContainerColor = SurfaceContainerHigh,
                            disabledLeadingIconColor = Primary,
                            disabledLabelColor = OnSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // End Date
                    AnimatedVisibility(visible = !isIndefinite, enter = expandVertically(), exit = shrinkVertically()) {
                        OutlinedTextField(
                            value = endDate?.let { dateFormatter.format(Date(it)) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.end_date), color = OnSurfaceVariant) },
                            leadingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null, tint = Primary) },
                            modifier = Modifier.fillMaxWidth().clickable { showEndDatePicker = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = OnBackground,
                                disabledBorderColor = OutlineVariant,
                                disabledContainerColor = SurfaceContainerHigh,
                                disabledLeadingIconColor = Primary,
                                disabledLabelColor = OnSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    if (isIndefinite) {
                        OutlinedTextField(
                            value = "Indefinida",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.end_date), color = OnSurfaceVariant) },
                            leadingIcon = { Icon(Icons.Default.AllInclusive, contentDescription = null, tint = Primary) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Primary,
                                disabledBorderColor = Primary.copy(0.5f),
                                disabledContainerColor = Primary.copy(0.1f),
                                disabledLeadingIconColor = Primary,
                                disabledLabelColor = Primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    NeonTextField(
                        value = paymentDueDay,
                        onValueChange = {
                            if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                                val num = it.toIntOrNull()
                                if (it.isEmpty() || (num != null && num in 1..31)) paymentDueDay = it
                            }
                        },
                        label = stringResource(R.string.payment_due_day),
                        leadingIcon = Icons.Default.Event,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            SectionLabel("Valores Financieros")
            val isAnticretico = property?.paymentType == "Anticrético"
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, Tertiary.copy(0.15f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isAnticretico) {
                        Surface(
                            color = Tertiary.copy(0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Tertiary, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Modalidad Anticrético: El alquiler mensual es 0 y se registra el monto total como depósito.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NeonTextField(
                            value = monthlyRent,
                            onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) monthlyRent = it },
                            label = if (isAnticretico) "Alquiler (0)" else stringResource(R.string.monthly_rent, ""),
                            leadingIcon = Icons.Default.AttachMoney,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            enabled = false
                        )
                        NeonTextField(
                            value = deposit,
                            onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) deposit = it },
                            label = if (isAnticretico) "Monto Anticrético" else stringResource(R.string.deposit),
                            leadingIcon = Icons.Default.AccountBalanceWallet,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            SectionLabel("Cláusulas y Penalizaciones")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, OutlineVariant.copy(0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHigh)
                            .clickable { hasEvictionClause = !hasEvictionClause }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cláusula de Allanamiento Futuro", color = OnBackground, fontWeight = FontWeight.Bold)
                            Text("Agiliza el proceso de desalojo", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                        Switch(
                            checked = hasEvictionClause,
                            onCheckedChange = { hasEvictionClause = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = OnPrimaryFixed, checkedTrackColor = Primary)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        NeonTextField(
                            value = earlyTerminationPenalty,
                            onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) earlyTerminationPenalty = it },
                            label = "Penalidad Term. Antic.",
                            leadingIcon = Icons.Default.Cancel,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            SectionLabel("Aval (Opcional)")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, OutlineVariant.copy(0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHigh)
                            .clickable { isGuarantorRequired = !isGuarantorRequired }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Incluir Aval / Garantía", color = OnBackground, fontWeight = FontWeight.Bold)
                            Text("Activar para ingresar datos del aval", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                        Switch(
                            checked = isGuarantorRequired,
                            onCheckedChange = { isGuarantorRequired = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OnPrimaryFixed,
                                checkedTrackColor = Primary,
                                uncheckedThumbColor = OnSurfaceVariant,
                                uncheckedTrackColor = SurfaceContainerHighest
                            )
                        )
                    }

                    AnimatedVisibility(visible = isGuarantorRequired) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            NeonTextField(
                                value = guarantorName,
                                onValueChange = { guarantorName = it },
                                label = "Nombre del Aval",
                                leadingIcon = Icons.Default.PersonOutline
                            )
                            NeonTextField(
                                value = guarantorProperty,
                                onValueChange = { guarantorProperty = it },
                                label = "Inmueble en Garantía",
                                leadingIcon = Icons.Default.HomeWork
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Save Button ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(listOf(NeonGradientStart, NeonGradientEnd)),
                        alpha = if (isSaving || !isFormValid) 0.45f else 1f
                    )
                    .clickable(enabled = !isSaving && isFormValid) {
                        showTermsDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = OnPrimary, modifier = Modifier.size(26.dp), strokeWidth = 3.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = OnPrimaryFixed, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.create_contract).uppercase(),
                            color = OnPrimaryFixed,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        // ── Date Pickers ─────────────────────────────────────────────────────
        if (showStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        startDate = startDatePickerState.selectedDateMillis
                        showStartDatePicker = false
                    }) { Text(stringResource(R.string.ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) { Text(stringResource(R.string.cancel)) }
                }
            ) { DatePicker(state = startDatePickerState) }
        }

        if (showEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        endDate = endDatePickerState.selectedDateMillis
                        showEndDatePicker = false
                    }) { Text(stringResource(R.string.ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) { Text(stringResource(R.string.cancel)) }
                }
            ) { DatePicker(state = endDatePickerState) }
        }

        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                icon = { Icon(Icons.Default.Gavel, contentDescription = null, tint = Primary) },
                title = { Text(stringResource(R.string.terms_title), fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                stringResource(R.string.terms_content),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                        
                        HorizontalDivider(color = OutlineVariant.copy(0.4f))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { termsAccepted = !termsAccepted }
                        ) {
                            Checkbox(
                                checked = termsAccepted,
                                onCheckedChange = { termsAccepted = it },
                                colors = CheckboxDefaults.colors(checkedColor = Primary)
                            )
                            Text(
                                stringResource(R.string.accept_terms),
                                style = MaterialTheme.typography.labelMedium,
                                color = OnBackground
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showTermsDialog = false
                            isSaving = true
                            scope.launch {
                                // 1. Resolve tenant ID
                                val finalTenantId: Long = if (tenantMode == 1) {
                                    val newTenant = Tenant(
                                        firstName = newFirstName.trim(),
                                        lastName = newLastName.trim(),
                                        email = newEmail.trim(),
                                        phone = newPhone.trim(),
                                        documentId = newDocumentId.trim(),
                                        occupation = newOccupation.trim(),
                                        monthlyRentDueDate = paymentDueDay.toIntOrNull() ?: 5
                                    )
                                    tenantViewModel.insertTenantAndGetId(newTenant) ?: -1L
                                } else {
                                    selectedTenantId ?: -1L
                                }

                                if (finalTenantId == -1L) {
                                    isSaving = false
                                    return@launch
                                }

                                // 2. Create contract
                                val finalEndDate = if (isIndefinite) 0L else endDate!!
                                val newContract = Contract(
                                    propertyId = propertyId,
                                    tenantId = finalTenantId,
                                    startDate = startDate!!,
                                    endDate = finalEndDate,
                                    monthlyRent = monthlyRent.toDoubleOrNull() ?: 0.0,
                                    deposit = deposit.toDoubleOrNull() ?: 0.0,
                                    paymentDueDay = paymentDueDay.toIntOrNull() ?: 5,
                                    notes = "",
                                    status = "ACTIVE",
                                    hasEvictionClause = hasEvictionClause,
                                    lateFeePenalty = 0.0,
                                    earlyTerminationPenalty = earlyTerminationPenalty.toDoubleOrNull() ?: 0.0,
                                    guarantorName = guarantorName.trim(),
                                    guarantorProperty = guarantorProperty.trim()
                                )
                                contractRepo.insertContract(newContract)

                                // 3. Mark property as RENTED
                                propertyViewModel.updatePropertyStatus(propertyId, "RENTED")

                                // 4. Notify Owner
                                val tenantName = if (tenantMode == 1) "$newFirstName $newLastName"
                                else tenants.find { it.id == selectedTenantId }?.let { "${it.firstName} ${it.lastName}" } ?: ""
                                
                                NotificationHelper.showGeneralNotification(
                                    context,
                                    context.getString(R.string.notification_property_rented_title),
                                    context.getString(R.string.notification_property_rented_body, property?.name ?: "", tenantName)
                                )

                                kotlinx.coroutines.delay(400)
                                onSuccess()
                            }
                        },
                        enabled = termsAccepted,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(stringResource(R.string.create_contract))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTermsDialog = false }) {
                        Text(stringResource(R.string.cancel), color = OnSurfaceVariant)
                    }
                },
                containerColor = SurfaceContainerHigh,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

/**
 * Segmented toggle to switch between "Existing Tenant" and "New Tenant" modes.
 */
@Composable
private fun TenantModeToggle(selectedMode: Int, onModeChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainer)
            .border(1.dp, OutlineVariant.copy(0.4f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            Triple(0, Icons.Default.PersonSearch, "Inquilino Existente"),
            Triple(1, Icons.Default.PersonAdd, "Nuevo Inquilino")
        ).forEach { (mode, icon, label) ->
            val selected = selectedMode == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) Brush.horizontalGradient(listOf(NeonGradientStart, NeonGradientEnd))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable { onModeChange(mode) },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) OnPrimaryFixed else OnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        color = if (selected) OnPrimaryFixed else OnSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
