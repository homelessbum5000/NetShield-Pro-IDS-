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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class LiveTrafficDataPoint(
    val secondLabel: String,
    val throughputMbps: Float, // e.g. 10..500 Mbps
    val quantumVulnerabilityScore: Float // 0..100%
)

enum class OverviewTrafficScenario {
    BASELINE_SECURE,
    HARVEST_NOW_DECRYPT_LATER,
    CLASSICAL_RSA_SPIKE,
    FULL_QUANTUM_SHIELD
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecurityOverviewDashboardCard() {
    var scenario by remember { mutableStateOf(OverviewTrafficScenario.BASELINE_SECURE) }
    var isLiveStreaming by remember { mutableStateOf(true) }

    // Sliding window of real-time telemetry data points (last 15 seconds)
    var trafficStream by remember {
        mutableStateOf(generateInitialStream(scenario))
    }

    // Emergency action trigger state
    var isEmergencyRotationActive by remember { mutableStateOf(false) }
    var rotationStatusText by remember { mutableStateOf<String?>(null) }

    // Live Stream Data Generator Effect
    LaunchedEffect(isLiveStreaming, scenario, isEmergencyRotationActive) {
        var secondCounter = 15
        while (isLiveStreaming) {
            delay(1000L)
            secondCounter++
            val timeLabel = "${secondCounter}s"

            val baseMbps = when (scenario) {
                OverviewTrafficScenario.BASELINE_SECURE -> 120f + (Math.random().toFloat() * 40f)
                OverviewTrafficScenario.HARVEST_NOW_DECRYPT_LATER -> 380f + (Math.random().toFloat() * 90f)
                OverviewTrafficScenario.CLASSICAL_RSA_SPIKE -> 250f + (Math.random().toFloat() * 60f)
                OverviewTrafficScenario.FULL_QUANTUM_SHIELD -> 140f + (Math.random().toFloat() * 30f)
            }

            val vulnScore = if (isEmergencyRotationActive) {
                (12f + Math.random().toFloat() * 5f).coerceIn(0f, 100f)
            } else {
                when (scenario) {
                    OverviewTrafficScenario.BASELINE_SECURE -> 28f + (Math.random().toFloat() * 10f)
                    OverviewTrafficScenario.HARVEST_NOW_DECRYPT_LATER -> 88f + (Math.random().toFloat() * 8f)
                    OverviewTrafficScenario.CLASSICAL_RSA_SPIKE -> 72f + (Math.random().toFloat() * 12f)
                    OverviewTrafficScenario.FULL_QUANTUM_SHIELD -> 14f + (Math.random().toFloat() * 4f)
                }
            }

            val newPoint = LiveTrafficDataPoint(timeLabel, baseMbps, vulnScore)
            val updatedStream = trafficStream.drop(1) + newPoint
            trafficStream = updatedStream
        }
    }

    val latestPoint = trafficStream.lastOrNull() ?: LiveTrafficDataPoint("0s", 120f, 25f)
    val avgVuln = trafficStream.map { it.quantumVulnerabilityScore }.average().toFloat()
    val peakMbps = trafficStream.maxOf { it.throughputMbps }

    val (riskBadgeText, riskColor) = when {
        avgVuln > 75f -> "CRITICAL QUANTUM RISK" to Color(0xFFEF4444)
        avgVuln > 50f -> "ELEVATED VULNERABILITY" to Color(0xFFF59E0B)
        avgVuln > 25f -> "MODERATE SHIELDING" to Color(0xFF38BDF8)
        else -> "QUANTUM SAFE (ML-KEM)" to Color(0xFF10B981)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("security_overview_dashboard_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = "Overview",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Quantum Security Overview",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Real-Time Vulnerability Index & Traffic Overlay",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                // Risk Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(riskColor.copy(alpha = 0.2f))
                        .border(1.dp, riskColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = riskBadgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = riskColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            // Stream Controls & Preset Scenarios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attack Vector Simulation Scenarios:",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                )

                // Pause / Resume Stream Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F172A))
                        .clickable { isLiveStreaming = !isLiveStreaming }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiveStreaming) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLiveStreaming) "Pause" else "Stream",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8))
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = scenario == OverviewTrafficScenario.BASELINE_SECURE,
                    onClick = {
                        scenario = OverviewTrafficScenario.BASELINE_SECURE
                        isEmergencyRotationActive = false
                        trafficStream = generateInitialStream(scenario)
                    },
                    label = { Text("Standard Baseline", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
                FilterChip(
                    selected = scenario == OverviewTrafficScenario.HARVEST_NOW_DECRYPT_LATER,
                    onClick = {
                        scenario = OverviewTrafficScenario.HARVEST_NOW_DECRYPT_LATER
                        isEmergencyRotationActive = false
                        trafficStream = generateInitialStream(scenario)
                    },
                    label = { Text("Harvest-Now Threat", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFDC2626),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
                FilterChip(
                    selected = scenario == OverviewTrafficScenario.CLASSICAL_RSA_SPIKE,
                    onClick = {
                        scenario = OverviewTrafficScenario.CLASSICAL_RSA_SPIKE
                        isEmergencyRotationActive = false
                        trafficStream = generateInitialStream(scenario)
                    },
                    label = { Text("Legacy RSA Exposure", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD97706),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
                FilterChip(
                    selected = scenario == OverviewTrafficScenario.FULL_QUANTUM_SHIELD,
                    onClick = {
                        scenario = OverviewTrafficScenario.FULL_QUANTUM_SHIELD
                        isEmergencyRotationActive = false
                        trafficStream = generateInitialStream(scenario)
                    },
                    label = { Text("Full ML-KEM Shield", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF7C3AED),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time Dual-Axis Overlay Canvas Chart
            // Primary Line (Cyan): Network Traffic (Mbps)
            // Secondary Line (Purple/Red): Quantum Vulnerability Score (%)
            SecurityOverviewDualChart(
                stream = trafficStream,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.75f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Live Stat Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OverviewStatCard(
                        label = "Live Throughput",
                        value = "${latestPoint.throughputMbps.toInt()} Mbps",
                        subtext = "Peak: ${peakMbps.toInt()} Mbps",
                        accentColor = Color(0xFF38BDF8)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    OverviewStatCard(
                        label = "Quantum Risk Index",
                        value = "${latestPoint.quantumVulnerabilityScore.toInt()}%",
                        subtext = "Avg: ${avgVuln.toInt()}%",
                        accentColor = riskColor
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    OverviewStatCard(
                        label = "Shielding Standard",
                        value = if (isEmergencyRotationActive || scenario == OverviewTrafficScenario.FULL_QUANTUM_SHIELD) "ML-KEM 1024" else "RSA-2048 / EC",
                        subtext = "FIPS 203 Valid",
                        accentColor = Color(0xFFA855F7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Emergency Action Button: Trigger Quantum Key Rotation
            OutlinedButton(
                onClick = {
                    isEmergencyRotationActive = true
                    rotationStatusText = "Executing emergency key rotation -> Provisioning ML-KEM-1024 quantum keys across all gateway tunnels..."
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFF7E22CE).copy(alpha = 0.2f),
                    contentColor = Color(0xFFE9D5FF)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EnhancedEncryption,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEmergencyRotationActive) "RE-KEYED TO ML-KEM-1024 (QUANTUM SAFE)" else "TRIGGER EMERGENCY QUANTUM KEY ROTATION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            if (rotationStatusText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = rotationStatusText!!,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF34D399),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun SecurityOverviewDualChart(
    stream: List<LiveTrafficDataPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val paddingLeft = 40f
        val paddingRight = 40f
        val paddingTop = 20f
        val paddingBottom = 40f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        val maxMbps = 500f
        val maxVuln = 100f

        // Draw horizontal grid lines
        val gridLines = 4
        val stepY = chartHeight / gridLines
        for (i in 0..gridLines) {
            val y = paddingTop + i * stepY
            drawLine(
                color = Color(0xFF334155),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )

            // Left Y-Axis Label (Mbps)
            val mbpsVal = ((maxMbps - (i * maxMbps / gridLines))).toInt()
            val paintLeft = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#38BDF8")
                textSize = 20f
                textAlign = android.graphics.Paint.Align.RIGHT
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                "${mbpsVal}M",
                paddingLeft - 6f,
                y + 6f,
                paintLeft
            )

            // Right Y-Axis Label (Vuln Risk %)
            val vulnVal = ((maxVuln - (i * maxVuln / gridLines))).toInt()
            val paintRight = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#A855F7")
                textSize = 20f
                textAlign = android.graphics.Paint.Align.LEFT
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                "${vulnVal}%",
                width - paddingRight + 6f,
                y + 6f,
                paintRight
            )
        }

        val stepX = chartWidth / (stream.size - 1).coerceAtLeast(1)

        val mbpsPoints = mutableListOf<Offset>()
        val vulnPoints = mutableListOf<Offset>()

        stream.forEachIndexed { i, pt ->
            val x = paddingLeft + i * stepX

            val yMbps = paddingTop + chartHeight * (1f - (pt.throughputMbps / maxMbps).coerceIn(0f, 1f))
            mbpsPoints.add(Offset(x, yMbps))

            val yVuln = paddingTop + chartHeight * (1f - (pt.quantumVulnerabilityScore / maxVuln).coerceIn(0f, 1f))
            vulnPoints.add(Offset(x, yVuln))

            // X-Axis Time Label (every 3 points)
            if (i % 3 == 0 || i == stream.lastIndex) {
                val paintX = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#94A3B8")
                    textSize = 20f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    pt.secondLabel,
                    x,
                    height - 8f,
                    paintX
                )
            }
        }

        // Draw Throughput Area Fill (Cyan)
        val mbpsArea = Path().apply {
            if (mbpsPoints.isNotEmpty()) {
                moveTo(mbpsPoints[0].x, height - paddingBottom)
                mbpsPoints.forEach { pt -> lineTo(pt.x, pt.y) }
                lineTo(mbpsPoints.last().x, height - paddingBottom)
                close()
            }
        }
        drawPath(
            path = mbpsArea,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.35f), Color(0xFF38BDF8).copy(alpha = 0.02f)),
                startY = paddingTop,
                endY = height - paddingBottom
            )
        )

        // Draw Throughput Line Path
        val mbpsLine = Path().apply {
            if (mbpsPoints.isNotEmpty()) {
                moveTo(mbpsPoints[0].x, mbpsPoints[0].y)
                for (i in 1 until mbpsPoints.size) {
                    lineTo(mbpsPoints[i].x, mbpsPoints[i].y)
                }
            }
        }
        drawPath(
            path = mbpsLine,
            color = Color(0xFF38BDF8),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Quantum Vulnerability Line Path (Purple / Red)
        val vulnLine = Path().apply {
            if (vulnPoints.isNotEmpty()) {
                moveTo(vulnPoints[0].x, vulnPoints[0].y)
                for (i in 1 until vulnPoints.size) {
                    lineTo(vulnPoints[i].x, vulnPoints[i].y)
                }
            }
        }
        drawPath(
            path = vulnLine,
            color = Color(0xFFA855F7),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Live End Dots
        mbpsPoints.lastOrNull()?.let { pt ->
            drawCircle(color = Color(0xFF38BDF8), radius = 4.dp.toPx(), center = pt)
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pt)
        }

        vulnPoints.lastOrNull()?.let { pt ->
            drawCircle(color = Color(0xFFA855F7), radius = 5.dp.toPx(), center = pt)
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = pt)
        }
    }
}

@Composable
fun OverviewStatCard(
    label: String,
    value: String,
    subtext: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 9.sp)
            )
        }
    }
}

private fun generateInitialStream(scenario: OverviewTrafficScenario): List<LiveTrafficDataPoint> {
    return (1..15).map { sec ->
        val label = "${sec}s"
        val mbps = when (scenario) {
            OverviewTrafficScenario.BASELINE_SECURE -> 120f + (Math.random().toFloat() * 30f)
            OverviewTrafficScenario.HARVEST_NOW_DECRYPT_LATER -> 350f + (Math.random().toFloat() * 80f)
            OverviewTrafficScenario.CLASSICAL_RSA_SPIKE -> 240f + (Math.random().toFloat() * 50f)
            OverviewTrafficScenario.FULL_QUANTUM_SHIELD -> 130f + (Math.random().toFloat() * 20f)
        }

        val vuln = when (scenario) {
            OverviewTrafficScenario.BASELINE_SECURE -> 25f + (Math.random().toFloat() * 8f)
            OverviewTrafficScenario.HARVEST_NOW_DECRYPT_LATER -> 85f + (Math.random().toFloat() * 10f)
            OverviewTrafficScenario.CLASSICAL_RSA_SPIKE -> 70f + (Math.random().toFloat() * 10f)
            OverviewTrafficScenario.FULL_QUANTUM_SHIELD -> 12f + (Math.random().toFloat() * 5f)
        }

        LiveTrafficDataPoint(label, mbps, vuln)
    }
}
