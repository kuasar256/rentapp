package com.example.rentapp.ui.screens.payment

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.Payment
import com.example.rentapp.ui.screens.auth.NeonTextField
import com.example.rentapp.ui.theme.*
import com.example.rentapp.viewmodel.PaymentViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    contractId: Long,
    paymentViewModel: PaymentViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Efectivo") }
    var methodDropdownExpanded by remember { mutableStateOf(false) }

    // Date Picker
    var paidDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = paidDate)

    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val paymentMethods = listOf("Efectivo", "Transferencia", "Tarjeta", "Cheque")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_payment), color = OnBackground, fontWeight = FontWeight.Bold) },
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

            Text(
                text = stringResource(R.string.details).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = Primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Amount
            NeonTextField(
                value = amount,
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() || char == '.' }) amount = it },
                label = stringResource(R.string.payment_amount),
                leadingIcon = Icons.Default.AttachMoney,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // Date
            OutlinedTextField(
                value = dateFormatter.format(Date(paidDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.payment_date), color = OnSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Primary) },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = OnBackground,
                    disabledBorderColor = OutlineVariant,
                    disabledContainerColor = SurfaceContainer,
                    disabledLeadingIconColor = Primary,
                    disabledLabelColor = OnSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Payment Method Dropdown
            ExposedDropdownMenuBox(
                expanded = methodDropdownExpanded, 
                onExpandedChange = { methodDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = paymentMethod, 
                    onValueChange = {}, 
                    readOnly = true,
                    label = { Text(stringResource(R.string.payment_method), color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = Primary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                        focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = methodDropdownExpanded,
                    onDismissRequest = { methodDropdownExpanded = false },
                    modifier = Modifier.background(SurfaceContainerHigh)
                ) {
                    paymentMethods.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method, color = OnBackground) },
                            onClick = { 
                                paymentMethod = method
                                methodDropdownExpanded = false 
                            }
                        )
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text(stringResource(R.string.reference_notes), color = OnSurfaceVariant) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                    focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                    focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))

            val isFormValid = amount.isNotBlank() && amount.toDoubleOrNull() != null

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(listOf(NeonGradientStart, NeonGradientEnd)),
                        alpha = if (isSaving || !isFormValid) 0.5f else 1f
                    )
                    .clickable(enabled = !isSaving && isFormValid) {
                        isSaving = true
                        scope.launch {
                            val calendar = Calendar.getInstance().apply { timeInMillis = paidDate }
                            val newPayment = Payment(
                                contractId = contractId,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                dueDate = paidDate, // Assuming due date matches paid date for ad-hoc payments
                                paidDate = paidDate,
                                status = "PAID",
                                month = calendar.get(Calendar.MONTH) + 1,
                                year = calendar.get(Calendar.YEAR),
                                paymentMethod = paymentMethod,
                                notes = notes
                            )
                            paymentViewModel.insertPayment(newPayment)
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
                        text = stringResource(R.string.register_payment),
                        color = OnPrimaryFixed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { paidDate = it }
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
    }
}
