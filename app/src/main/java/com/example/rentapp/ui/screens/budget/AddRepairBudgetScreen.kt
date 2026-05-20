package com.example.rentapp.ui.screens.budget

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.RepairBudget
import com.example.rentapp.ui.screens.auth.NeonTextField
import com.example.rentapp.ui.screens.property.SectionLabel
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PropertyViewModel
import com.example.rentapp.viewmodel.RepairBudgetViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRepairBudgetScreen(
    viewModel: RepairBudgetViewModel,
    propertyViewModel: PropertyViewModel,
    propertyId: Long? = null,
    editBudgetId: Long? = null,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val allProperties by propertyViewModel.allProperties.collectAsState()
    
    val isEditing = editBudgetId != null
    val existingBudget = if (isEditing) {
        viewModel.allBudgets.collectAsState().value.find { it.id == editBudgetId }
    } else null

    var selectedPropertyId by remember { mutableStateOf(propertyId ?: existingBudget?.propertyId ?: -1L) }
    var description by remember { mutableStateOf(existingBudget?.description ?: "") }
    var estimatedCost by remember { mutableStateOf(existingBudget?.estimatedCost?.toString() ?: "") }
    var status by remember { mutableStateOf(existingBudget?.status ?: "PENDING") }
    var provider by remember { mutableStateOf(existingBudget?.provider ?: "") }

    var propertyExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Get currency from PreferencesManager
    val context = LocalContext.current
    val currentCurrency by com.example.rentapp.data.preferences.PreferencesManager.getCurrencyFlow(context).collectAsState(initial = "USD")

    val statusOptions = listOf(
        "PENDING" to stringResource(R.string.status_pending),
        "APPROVED" to stringResource(R.string.status_approved),
        "IN_PROGRESS" to stringResource(R.string.status_in_progress),
        "COMPLETED" to stringResource(R.string.status_completed)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) stringResource(R.string.edit_budget) else stringResource(R.string.add_budget),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Primary)
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

            SectionLabel(stringResource(R.string.dashboard_properties))
            Spacer(Modifier.height(4.dp))

            // Property Selection
            ExposedDropdownMenuBox(
                expanded = propertyExpanded,
                onExpandedChange = { if (!isEditing && propertyId == null) propertyExpanded = it }
            ) {
                val selectedPropertyName = allProperties.find { it.id == selectedPropertyId }?.name ?: stringResource(R.string.select_property)
                OutlinedTextField(
                    value = selectedPropertyName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.dashboard_properties), color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Primary) },
                    trailingIcon = { if (!isEditing && propertyId == null) ExposedDropdownMenuDefaults.TrailingIcon(expanded = propertyExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                        focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (!isEditing && propertyId == null) {
                    ExposedDropdownMenu(
                        expanded = propertyExpanded,
                        onDismissRequest = { propertyExpanded = false },
                        modifier = Modifier.background(SurfaceContainerHigh)
                    ) {
                        allProperties.forEach { prop ->
                            DropdownMenuItem(
                                text = { Text(prop.name, color = OnBackground) },
                                onClick = {
                                    selectedPropertyId = prop.id
                                    propertyExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            SectionLabel(stringResource(R.string.details))
            Spacer(Modifier.height(4.dp))

            NeonTextField(
                value = description,
                onValueChange = { description = it },
                label = stringResource(R.string.repair_description),
                leadingIcon = Icons.Default.Description
            )

            NeonTextField(
                value = estimatedCost,
                onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) estimatedCost = it },
                label = stringResource(R.string.estimated_cost),
                leadingIcon = if (currentCurrency == "BOB") Icons.Default.Payments else Icons.Default.AttachMoney,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                val statusLabel = statusOptions.find { it.first == status }?.second ?: status
                OutlinedTextField(
                    value = statusLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.repair_status), color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = Primary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                        focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false },
                    modifier = Modifier.background(SurfaceContainerHigh)
                ) {
                    statusOptions.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label, color = OnBackground) },
                            onClick = {
                                status = key
                                statusExpanded = false
                            }
                        )
                    }
                }
            }

            NeonTextField(
                value = provider,
                onValueChange = { provider = it },
                label = stringResource(R.string.provider),
                leadingIcon = Icons.Default.Engineering
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedPropertyId != -1L && description.isNotBlank()) {
                        isSaving = true
                        val budget = RepairBudget(
                            id = if (isEditing) (editBudgetId ?: 0L) else 0L,
                            propertyId = selectedPropertyId,
                            description = description,
                            estimatedCost = estimatedCost.toDoubleOrNull() ?: 0.0,
                            actualCost = existingBudget?.actualCost,
                            status = status,
                            provider = provider,
                            notes = existingBudget?.notes
                        )
                        scope.launch {
                            try {
                                if (isEditing) viewModel.updateBudget(budget) else viewModel.addBudget(budget)
                                onSuccess()
                            } catch (e: Exception) {
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(NeonGradientStart, NeonGradientEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = OnPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = stringResource(R.string.save_budget),
                            color = OnPrimaryFixed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
