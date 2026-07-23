package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class ThreatAxis(
    val name: String,
    val value: Float, // 0.0f to 1.0f
    val displayValue: String
)

enum class ThreatPreset {
    NORMAL,
    DDOS_SPIKE,
    QUANTUM_COMPROMISE,
    EXFILTRATION_SURGE,
    PORT_SCAN_BURST
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThreatSpiderChartDashboardCard() {
    var selectedPreset by remember { mutableStateOf(ThreatPreset.NORMAL) }
    var isLiveSimulationActive by remember { mutableStateOf(true) }

    // Dynamic metrics state
    var heuristicScore by remember { mutableStateOf(0.22f) }
    var quantumVariance by remember { mutableStateOf(0.18f) }
    var correlationIndex by remember { mutableStateOf(0.30f) }
    var flowVelocity by remember { mutableStateOf(0.25f) }
    var payloadEntropy by remember { mutableStateOf(0.35f) }
    var latencySpike by remember { mutableStateOf(0.15f) }

    // Apply preset values
    fun applyPreset(preset: ThreatPreset) {
        selectedPreset = preset
        when (preset) {
            ThreatPreset.NORMAL -> {
                heuristicScore = 0.20f
                quantumVariance = 0.15f
                correlationIndex = 0.25f
                flowVelocity = 0.30f
                payloadEntropy = 0.28f
                latencySpike = 0.12f
            }
            ThreatPreset.DDOS_SPIKE -> {
                heuristicScore = 0.85f
                quantumVariance = 0.40f
                correlationIndex = 0.90f
                flowVelocity = 0.95f
                payloadEntropy = 0.50f
                latencySpike = 0.88f
            }
            ThreatPreset.QUANTUM_COMPROMISE -> {
                heuristicScore = 0.60f
                quantumVariance = 0.98f
                correlationIndex = 0.75f
                flowVelocity = 0.45f
                payloadEntropy = 0.85f
                latencySpike = 0.35f
            }
            ThreatPreset.EXFILTRATION_SURGE -> {
                heuristicScore = 0.70f
                quantumVariance = 0.30f
                correlationIndex = 0.82f
                flowVelocity = 0.65f
                payloadEntropy = 0.92f
                latencySpike = 0.40f
            }
            ThreatPreset.PORT_SCAN_BURST -> {
                heuristicScore = 0.78f
                quantumVariance = 0.20f
                correlationIndex = 0.65f
                flowVelocity = 0.88f
                payloadEntropy = 0.40f
                latencySpike = 0.25f
            }
        }
    }

    // Periodic live simulation effect
    LaunchedEffect(isLiveSimulationActive, selectedPreset) {
        while (isLiveSimulationActive) {
            delay(1200L)
            if (selectedPreset == ThreatPreset.NORMAL) {
                heuristicScore = (0.15f + Math.random().toFloat() * 0.15f).coerceIn(0f, 1f)
                quantumVariance = (0.10f + Math.random().toFloat() * 0.12f).coerceIn(0f, 1f)
                correlationIndex = (0.20f + Math.random().toFloat() * 0.15f).coerceIn(0f, 1f)
                flowVelocity = (0.20f + Math.random().toFloat() * 0.20f).coerceIn(0f, 1f)
                payloadEntropy = (0.25f + Math.random().toFloat() * 0.15f).coerceIn(0f, 1f)
                latencySpike = (0.10f + Math.random().toFloat() * 0.10f).coerceIn(0f, 1f)
            } else {
                // Add subtle organic jitter to active threat preset
                heuristicScore = (heuristicScore + (Math.random().toFloat() - 0.5f) * 0.08f).coerceIn(0.1f, 1.0f)
                quantumVariance = (quantumVariance + (Math.random().toFloat() - 0.5f) * 0.08f).coerceIn(0.1f, 1.0f)
                correlationIndex = (correlationIndex + (Math.random().toFloat() - 0.5f) * 0.08f).coerceIn(0.1f, 1.0f)
                flowVelocity = (flowVelocity + (Math.random().toFloat() - 0.5f) * 0.08f).coerceIn(0.1f, 1.0f)
                payloadEntropy = (payloadEntropy + (Math.random().toFloat() - 0.5f) * 0.08f).coerceIn(0.1f, 1.0f)
                latencySpike = (latencySpike + (Math.random().toFloat() - 0.5f) * 0.08f).coerceIn(0.1f, 1.0f)
            }
        }
    }

    val axes = listOf(
        ThreatAxis("Heuristic", heuristicScore, "${(heuristicScore * 100).toInt()}%"),
        ThreatAxis("Quantum Var", quantumVariance, "${(quantumVariance * 100).toInt()}%"),
        ThreatAxis("Correlation", correlationIndex, "${(correlationIndex * 100).toInt()}%"),
        ThreatAxis("Flow Rate", flowVelocity, "${(flowVelocity * 100).toInt()}%"),
        ThreatAxis("Payload Ent", payloadEntropy, "${(payloadEntropy * 100).toInt()}%"),
        ThreatAxis("Latency Spike", latencySpike, "${(latencySpike * 100).toInt()}%")
    )

    val maxScore = axes.maxOf { it.value }
    val (threatLevel, threatColor) = when {
        maxScore > 0.80f -> "CRITICAL ANOMALY" to Color(0xFFEF4444)
        maxScore > 0.60f -> "HIGH RISK" to Color(0xFFF59E0B)
        maxScore > 0.35f -> "MODERATE" to Color(0xFF38BDF8)
        else -> "LOW / NORMAL" to Color(0xFF10B981)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("spider_chart_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Threat Radar",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "IDS Anomaly Spider Chart",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Real-Time 6-Axis Threat Vector Analytics",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                // Threat Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(threatColor.copy(alpha = 0.2f))
                        .border(1.dp, threatColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = threatLevel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = threatColor,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            // Preset Selector Chips
            Text(
                text = "Simulate Network Threat Vectors:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = selectedPreset == ThreatPreset.NORMAL,
                    onClick = { applyPreset(ThreatPreset.NORMAL) },
                    label = { Text("Normal Baseline", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
                FilterChip(
                    selected = selectedPreset == ThreatPreset.DDOS_SPIKE,
                    onClick = { applyPreset(ThreatPreset.DDOS_SPIKE) },
                    label = { Text("DDoS Burst", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFDC2626),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
                FilterChip(
                    selected = selectedPreset == ThreatPreset.QUANTUM_COMPROMISE,
                    onClick = { applyPreset(ThreatPreset.QUANTUM_COMPROMISE) },
                    label = { Text("Quantum Key Anomaly", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD97706),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
                FilterChip(
                    selected = selectedPreset == ThreatPreset.EXFILTRATION_SURGE,
                    onClick = { applyPreset(ThreatPreset.EXFILTRATION_SURGE) },
                    label = { Text("Data Exfiltration", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF7C3AED),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Spider Radar Chart
            SpiderChartCanvas(
                axes = axes,
                accentColor = threatColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Breakdown Grid of Threat Vectors
            Text(
                text = "Axis Breakdown Metrics:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                axes.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { axis ->
                            Box(modifier = Modifier.weight(1f)) {
                                AxisMetricItem(axis = axis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpiderChartCanvas(
    axes: List<ThreatAxis>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    // Smooth animated values for each vertex
    val animatedValues = axes.map { axis ->
        animateFloatAsState(
            targetValue = axis.value,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            label = "axis_anim_${axis.name}"
        ).value
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (size.width.coerceAtMost(size.height) / 2f) * 0.68f
        val numAxes = axes.size
        val angleStep = (2 * Math.PI / numAxes).toFloat()

        // Draw concentric polygon web rings (20%, 40%, 60%, 80%, 100%)
        val rings = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
        rings.forEach { scale ->
            val webPath = Path()
            for (i in 0 until numAxes) {
                val angle = i * angleStep - (Math.PI / 2).toFloat()
                val x = center.x + radius * scale * cos(angle)
                val y = center.y + radius * scale * sin(angle)
                if (i == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
            }
            webPath.close()

            drawPath(
                path = webPath,
                color = Color(0xFF334155),
                style = Stroke(width = if (scale == 1.0f) 1.5.dp.toPx() else 1.dp.toPx())
            )
        }

        // Draw radial axis lines from center to vertices
        for (i in 0 until numAxes) {
            val angle = i * angleStep - (Math.PI / 2).toFloat()
            val endX = center.x + radius * cos(angle)
            val endY = center.y + radius * sin(angle)

            drawLine(
                color = Color(0xFF334155),
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )

            // Draw axis text label using Android Canvas
            val labelRadius = radius * 1.20f
            val labelX = center.x + labelRadius * cos(angle)
            val labelY = center.y + labelRadius * sin(angle)

            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#94A3B8")
                textSize = 28f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                isFakeBoldText = true
            }

            val axisLabel = "${axes[i].name}\n(${(animatedValues[i] * 100).toInt()}%)"
            val lines = axisLabel.split("\n")
            drawContext.canvas.nativeCanvas.drawText(
                lines[0],
                labelX,
                labelY - 6f,
                paint
            )
            paint.textSize = 22f
            paint.color = android.graphics.Color.parseColor("#38BDF8")
            drawContext.canvas.nativeCanvas.drawText(
                lines[1],
                labelX,
                labelY + 20f,
                paint
            )
        }

        // Draw the dynamic filled threat polygon
        val threatPath = Path()
        val points = mutableListOf<Offset>()

        for (i in 0 until numAxes) {
            val angle = i * angleStep - (Math.PI / 2).toFloat()
            val scaledValue = animatedValues[i].coerceIn(0.05f, 1.0f)
            val x = center.x + radius * scaledValue * cos(angle)
            val y = center.y + radius * scaledValue * sin(angle)
            val point = Offset(x, y)
            points.add(point)

            if (i == 0) threatPath.moveTo(x, y) else threatPath.lineTo(x, y)
        }
        threatPath.close()

        // Fill path with semi-transparent accent color
        drawPath(
            path = threatPath,
            color = accentColor.copy(alpha = 0.30f)
        )

        // Draw path boundary stroke
        drawPath(
            path = threatPath,
            color = accentColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw vertex dots with glowing inner core
        points.forEach { pt ->
            drawCircle(
                color = accentColor.copy(alpha = 0.4f),
                radius = 7.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = accentColor,
                radius = 4.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
fun AxisMetricItem(axis: ThreatAxis) {
    val barColor = when {
        axis.value > 0.75f -> Color(0xFFEF4444)
        axis.value > 0.50f -> Color(0xFFF59E0B)
        else -> Color(0xFF38BDF8)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = axis.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8)
                    )
                )
                Text(
                    text = axis.displayValue,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = barColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { axis.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = barColor,
                trackColor = Color(0xFF1E293B)
            )
        }
    }
}
