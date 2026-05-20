package com.example.rentapp.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentapp.data.preferences.PreferencesManager
import com.example.rentapp.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pages = listOf(
        OnboardingPage.Welcome,
        OnboardingPage.Management,
        OnboardingPage.Analytics,
        OnboardingPage.Automation
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Background, SurfaceContainer)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                OnboardingPageContent(pages[index])
            }

            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(pages.size) { index ->
                        val color = if (pagerState.currentPage == index) Primary else OutlineVariant
                        val width = if (pagerState.currentPage == index) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Next / Finish Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            scope.launch {
                                PreferencesManager.setOnboardingCompleted(context, true)
                                onFinish()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "Empezar" else "Siguiente",
                        color = OnPrimaryFixed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (pagerState.currentPage < pages.size - 1) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = OnPrimaryFixed)
                    }
                }
            }
        }
        
        // Skip button
        if (pagerState.currentPage < pages.size - 1) {
            TextButton(
                onClick = {
                    scope.launch {
                        PreferencesManager.setOnboardingCompleted(context, true)
                        onFinish()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("Saltar", color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape),
            color = Primary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = OnBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

sealed class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    object Welcome : OnboardingPage(
        "Bienvenido a RentApp",
        "La herramienta definitiva para propietarios que buscan profesionalizar la gestión de sus rentas.",
        Icons.Default.HomeWork
    )
    object Management : OnboardingPage(
        "Gestión Simplificada",
        "Administra propiedades, inquilinos y contratos en un solo lugar. Todo bajo control.",
        Icons.Default.Dashboard
    )
    object Analytics : OnboardingPage(
        "Analítica Avanzada",
        "Visualiza tus ingresos anuales con gráficos interactivos y genera reportes PDF profesionales.",
        Icons.Default.BarChart
    )
    object Automation : OnboardingPage(
        "Automatización Total",
        "Recordatorios automáticos de pagos pendientes y sincronización en la nube para tu tranquilidad.",
        Icons.Default.NotificationsActive
    )
}
