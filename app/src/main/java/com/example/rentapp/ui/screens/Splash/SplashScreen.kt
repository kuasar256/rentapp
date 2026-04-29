package com.example.rentapp.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentapp.R
import com.example.rentapp.data.preferences.PreferencesManager
import com.example.rentapp.ui.navigation.Screen
import com.example.rentapp.ui.theme.Background
import com.example.rentapp.ui.theme.Primary
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToBiometric: () -> Unit
) {
    val context = LocalContext.current
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = { OvershootInterpolator(2f).getInterpolation(it) }
            )
        )
        delay(1500) // Tiempo de visibilidad del logo

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onNavigateToLogin()
        } else {
            val biometricEnabled = PreferencesManager.getBiometricEnabledFlow(context).first()
            if (biometricEnabled) {
                onNavigateToBiometric()
            } else {
                onNavigateToDashboard()
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Reemplaza con tu logo si tienes uno
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo",
                tint = Primary,
                modifier = Modifier.size(120.dp).scale(scale.value)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "RENT APP",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Primary,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Primary,
                strokeWidth = 3.dp
            )
        }
    }
}

class OvershootInterpolator(private val tension: Float) : android.view.animation.Interpolator {
    override fun getInterpolation(t: Float): Float {
        var tValue = t
        tValue -= 1.0f
        return tValue * tValue * ((tension + 1) * tValue + tension) + 1.0f
    }
}
