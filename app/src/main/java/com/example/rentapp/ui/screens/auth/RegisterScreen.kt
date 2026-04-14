package com.example.rentapp.ui.screens.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import com.example.rentapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta", color = OnBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SurfaceContainer, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Primary, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Información Personal", style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceVariant, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(12.dp))

            NeonTextField(value = name, onValueChange = { name = it }, label = "Nombre Completo", leadingIcon = Icons.Default.Person)
            Spacer(Modifier.height(12.dp))
            NeonTextField(value = email, onValueChange = { email = it }, label = "Correo Electrónico",
                leadingIcon = Icons.Default.Email, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(Modifier.height(12.dp))
            NeonTextField(value = phone, onValueChange = { phone = it }, label = "Teléfono",
                leadingIcon = Icons.Default.Phone, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            Spacer(Modifier.height(12.dp))
            NeonTextField(value = company, onValueChange = { company = it }, label = "Empresa / Razón Social",
                leadingIcon = Icons.Default.Business)

            Spacer(Modifier.height(24.dp))
            Text("Seguridad", style = MaterialTheme.typography.titleMedium,
                color = OnSurfaceVariant, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(12.dp))

            NeonTextField(
                value = password, onValueChange = { password = it }, label = "Contraseña",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = OnSurfaceVariant)
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
            NeonTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it },
                label = "Confirmar Contraseña", leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { isLoading = true; onRegisterSuccess() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = OnPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Crear Cuenta", color = OnPrimaryFixed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya tienes cuenta? ", color = OnSurfaceVariant)
                Text("Inicia Sesión", color = Primary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
