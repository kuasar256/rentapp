package com.example.rentapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentapp.data.local.dao.MonthlyEarning
import com.example.rentapp.ui.theme.Primary
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import com.example.rentapp.ui.theme.*

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon

@Composable
fun AnnualLineChart(
    monthlyEarnings: List<MonthlyEarning>,
    modifier: Modifier = Modifier
) {
    val maxEarning = (monthlyEarnings.maxOfOrNull { it.total } ?: 1.0).coerceAtLeast(1.0)
    val months = listOf("E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
    
    val primaryColor = Primary
    val surfaceColor = SurfaceContainerHighest

    // Get statistics for the header
    val currentPlayers = monthlyEarnings.lastOrNull()?.total ?: 0.0
    val peak24h = monthlyEarnings.maxOfOrNull { it.total } ?: 0.0
    val allTimePeak = peak24h // For this mockup we use the same

    Column(modifier = modifier.fillMaxWidth()) {
        // Header with stats (Steam style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = String.format("%,.0f", currentPlayers),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Text(
                    "ingresos este mes",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%,.0f", peak24h),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Text(
                    "pico anual",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }

        // Zoom/Filter bar
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Zoom", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
            listOf("48h", "1w", "1m", "3m", "6m", "1y", "MAX").forEach { label ->
                val isSelected = label == "MAX"
                Surface(
                    onClick = {},
                    color = if (isSelected) Color(0xFF1b2838) else Color.Transparent,
                    shape = RoundedCornerShape(4.dp),
                    border = if (isSelected) null else BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Primary else OnSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF0b0e13), RoundedCornerShape(4.dp))
                .padding(vertical = 16.dp)
        ) {
            // Background Grid Lines
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(5) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                }
            }

            // Line Chart with Smooth Curves
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height
                val spacing = width / 11f
                
                val points = (1..12).map { month ->
                    val earning = monthlyEarnings.find { it.month == month }?.total ?: 0.0
                    val x = (month - 1) * spacing
                    // Add a tiny bit of "noise" if the value is 0 to avoid perfect flatness
                    val displayEarning = if (earning == 0.0) 0.05 * maxEarning else earning
                    val y = height - (displayEarning.toFloat() / maxEarning.toFloat() * height * 0.8f) - (height * 0.1f)
                    androidx.compose.ui.geometry.Offset(x, y)
                }

                val path = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points[0].x, points[0].y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val controlPoint1 = androidx.compose.ui.geometry.Offset(p0.x + (p1.x - p0.x) / 2, p0.x)
                            val controlPoint2 = androidx.compose.ui.geometry.Offset(p0.x + (p1.x - p0.x) / 2, p1.y)
                            
                            // Using cubicTo for smooth Steam-like curves
                            cubicTo(
                                x1 = p0.x + (p1.x - p0.x) / 2f,
                                y1 = p0.y,
                                x2 = p0.x + (p1.x - p0.x) / 2f,
                                y2 = p1.y,
                                x3 = p1.x,
                                y3 = p1.y
                            )
                        }
                    }
                }

                // Area Fill
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent)
                    )
                )

                // Main Line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Markers (the dots on the line)
                points.forEach { point ->
                    drawCircle(
                        color = Color.Black,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 2.dp.toPx(),
                        center = point
                    )
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            months.forEach { month ->
                Text(
                    text = month,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = OnSurfaceVariant
                )
            }
        }

        // Legend (Steam Style)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(12.dp, 2.dp).background(Primary))
            Spacer(Modifier.width(8.dp))
            Text("Ingresos", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Spacer(Modifier.width(24.dp))
            Box(modifier = Modifier.size(8.dp).background(Color(0xFF4d5a27), androidx.compose.foundation.shape.CircleShape))
            Spacer(Modifier.width(8.dp))
            Text("Marcadores", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
    }
}

@Composable
fun RevenueDecayChart(
    totalRevenue: Double,
    repairCosts: Double,
    currencyFormatter: java.text.NumberFormat,
    modifier: Modifier = Modifier
) {
    val remaining = (totalRevenue - repairCosts).coerceAtLeast(0.0)
    val decayPercentage = if (totalRevenue > 0) (repairCosts / totalRevenue).toFloat().coerceIn(0f, 1f) else 0f
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Impacto de Reparaciones en Ingresos",
            style = MaterialTheme.typography.labelSmall,
            color = Tertiary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceContainerHighest)
        ) {
            // Base Revenue (Total)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Primary.copy(alpha = 0.2f))
            )
            
            // Remaining Revenue
            Box(
                modifier = Modifier
                    .fillMaxWidth(1f - decayPercentage)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Primary, Primary.copy(alpha = 0.7f))
                        )
                    )
            )
            
            // Decay Marker
            if (decayPercentage > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(decayPercentage)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .background(Error.copy(alpha = 0.4f))
                        .border(1.dp, Error.copy(alpha = 0.6f))
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Ingreso Neto", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Text(
                    currencyFormatter.format(remaining),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Primary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Costo Reparaciones", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Text(
                    "- ${currencyFormatter.format(repairCosts)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Error
                )
            }
        }
        
        if (decayPercentage > 0.3f) {
            Surface(
                modifier = Modifier.padding(top = 12.dp),
                color = Error.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Las reparaciones consumen el ${(decayPercentage * 100).toInt()}% de los ingresos.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AnnualBarChart(

    monthlyEarnings: List<MonthlyEarning>,
    modifier: Modifier = Modifier
) {
    val maxEarning = monthlyEarnings.maxOfOrNull { it.total } ?: 1.0
    val months = listOf("E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 24.dp)
        ) {
            // Background Grid Lines
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(4) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp)
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                for (i in 1..12) {
                    val earning = monthlyEarnings.find { it.month == i }?.total ?: 0.0
                    val ratio = if (maxEarning > 0) (earning / maxEarning).toFloat() else 0f
                    
                    val barHeight by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        label = "barHeight"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (earning > 0 && ratio > 0.1f) {
                            Text(
                                text = if (earning >= 1000) "${(earning/1000).toInt()}k" else earning.toInt().toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(barHeight.coerceAtLeast(0.01f))
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Primary, Secondary)
                                    )
                                )
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            months.forEach { month ->
                Text(
                    text = month,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
