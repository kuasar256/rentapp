package com.example.rentapp.ui.screens.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.example.rentapp.R
import com.example.rentapp.data.local.entity.User
import com.example.rentapp.ui.screens.auth.NeonTextField
import com.example.rentapp.ui.screens.property.SectionLabel
import com.example.rentapp.ui.theme.*
import com.example.rentapp.ui.components.RentAppBottomBar
import com.example.rentapp.viewmodel.LanguageViewModel
import com.example.rentapp.data.preferences.PreferencesManager
import com.example.rentapp.auth.BiometricHelper
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavHostController,
    userViewModel: com.example.rentapp.viewmodel.UserViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val languageViewModel = remember { LanguageViewModel(context) }
    
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val currentCurrency by languageViewModel.currentCurrency.collectAsState()
    
    var name by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var phone by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    
    // Config states
    var languageExpanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember(currentLanguage) { 
        mutableStateOf(
            when (currentLanguage) {
                "en" -> "English"
                "pt" -> "Português"
                "fr" -> "Français"
                "de" -> "Deutsch"
                else -> "Español"
            }
        ) 
    }
    
    var currencyExpanded by remember { mutableStateOf(false) }
    var selectedCurrency by remember(currentCurrency) { mutableStateOf(currentCurrency) }
    
    val userState by userViewModel.user.collectAsState()
    
    // Al cargar el usuario del ViewModel, actualizar los estados locales
    LaunchedEffect(userState) {
        userState?.let { u ->
            name = u.name
            email = u.email
            phone = u.phone
            company = u.company
        }
    }
    
    var isSaved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Removed LaunchedEffect for room user

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.user_profile), fontWeight = FontWeight.Bold, color = OnBackground) },
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
        bottomBar = {
            RentAppBottomBar(navController = navController)
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
            NeonTextField(value = name, onValueChange = { name = it }, label = "Nombre Completo", leadingIcon = Icons.Default.Person)
            NeonTextField(value = email, onValueChange = { email = it }, label = "Correo", leadingIcon = Icons.Default.Email)
            NeonTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Teléfono",
                leadingIcon = Icons.Default.Phone,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
            )
            NeonTextField(value = company, onValueChange = { company = it }, label = "Empresa", leadingIcon = Icons.Default.Business)
            
            Spacer(Modifier.height(16.dp))
            SectionLabel("Configuración de Interfaz")
            
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
                    listOf("Español", "English", "Português", "Français", "Deutsch").forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang) },
                            onClick = {
                                selectedLanguage = lang
                                languageViewModel.setLanguage(
                                    when (lang) {
                                        "English" -> "en"
                                        "Português" -> "pt"
                                        "Français" -> "fr"
                                        "Deutsch" -> "de"
                                        else -> "es"
                                    }
                                )
                                languageExpanded = false
                            }
                        )
                    }
                }
            }

            // --- Biometría ---
            val scope = rememberCoroutineScope()
            val biometricEnabled by PreferencesManager.getBiometricEnabledFlow(context).collectAsState(initial = false)

            Spacer(Modifier.height(8.dp))
            SectionLabel("Seguridad")
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Tertiary)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bloqueo Biométrico", style = MaterialTheme.typography.bodyLarge, color = OnBackground)
                    Text("Proteger inicio de la aplicación", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
                Switch(
                    checked = biometricEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (BiometricHelper.canAuthenticate(context)) {
                                BiometricHelper.showBiometricPrompt(
                                    activity = context as FragmentActivity,
                                    onSuccess = {
                                        scope.launch {
                                            PreferencesManager.setBiometricEnabled(context, true)
                                            // Actualizar timestamp para no pedirla inmediatamente
                                            PreferencesManager.setLastAuthTimestamp(context, System.currentTimeMillis())
                                        }
                                    }
                                )
                            }
                        } else {
                            scope.launch { PreferencesManager.setBiometricEnabled(context, false) }
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = Primary.copy(alpha = 0.5f))
                )
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
                    listOf("USD", "BOB", "MXN").forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency) },
                            onClick = {
                                selectedCurrency = currency
                                languageViewModel.setCurrency(currency)
                                currencyExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    val updatedUser = User(
                        id = userState?.id ?: 0,
                        name = name,
                        email = email,
                        phone = phone,
                        company = company,
                        rfc = userState?.rfc ?: "",
                        userType = "Landlord"
                    )
                    
                    if (userState != null) {
                        userViewModel.updateUser(updatedUser)
                    } else {
                        userViewModel.insertUser(updatedUser)
                    }
                    
                    isSaved = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Cambios", color = OnPrimaryFixed, fontWeight = FontWeight.Bold)
            }
            
            if (isSaved) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    isSaved = false
                }
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Primary,
                    contentColor = OnPrimaryFixed
                ) {
                    Text("Cambios guardados localmente", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
