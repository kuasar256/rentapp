package com.example.rentapp.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rentapp.ui.screens.auth.NeonTextField
import com.example.rentapp.ui.screens.property.SectionLabel
import com.example.rentapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var name by remember { mutableStateOf("Admin Username") }
    var email by remember { mutableStateOf("admin@rentapp.com") }
    
    // Config states
    var languageExpanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("Español") }
    
    var currencyExpanded by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("USD") }
    
    var isDarkMode by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil y Ajustes", fontWeight = FontWeight.Bold, color = OnBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión", tint = Error)
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
            SectionLabel("Datos de Usuario")
            NeonTextField(value = name, onValueChange = { name = it }, label = "Nombre del Administrador", leadingIcon = Icons.Default.Person)
            NeonTextField(value = email, onValueChange = { email = it }, label = "Correo", leadingIcon = Icons.Default.Email)
            
            Spacer(Modifier.height(16.dp))
            SectionLabel("Configuración de Interfaz")
            
            // Tema Oscuro / Claro
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, contentDescription = null, tint = Tertiary)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tema Oscuro", style = MaterialTheme.typography.bodyLarge, color = OnBackground)
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { isDarkMode = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = Primary.copy(alpha = 0.5f))
                )
            }
            
            // Selector de Idioma
            ExposedDropdownMenuBox(expanded = languageExpanded, onExpandedChange = { languageExpanded = it }) {
                OutlinedTextField(
                    value = selectedLanguage, onValueChange = {}, readOnly = true,
                    label = { Text("Idioma", color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null, tint = OnSurfaceVariant) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                        focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                    listOf("Español", "English").forEach { lang ->
                        DropdownMenuItem(text = { Text(lang) }, onClick = { selectedLanguage = lang; languageExpanded = false })
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            SectionLabel("Moneda Base")
            Text(
                "Elija la moneda principal de operaciones. Los precios se convertirán y mostrarán de acuerdo a esto.",
                style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant
            )
            ExposedDropdownMenuBox(expanded = currencyExpanded, onExpandedChange = { currencyExpanded = it }) {
                OutlinedTextField(
                    value = selectedCurrency, onValueChange = {}, readOnly = true,
                    label = { Text("Moneda", color = OnSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = OnSurfaceVariant) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnBackground, unfocusedTextColor = OnBackground,
                        focusedBorderColor = Primary, unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = SurfaceContainer, unfocusedContainerColor = SurfaceContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                    listOf("USD", "Bs", "MXN").forEach { currency ->
                        DropdownMenuItem(text = { Text(currency) }, onClick = { selectedCurrency = currency; currencyExpanded = false })
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Cambios", color = OnPrimaryFixed, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
