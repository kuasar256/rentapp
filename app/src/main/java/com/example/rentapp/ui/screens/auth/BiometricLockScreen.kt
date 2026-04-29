package com.example.rentapp.ui.screens.auth

import android.app.Activity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.rentapp.auth.BiometricHelper
import com.example.rentapp.ui.theme.*

@Composable
fun BiometricLockScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    // Animación de pulso en el ícono de huella
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Lanzar biometría automáticamente al mostrar la pantalla
    LaunchedEffect(Unit) {
        if (activity != null && BiometricHelper.canAuthenticate(context)) {
            isAuthenticating = true
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "Acceso a RentApp",
                subtitle = "Verifique su identidad para continuar",
                onSuccess = {
                    android.util.Log.d("RentAppDebug", "BiometricLockScreen: success callback received")
                    isAuthenticating = false
                    (activity as? com.example.rentapp.MainActivity)?.onBiometricSuccess()
                    onAuthSuccess()
                },
                onError = { errorCode, errString ->
                    isAuthenticating = false
                    // Errores que no requieren mostrar mensaje (cancelación manual)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        errorMessage = errString
                    }
                },
                onFailed = {
                    isAuthenticating = false
                    errorMessage = "Autenticación no reconocida. Intente de nuevo."
                }
            )
        } else if (activity == null || !BiometricHelper.canAuthenticate(context)) {
            // Sin hardware biométrico disponible → acceso directo
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Background, SurfaceVariant, Background)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            // Ícono de candado
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Primary.copy(alpha = 0.25f), Primary.copy(alpha = 0.05f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "App bloqueada",
                    tint = Primary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Text(
                text = "RentApp",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OnBackground
            )

            Text(
                text = "Verificación de seguridad requerida",
                fontSize = 14.sp,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Botón de huella dactilar con animación de pulso
            IconButton(
                onClick = {
                    if (activity != null && !isAuthenticating) {
                        isAuthenticating = true
                        errorMessage = null
                        BiometricHelper.showBiometricPrompt(
                            activity = activity,
                            onSuccess = {
                                isAuthenticating = false
                                (activity as? com.example.rentapp.MainActivity)?.onBiometricSuccess()
                                onAuthSuccess()
                            },
                            onError = { _, errString ->
                                isAuthenticating = false
                                errorMessage = errString
                            },
                            onFailed = {
                                isAuthenticating = false
                                errorMessage = "Biometría no reconocida. Intente de nuevo."
                            }
                        )
                    }
                },
                modifier = Modifier
                    .size(80.dp)
                    .scale(if (isAuthenticating) pulse else 1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Autenticar con huella",
                    tint = if (isAuthenticating) Primary else OnSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
            }

            errorMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = msg,
                        color = Error,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            // Botón manual para reintentar
            if (!isAuthenticating) {
                OutlinedButton(
                    onClick = {
                        if (activity != null) {
                            isAuthenticating = true
                            errorMessage = null
                            BiometricHelper.showBiometricPrompt(
                                activity = activity,
                                onSuccess = {
                                    isAuthenticating = false
                                    (activity as? com.example.rentapp.MainActivity)?.onBiometricSuccess()
                                    onAuthSuccess()
                                },
                                onError = { _, errString ->
                                    isAuthenticating = false
                                    errorMessage = errString
                                },
                                onFailed = {
                                    isAuthenticating = false
                                    errorMessage = "Biometría no reconocida."
                                }
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text("Intentar de nuevo", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
