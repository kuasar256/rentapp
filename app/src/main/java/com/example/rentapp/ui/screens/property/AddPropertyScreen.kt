package com.example.rentapp.ui.screens.property

import android.location.Geocoder
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.Marker as OsmdroidMarker
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.Property
import com.example.rentapp.ui.screens.auth.NeonTextField
import com.example.rentapp.ui.theme.*
import com.example.rentapp.ui.components.ImagePickerField
import com.example.rentapp.viewmodel.PropertyViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
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
    var paymentType by remember { mutableStateOf(existingProperty?.paymentType ?: "Efectivo") }
    var description by remember { mutableStateOf(existingProperty?.description ?: "") }
    var rules by remember { mutableStateOf(existingProperty?.rules ?: "") }
    
    // Updated imageUri to handle String paths directly (fix for the bug)
    var imageUri by remember { mutableStateOf(existingProperty?.imageUrl) }
    
    var selectedStatus by remember { mutableStateOf(if (isEditing) existingProperty?.status ?: "AVAILABLE" else "AVAILABLE") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var paymentTypeDropdownExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    
    // Get currency from PreferencesManager
    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")
    
    // OSmdroid Setup
    DisposableEffect(Unit) {
        Configuration.getInstance().load(context, android.preference.PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose { }
    }

    // Map State
    val defaultLocal = GeoPoint(19.4326, -99.1332) // CDMX default
    val selectedPosition = remember { mutableStateOf(if (isEditing && existingProperty?.latitude != 0.0 && existingProperty?.latitude != null) 
            GeoPoint(existingProperty.latitude, existingProperty.longitude) 
        else defaultLocal) }
    
    val mapController = remember { mutableStateOf<org.osmdroid.api.IMapController?>(null) }
    val markerRef = remember { mutableStateOf<OsmdroidMarker?>(null) }
    val mapView = remember { MapView(context) }

    // Lifecycle handling for MapView
    val lifecycleObserver = remember {
        androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> mapView.onResume()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> {}
            }
        }
    }
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    fun searchAddress(query: String) {
        if (query.isBlank()) return
        scope.launch {
            try {
                val addresses = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName(query, 1)
                }
                if (!addresses.isNullOrEmpty()) {
                    val addressObj = addresses[0]
                    val newGeoPoint = GeoPoint(addressObj.latitude, addressObj.longitude)
                    selectedPosition.value = newGeoPoint
                    markerRef.value?.position = newGeoPoint
                    mapController.value?.setZoom(17.0)
                    mapController.value?.animateTo(newGeoPoint)
                    mapView.invalidate()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun updateAddressFromLocation(lat: Double, lon: Double) {
        val newGeoPoint = GeoPoint(lat, lon)
        selectedPosition.value = newGeoPoint
        markerRef.value?.position = newGeoPoint
        scope.launch {
            try {
                val addresses = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(lat, lon, 1)
                }
                if (!addresses.isNullOrEmpty()) {
                    val addressObj = addresses[0]
                    address = addressObj.getAddressLine(0) ?: ""
                }
                mapController.value?.animateTo(newGeoPoint)
                mapView.invalidate()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    val propertyTypes = listOf(
        stringResource(R.string.type_house),
        stringResource(R.string.type_apartment),
        stringResource(R.string.type_commercial),
        stringResource(R.string.type_office),
        stringResource(R.string.type_warehouse),
        stringResource(R.string.type_land)
    )
    
    val statusOptions = listOf(
        "AVAILABLE" to stringResource(R.string.status_available),
        "RENTED" to stringResource(R.string.status_rented),
        "MAINTENANCE" to stringResource(R.string.status_maintenance)
    )

    LaunchedEffect(selectedPosition.value) {
        mapController.value?.animateTo(selectedPosition.value)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) stringResource(R.string.edit_property) else stringResource(R.string.add_property),
                        color = OnBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = Primary)
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

            SectionLabel(stringResource(R.string.property_info))
            NeonTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.property_name),
                leadingIcon = Icons.Default.Home
            )

            // Type dropdown
            ExposedDropdownMenuBox(expanded = typeDropdownExpanded, onExpandedChange = { typeDropdownExpanded = it }) {
                OutlinedTextField(
                    value = type, onValueChange = {}, readOnly = true,
                    label = { Text(stringResource(R.string.property_type), color = OnSurfaceVariant) },
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
                ExposedDropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false },
                    modifier = Modifier.background(SurfaceContainerHigh)
                ) {
                    propertyTypes.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t, color = OnBackground) },
                            onClick = { type = t; typeDropdownExpanded = false }
                        )
                    }
                }
            }

            NeonTextField(
                value = address, 
                onValueChange = { address = it }, 
                label = stringResource(R.string.address), 
                leadingIcon = Icons.Default.LocationOn,
                trailingIcon = {
                    IconButton(onClick = { searchAddress(address) }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_on_map), tint = Primary)
                    }
                }
            )

            SectionLabel(stringResource(R.string.map_location_title))
            Text(stringResource(R.string.map_location_hint), style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerHigh)
                    .border(1.dp, OutlineVariant, RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        mapView.apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(selectedPosition.value)
                            mapController.value = controller
                            
                            val marker = OsmdroidMarker(this)
                            marker.position = selectedPosition.value
                            marker.title = "Ubicación"
                            marker.setAnchor(OsmdroidMarker.ANCHOR_CENTER, OsmdroidMarker.ANCHOR_BOTTOM)
                            overlays.add(marker)
                            markerRef.value = marker
                            
                            val overlayEvents = object : org.osmdroid.views.overlay.MapEventsOverlay(object : org.osmdroid.events.MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                    p?.let { updateAddressFromLocation(it.latitude, it.longitude) }
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint?): Boolean = false
                            }) {}
                            overlays.add(overlayEvents)

                            setOnTouchListener { view, event ->
                                when (event.action) {
                                    android.view.MotionEvent.ACTION_DOWN -> view.parent.requestDisallowInterceptTouchEvent(true)
                                    android.view.MotionEvent.ACTION_UP -> view.parent.requestDisallowInterceptTouchEvent(false)
                                }
                                false
                            }
                        }
                    },
                    update = { mv ->
                        if (markerRef.value?.position != selectedPosition.value) {
                            markerRef.value?.position = selectedPosition.value
                            mv.invalidate()
                        }
                    }
                )
            }

            SectionLabel(stringResource(R.string.details))
            NeonTextField(
                value = monthlyRent, 
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) monthlyRent = it }, 
                label = stringResource(R.string.monthly_rent, currentCurrency.uppercase()),
                leadingIcon = Icons.Default.AttachMoney, 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NeonTextField(
                    value = rooms, 
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) rooms = it }, 
                    label = stringResource(R.string.rooms),
                    leadingIcon = Icons.Default.Bed, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                NeonTextField(
                    value = bathrooms, 
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) bathrooms = it }, 
                    label = stringResource(R.string.bathrooms),
                    leadingIcon = Icons.Default.Bathroom, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            NeonTextField(
                value = area, 
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) area = it }, 
                label = stringResource(R.string.area),
                leadingIcon = Icons.Default.SquareFoot, 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // Payment Type Selector
            SectionLabel("Modalidad de Pago")
            ExposedDropdownMenuBox(
                expanded = paymentTypeDropdownExpanded,
                onExpandedChange = { paymentTypeDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = paymentType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Contrato", color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = Primary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentTypeDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                        focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = paymentTypeDropdownExpanded,
                    onDismissRequest = { paymentTypeDropdownExpanded = false },
                    modifier = Modifier.background(SurfaceContainerHigh)
                ) {
                    listOf("Efectivo", "Anticrético").forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode, color = OnBackground) },
                            onClick = { 
                                paymentType = mode
                                paymentTypeDropdownExpanded = false 
                            }
                        )
                    }
                }
            }

            // Status dropdown
            if (isEditing) {
                ExposedDropdownMenuBox(expanded = statusDropdownExpanded, onExpandedChange = { statusDropdownExpanded = it }) {
                    OutlinedTextField(
                        value = statusOptions.find { it.first == selectedStatus }?.second ?: "",
                        onValueChange = {}, readOnly = true,
                        label = { Text(stringResource(R.string.status), color = OnSurfaceVariant) },
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
            }

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_optional), color = OnSurfaceVariant) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                    focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                    focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = rules, onValueChange = { rules = it },
                label = { Text("Reglas del Inmueble", color = OnSurfaceVariant) },
                placeholder = { Text("Ej: No mascotas, no fiestas...", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                    focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                    focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            )

            SectionLabel(stringResource(R.string.property_photo))
            ImagePickerField(
                label = stringResource(R.string.tap_to_add_photo),
                imageUri = imageUri,
                onImageSelected = { imageUri = it }
            )

            Spacer(Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(listOf(NeonGradientStart, NeonGradientEnd)),
                        alpha = if (isSaving || name.isBlank() || address.isBlank()) 0.5f else 1f
                    )
                    .clickable(enabled = !isSaving && name.isNotBlank() && address.isNotBlank()) {
                        isSaving = true
                        scope.launch {
                            val property = Property(
                                id = existingProperty?.id ?: 0L,
                                name = name, address = address, type = type,
                                monthlyRent = monthlyRent.toDoubleOrNull() ?: 0.0,
                                rooms = rooms.toIntOrNull() ?: 0,
                                bathrooms = bathrooms.toIntOrNull() ?: 0,
                                area = area.toDoubleOrNull() ?: 0.0,
                                latitude = selectedPosition.value.latitude,
                                longitude = selectedPosition.value.longitude,
                                status = selectedStatus, 
                                paymentType = paymentType,
                                description = description,
                                rules = rules,
                                imageUrl = imageUri ?: ""
                            )
                            if (isEditing) viewModel.updateProperty(property) else viewModel.insertProperty(property)
                            // Small delay to ensure DB operation is dispatched
                            kotlinx.coroutines.delay(500)
                            onSuccess()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = OnPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isEditing) stringResource(R.string.save_changes) else stringResource(R.string.add_property_btn),
                        color = OnPrimaryFixed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = Primary,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 8.dp)
    )
}
