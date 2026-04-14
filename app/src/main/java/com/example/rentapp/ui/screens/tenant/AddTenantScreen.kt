package com.example.rentapp.ui.screens.tenant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.example.rentapp.data.local.entity.Tenant
import com.example.rentapp.ui.screens.auth.NeonTextField
import com.example.rentapp.ui.screens.property.SectionLabel
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.TenantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(
    viewModel: TenantViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var documentId by remember { mutableStateOf("") }
    var documentType by remember { mutableStateOf("INE") }
    var occupation by remember { mutableStateOf("") }
    var monthlyIncome by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var docTypeExpanded by remember { mutableStateOf(false) }
    
    var nationalityExpanded by remember { mutableStateOf(false) }
    var nationality by remember { mutableStateOf("") }
    val countriesList by viewModel.countries.collectAsState()

    val documentTypes = listOf("INE", "Pasaporte", "Licencia de Conducir", "RFC", "CURP")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Inquilino", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Primary)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SectionLabel("Datos Personales")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeonTextField(value = firstName, onValueChange = { firstName = it }, label = "Nombre(s)",
                    leadingIcon = Icons.Default.Person, modifier = Modifier.weight(1f))
                NeonTextField(value = lastName, onValueChange = { lastName = it }, label = "Apellidos",
                    leadingIcon = Icons.Default.Person, modifier = Modifier.weight(1f))
            }
            NeonTextField(value = email, onValueChange = { email = it }, label = "Correo Electrónico",
                leadingIcon = Icons.Default.Email, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            NeonTextField(value = phone, onValueChange = { phone = it }, label = "Teléfono",
                leadingIcon = Icons.Default.Phone, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            NeonTextField(value = occupation, onValueChange = { occupation = it }, label = "Ocupación / Empresa",
                leadingIcon = Icons.Default.Work)
            NeonTextField(value = monthlyIncome, onValueChange = { monthlyIncome = it }, label = "Ingreso Mensual (MXN)",
                leadingIcon = Icons.Default.AttachMoney, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

            SectionLabel("Identificación")
            ExposedDropdownMenuBox(expanded = docTypeExpanded, onExpandedChange = { docTypeExpanded = it }) {
                OutlinedTextField(
                    value = documentType, onValueChange = {}, readOnly = true,
                    label = { Text("Tipo de Identificación", color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = OnSurfaceVariant) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = docTypeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                        focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = docTypeExpanded, onDismissRequest = { docTypeExpanded = false }) {
                    documentTypes.forEach { t ->
                        DropdownMenuItem(text = { Text(t, color = OnBackground) },
                            onClick = { documentType = t; docTypeExpanded = false })
                    }
                }
            }
            NeonTextField(value = documentId, onValueChange = { documentId = it }, label = "Número de Identificación",
                leadingIcon = Icons.Default.Badge)
                
            ExposedDropdownMenuBox(expanded = nationalityExpanded, onExpandedChange = { nationalityExpanded = it }) {
                OutlinedTextField(
                    value = nationality, onValueChange = { nationality = it }, 
                    label = { Text("Nacionalidad / País de Origen", color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, tint = OnSurfaceVariant) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nationalityExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
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

            SectionLabel("Contacto de Emergencia")
            NeonTextField(value = emergencyContact, onValueChange = { emergencyContact = it }, label = "Nombre de Contacto",
                leadingIcon = Icons.Default.PersonPin)
            NeonTextField(value = emergencyPhone, onValueChange = { emergencyPhone = it }, label = "Teléfono de Emergencia",
                leadingIcon = Icons.Default.Phone, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val tenant = Tenant(
                        firstName = firstName, lastName = lastName,
                        email = email, phone = phone,
                        documentId = documentId, documentType = documentType,
                        nationality = nationality,
                        occupation = occupation,
                        monthlyIncome = monthlyIncome.toDoubleOrNull() ?: 0.0,
                        emergencyContact = emergencyContact, emergencyPhone = emergencyPhone
                    )
                    viewModel.insertTenant(tenant)
                    onSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp),
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank()
            ) {
                Text("Agregar Inquilino", color = OnPrimaryFixed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
