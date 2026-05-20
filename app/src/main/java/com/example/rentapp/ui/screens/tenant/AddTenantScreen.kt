package com.example.rentapp.ui.screens.tenant

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.ui.screens.auth.NeonTextField
import com.example.rentapp.ui.screens.property.SectionLabel
import com.example.rentapp.ui.theme.*
import com.example.rentapp.ui.components.ImagePickerField
import com.example.rentapp.ui.components.DocumentGallery
import com.example.rentapp.ui.components.persistImageLocally
import com.example.rentapp.viewmodel.TenantViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(
    viewModel: TenantViewModel,
    propertyId: Long? = null,
    editTenantId: Long? = null,
    onBack: () -> Unit,
    onSuccess: (Long?) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var documentId by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var monthlyIncome by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var monthlyRentDueDate by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var documentImageUris by remember { mutableStateOf<List<String>>(emptyList()) }
    var existingTenant by remember { mutableStateOf<Tenant?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Validation States
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var documentIdError by remember { mutableStateOf<String?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    var nationalityExpanded by remember { mutableStateOf(false) }
    var nationality by remember { mutableStateOf("") }
    val countriesList by viewModel.countries.collectAsState()
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(editTenantId) {
        if (editTenantId != null) {
            val t = viewModel.getTenantById(editTenantId)
            if (t != null) {
                existingTenant = t
                firstName = t.firstName
                lastName = t.lastName
                email = t.email
                phone = t.phone
                documentId = t.documentId
                occupation = t.occupation
                monthlyIncome = t.monthlyIncome.toString()
                emergencyContact = t.emergencyContact
                emergencyPhone = t.emergencyPhone
                monthlyRentDueDate = t.monthlyRentDueDate.toString()
                nationality = t.nationality
                if (t.photoUrl.isNotBlank()) {
                    photoUri = t.photoUrl
                }
                documentImageUris = t.documentImageUris.split(",").filter { it.isNotBlank() }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (editTenantId == null) {
                            if (propertyId != null) stringResource(R.string.register_tenant_for_rent)
                            else stringResource(R.string.add_tenant)
                        } else stringResource(R.string.edit_tenant), 
                        color = OnBackground, 
                        fontWeight = FontWeight.Bold
                    ) 
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

            SectionLabel(stringResource(R.string.personal_data))
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeonTextField(
                    value = firstName,
                    onValueChange = { 
                        firstName = it
                        if (it.isNotBlank()) firstNameError = null
                    },
                    label = stringResource(R.string.first_name),
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.weight(1f),
                    isError = firstNameError != null,
                    supportingText = firstNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
                NeonTextField(
                    value = lastName,
                    onValueChange = { 
                        lastName = it
                        if (it.isNotBlank()) lastNameError = null
                    },
                    label = stringResource(R.string.last_name),
                    leadingIcon = Icons.Default.Person,
                    modifier = Modifier.weight(1f),
                    isError = lastNameError != null,
                    supportingText = lastNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
            
            val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")
            val currencyIcon = if (currentCurrency == "BOB") Icons.Default.Payments else Icons.Default.AttachMoney
            
            NeonTextField(
                value = email,
                onValueChange = { 
                    email = it
                    if (it.isNotBlank()) emailError = null
                },
                label = stringResource(R.string.email_label),
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            NeonTextField(
                value = phone, onValueChange = { phone = it }, label = stringResource(R.string.phone_label),
                leadingIcon = Icons.Default.Phone, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            NeonTextField(
                value = occupation, onValueChange = { occupation = it }, label = stringResource(R.string.occupation_label),
                leadingIcon = Icons.Default.Work
            )
            NeonTextField(
                value = monthlyIncome, onValueChange = { monthlyIncome = it }, 
                label = stringResource(R.string.monthly_income, currentCurrency.uppercase()),
                leadingIcon = currencyIcon, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            SectionLabel(stringResource(R.string.identification))
            Spacer(Modifier.height(4.dp))
            NeonTextField(
                value = documentId,
                onValueChange = { 
                    documentId = it
                    if (it.isNotBlank()) documentIdError = null
                },
                label = stringResource(R.string.id_number),
                leadingIcon = Icons.Default.Badge,
                isError = documentIdError != null,
                supportingText = documentIdError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
                
            ExposedDropdownMenuBox(expanded = nationalityExpanded, onExpandedChange = { nationalityExpanded = it }) {
                OutlinedTextField(
                    value = nationality, onValueChange = { nationality = it }, 
                    label = { Text(stringResource(R.string.nationality_label), color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, tint = OnSurfaceVariant) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nationalityExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                        focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (countriesList.isNotEmpty()) {
                    val filteredList = if (nationality.isBlank()) countriesList.take(20) 
                                       else countriesList.filter { it.name.common.contains(nationality, true) }.take(20)
                    ExposedDropdownMenu(expanded = nationalityExpanded, onDismissRequest = { nationalityExpanded = false }) {
                        filteredList.forEach { country ->
                            DropdownMenuItem(text = { Text(country.name.common, color = OnBackground) },
                                onClick = { nationality = country.name.common; nationalityExpanded = false })
                        }
                    }
                }
            }

            SectionLabel(stringResource(R.string.emergency_contact))
            Spacer(Modifier.height(4.dp))
            NeonTextField(
                value = emergencyContact, onValueChange = { emergencyContact = it }, label = stringResource(R.string.emergency_contact_name),
                leadingIcon = Icons.Default.PersonPin
            )
            NeonTextField(
                value = emergencyPhone, onValueChange = { emergencyPhone = it }, label = stringResource(R.string.emergency_phone),
                leadingIcon = Icons.Default.Phone, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            SectionLabel(stringResource(R.string.rental_info))
            Spacer(Modifier.height(4.dp))
            
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val calendar = Calendar.getInstance().apply { timeInMillis = millis }
                                monthlyRentDueDate = calendar.get(Calendar.DAY_OF_MONTH).toString()
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            NeonTextField(
                value = monthlyRentDueDate,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || (newValue.all { it.isDigit() } && (newValue.toIntOrNull() ?: 0) <= 31)) {
                        monthlyRentDueDate = newValue
                    }
                },
                label = stringResource(R.string.rent_due_day),
                leadingIcon = Icons.Default.DateRange,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha", tint = Primary)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            SectionLabel(stringResource(R.string.tenant_photo))
            Spacer(Modifier.height(4.dp))
            ImagePickerField(
                label = stringResource(R.string.tap_to_add_photo),
                imageUri = photoUri,
                onImageSelected = { photoUri = it }
            )

            SectionLabel("Documentos de Identidad (Fotos)")
            Spacer(Modifier.height(4.dp))
            val multiImageLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetMultipleContents()
            ) { uris: List<Uri> ->
                val localPaths = uris.mapNotNull { persistImageLocally(context, it) }
                documentImageUris = documentImageUris + localPaths
            }
            
            DocumentGallery(
                uris = documentImageUris,
                onAddClick = { multiImageLauncher.launch("image/*") },
                onImageClick = { index -> 
                    // Optional: View full screen even in edit mode
                }
            )

            Spacer(Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(listOf(NeonGradientStart, NeonGradientEnd)),
                        alpha = if (isSaving) 0.5f else 1f
                    )
                    .clickable(enabled = !isSaving) {
                        // Validate before saving
                        var hasError = false
                        if (firstName.isBlank()) {
                            firstNameError = context.resources.getString(R.string.error_field_required)
                            hasError = true
                        }
                        if (lastName.isBlank()) {
                            lastNameError = context.resources.getString(R.string.error_field_required)
                            hasError = true
                        }
                        if (email.isBlank()) {
                            emailError = context.resources.getString(R.string.error_field_required)
                            hasError = true
                        }
                        if (documentId.isBlank()) {
                            documentIdError = context.resources.getString(R.string.error_field_required)
                            hasError = true
                        }

                        if (!hasError) {
                            isSaving = true
                            scope.launch {
                                val tenant = (existingTenant ?: Tenant()).copy(
                                    firstName = firstName.trim(),
                                    lastName = lastName.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    documentId = documentId.trim(),
                                    nationality = nationality,
                                    occupation = occupation,
                                    monthlyIncome = monthlyIncome.toDoubleOrNull() ?: 0.0,
                                    emergencyContact = emergencyContact,
                                    emergencyPhone = emergencyPhone,
                                    monthlyRentDueDate = monthlyRentDueDate.toIntOrNull() ?: 1,
                                    photoUrl = photoUri ?: "",
                                    documentImageUris = documentImageUris.joinToString(",")
                                )
                                
                                var savedId: Long? = null
                                if (editTenantId == null) {
                                    savedId = viewModel.insertTenantAndGetId(tenant)
                                } else {
                                    viewModel.updateTenant(tenant)
                                    savedId = editTenantId
                                }
                                
                                kotlinx.coroutines.delay(500)
                                onSuccess(savedId)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = OnPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (editTenantId == null) {
                                if (propertyId != null) stringResource(R.string.next_create_contract)
                                else stringResource(R.string.add_tenant)
                            } else stringResource(R.string.save_changes), 
                            color = OnPrimaryFixed, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 18.sp
                        )
                        if (propertyId != null && editTenantId == null) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = OnPrimaryFixed)
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
