package com.example.rentapp.ui.screens.property

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.PropertyCondition
import com.example.rentapp.ui.components.DocumentGallery
import com.example.rentapp.ui.components.persistImageLocally
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PropertyConditionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyConditionScreen(
    propertyId: Long,
    contractId: Long? = null,
    initialType: String = "CHECK_IN",
    viewModel: PropertyConditionViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onViewPhotos: (List<String>, Int) -> Unit = { _, _ -> }
) {
    var type by remember { mutableStateOf(initialType) }
    var notes by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var imageUris by remember { mutableStateOf<List<String>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val types = listOf("CHECK_IN", "CHECK_OUT")
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_condition_report), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Selector
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (type == "CHECK_IN") stringResource(R.string.check_in) else stringResource(R.string.check_out),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.condition_type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = OutlineVariant
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SurfaceContainerHigh)
                ) {
                    types.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(if (t == "CHECK_IN") stringResource(R.string.check_in) else stringResource(R.string.check_out)) },
                            onClick = {
                                type = t
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Date Picker
            val dateInteractionSource = remember { MutableInteractionSource() }
            val isPressed by dateInteractionSource.collectIsPressedAsState()
            LaunchedEffect(isPressed) {
                if (isPressed) showDatePicker = true
            }

            OutlinedTextField(
                value = dateFormat.format(Date(date)),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.inspection_date)) },
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Primary) },
                modifier = Modifier.fillMaxWidth(),
                interactionSource = dateInteractionSource,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OutlineVariant,
                    focusedLabelColor = Primary
                )
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.condition_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OutlineVariant
                )
            )

            // Photos
            Text(
                stringResource(R.string.condition_photos),
                style = MaterialTheme.typography.labelMedium,
                color = Primary,
                fontWeight = FontWeight.Bold
            )

            val context = LocalContext.current
            val multiImageLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetMultipleContents()
            ) { uris ->
                val localPaths = uris.mapNotNull { persistImageLocally(context, it) }
                imageUris = imageUris + localPaths
            }

            DocumentGallery(
                uris = imageUris,
                onAddClick = { multiImageLauncher.launch("image/*") },
                onImageClick = { index -> onViewPhotos(imageUris, index) }
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.insertCondition(
                        PropertyCondition(
                            propertyId = propertyId,
                            contractId = contractId ?: 0,
                            type = type,
                            notes = notes,
                            date = date,
                            imageUris = imageUris.joinToString(",")
                        )
                    )
                    onSuccess()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(stringResource(R.string.save_condition_report), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
