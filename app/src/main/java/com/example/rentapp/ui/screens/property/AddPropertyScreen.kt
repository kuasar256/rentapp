package com.example.rentapp.ui.screens.property

import androidx.compose.foundation.background
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.*
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.ui.screens.auth.NeonTextField
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PropertyViewModel
import android.location.Geocoder
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyScreen(
    viewModel: PropertyViewModel,
    editPropertyId: Long? = null,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val isEditing = editPropertyId != null
    val existingProperty = if (isEditing) {
        viewModel.allProperties.value.find { it.id == editPropertyId }
    } else null

    var name by remember { mutableStateOf(existingProperty?.name ?: "") }
    var address by remember { mutableStateOf(existingProperty?.address ?: "") }
    var type by remember { mutableStateOf(existingProperty?.type ?: "Casa") }
    var monthlyRent by remember { mutableStateOf(existingProperty?.monthlyRent?.toString() ?: "") }
    var rooms by remember { mutableStateOf(existingProperty?.rooms?.toString() ?: "") }
    var bathrooms by remember { mutableStateOf(existingProperty?.bathrooms?.toString() ?: "") }
    var area by remember { mutableStateOf(existingProperty?.area?.toString() ?: "") }
    var description by remember { mutableStateOf(existingProperty?.description ?: "") }
    var selectedStatus by remember { mutableStateOf(existingProperty?.status ?: "AVAILABLE") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    
    // Map State (Default coordinates or existing)
    val defaultLocal = LatLng(19.4326, -99.1332) // CDMX default
    val markerState = rememberMarkerState(
        position = if (isEditing && existingProperty?.latitude != 0.0) 
            LatLng(existingProperty!!.latitude, existingProperty.longitude) 
        else defaultLocal
    )
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerState.position, 12f)
    }

    // Function to search address and move map
    fun searchAddress(query: String) {
        if (query.isBlank()) return
        scope.launch {
            try {
                val addresses = geocoder.getFromLocationName(query, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addressObj = addresses[0]
                    val newLatLng = LatLng(addressObj.latitude, addressObj.longitude)
                    markerState.position = newLatLng
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Function to update address from map click
    fun updateAddressFromLocation(latLng: LatLng) {
        markerState.position = latLng
        scope.launch {
            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addressObj = addresses[0]
                    address = addressObj.getAddressLine(0) ?: ""
                }
                cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val propertyTypes = listOf("Casa", "Departamento", "Local Comercial", "Oficina", "Bodega", "Terreno")
    val statusOptions = listOf("AVAILABLE" to "Disponible", "RENTED" to "Rentada", "MAINTENANCE" to "Mantenimiento")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Editar Vivienda" else "Agregar Vivienda",
                        color = OnBackground, fontWeight = FontWeight.Bold)
                },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SectionLabel("Información de la Propiedad")
            NeonTextField(value = name, onValueChange = { name = it }, label = "Nombre de la Propiedad", leadingIcon = Icons.Default.Home)

            // Type dropdown
            ExposedDropdownMenuBox(expanded = typeDropdownExpanded, onExpandedChange = { typeDropdownExpanded = it }) {
                OutlinedTextField(
                    value = type, onValueChange = {}, readOnly = true,
                    label = { Text("Tipo de Propiedad", color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Domain, contentDescription = null, tint = OnSurfaceVariant) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                        focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = typeDropdownExpanded, onDismissRequest = { typeDropdownExpanded = false },
                    modifier = Modifier.background(SurfaceContainerHigh)) {
                    propertyTypes.forEach { t ->
                        DropdownMenuItem(text = { Text(t, color = OnBackground) }, onClick = { type = t; typeDropdownExpanded = false })
                    }
                }
            }

            NeonTextField(
                value = address, 
                onValueChange = { address = it }, 
                label = "Dirección", 
                leadingIcon = Icons.Default.LocationOn,
                trailingIcon = {
                    IconButton(onClick = { searchAddress(address) }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar en mapa", tint = Primary)
                    }
                }
            )

            SectionLabel("Ubicación en el Mapa")
            Text("Toca el mapa para fijar la ubicación de la propiedad", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(SurfaceContainerHigh, RoundedCornerShape(12.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng -> updateAddressFromLocation(latLng) },
                    properties = MapProperties(isMyLocationEnabled = false), // Set to false unless you handle permissions
                    uiSettings = MapUiSettings(myLocationButtonEnabled = false)
                ) {
                    Marker(
                        state = markerState,
                        title = "Ubicación de la Propiedad"
                    )
                }
            }

            SectionLabel("Detalles")
            NeonTextField(value = monthlyRent, onValueChange = { monthlyRent = it }, label = "Renta Mensual (MXN)",
                leadingIcon = Icons.Default.AttachMoney, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeonTextField(value = rooms, onValueChange = { rooms = it }, label = "Recámaras",
                    leadingIcon = Icons.Default.Bed, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
                NeonTextField(value = bathrooms, onValueChange = { bathrooms = it }, label = "Baños",
                    leadingIcon = Icons.Default.Bathroom, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f))
            }
            NeonTextField(value = area, onValueChange = { area = it }, label = "Área (m²)",
                leadingIcon = Icons.Default.SquareFoot, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))

            // Status dropdown
            ExposedDropdownMenuBox(expanded = statusDropdownExpanded, onExpandedChange = { statusDropdownExpanded = it }) {
                OutlinedTextField(
                    value = statusOptions.find { it.first == selectedStatus }?.second ?: "Disponible",
                    onValueChange = {}, readOnly = true,
                    label = { Text("Estado", color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Circle, contentDescription = null, tint = when(selectedStatus) { "AVAILABLE" -> Primary; "RENTED" -> Tertiary; else -> Error }) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                        focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = statusDropdownExpanded, onDismissRequest = { statusDropdownExpanded = false },
                    modifier = Modifier.background(SurfaceContainerHigh)) {
                    statusOptions.forEach { (key, label) ->
                        DropdownMenuItem(text = { Text(label, color = OnBackground) },
                            onClick = { selectedStatus = key; statusDropdownExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Descripción (opcional)", color = OnSurfaceVariant) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                    focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                    focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val property = Property(
                        id = existingProperty?.id ?: 0,
                        name = name, address = address, type = type,
                        monthlyRent = monthlyRent.toDoubleOrNull() ?: 0.0,
                        rooms = rooms.toIntOrNull() ?: 0,
                        bathrooms = bathrooms.toIntOrNull() ?: 0,
                        area = area.toDoubleOrNull() ?: 0.0,
                        latitude = markerState.position.latitude,
                        longitude = markerState.position.longitude,
                        status = selectedStatus, description = description
                    )
                    if (isEditing) viewModel.updateProperty(property) else viewModel.insertProperty(property)
                    onSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank() && address.isNotBlank()
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Agregar Propiedad",
                    color = OnPrimaryFixed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
}
