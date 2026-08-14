package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin

enum class ThreatIntensityTimeframe(val label: String, val pointCount: Int, val intervalLabel: String) {
    LIVE_15M("15m Live", 15, "1 min/point"),
    ROLLING_1H("1 Hour", 20, "3 min/point"),
    WINDOW_6H("6 Hours", 24, "15 min/point"),
    TIMELINE_24H("24 Hours", 24, "1 hour/point")
}

enum class ThreatCategoryFilter(val label: String, val color: Color) {
    ALL("All Threats", Color(0xFF818CF8)),
    QUANTUM_DECRYPT("Quantum Decrypt", Color(0xFFC084FC)),
    DDOS_FLOOD("DDoS Flood", Color(0xFFEF4444)),
    EXFILTRATION("Data Exfiltration", Color(0xFFF97316)),
    PORT_SCAN("Recon & Scans", Color(0xFF38BDF8))
}

data class ThreatIntensityPoint(
    val timeLabel: String,
    val intensityPct: Float,       // 0f to 100f
    val anomalyCount: Int,
    val primaryVector: String,
    val severityLevel: String,     // "NORMAL", "ELEVATED", "HIGH", "CRITICAL"
    val packetThroughputKbps: Float,
    val quantumEntropyShift: Float
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThreatIntensityCanvasCard(
    capturedThreatCount: Int = 18,
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf(ThreatIntensityTimeframe.LIVE_15M) }
    var selectedCategoryFilter by remember { mutableStateOf(ThreatCategoryFilter.ALL) }
    var isLiveStreaming by remember { mutableStateOf(true) }
    var scrubIndex by remember { mutableIntStateOf(10) }
    var burstTriggeredCount by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Generate dynamic threat points based on timeframe and filter
    var dataPoints by remember(selectedTimeframe, selectedCategoryFilter, burstTriggeredCount) {
        mutableStateOf(
            generateThreatIntensityPoints(
                timeframe = selectedTimeframe,
                filter = selectedCategoryFilter,
                burstSeed = burstTriggeredCount
            )
        )
    }

    // Live continuous streaming animation
    LaunchedEffect(isLiveStreaming, selectedTimeframe) {
        while (isLiveStreaming) {
            delay(1800L)
            if (selectedTimeframe == ThreatIntensityTimeframe.LIVE_15M) {
                dataPoints = shiftLivePoints(dataPoints)
            }
        }
    }

    // Glowing pulse animation for critical peak points
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val currentInspectedPoint = dataPoints.getOrNull(scrubIndex.coerceIn(0, dataPoints.lastIndex))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("threat_intensity_canvas_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090E1A)),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Threat Intensity",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Threat Intensity Monitor",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Text(
                            text = "Real-time custom Compose Canvas visualization of intrusion telemetry & attack vectors",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                    }
                }

                Surface(
                    color = if (isLiveStreaming) Color(0xFF064E3B) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isLiveStreaming) Color(0xFF10B981) else Color(0xFF475569))
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { isLiveStreaming = !isLiveStreaming }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isLiveStreaming) Color(0xFF34D399) else Color(0xFF94A3B8))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isLiveStreaming) "LIVE CANVAS" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isLiveStreaming) Color(0xFFA7F3D0) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF1E293B)
            )

            // Current Intensity Stats Summary Bar
            val currentAvgIntensity = if (dataPoints.isNotEmpty()) dataPoints.map { it.intensityPct }.average().toFloat() else 0f
            val maxPeakPoint = dataPoints.maxByOrNull { it.intensityPct }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF131C31),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Current Intensity", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        val latestPoint = dataPoints.lastOrNull()?.intensityPct ?: 0f
                        Text(
                            text = "${"%.1f".format(latestPoint)}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = getIntensityColor(latestPoint),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp
                            )
                        )
                    }
                }

                Surface(
                    color = Color(0xFF131C31),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Window Peak", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${"%.1f".format(maxPeakPoint?.intensityPct ?: 0f)}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp
                            )
                        )
                    }
                }

                Surface(
                    color = Color(0xFF131C31),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Active Vector", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = maxPeakPoint?.primaryVector ?: "Quantum Brute",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timeframe Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ThreatIntensityTimeframe.values().forEach { tf ->
                    val isSelected = selectedTimeframe == tf
                    Surface(
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedTimeframe = tf
                                scrubIndex = (tf.pointCount / 2)
                            }
                    ) {
                        Text(
                            text = tf.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // CUSTOM COMPOSE CANVAS THREAT INTENSITY AREA
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070B14))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .pointerInput(dataPoints.size) {
                        detectTapGestures { offset ->
                            val pointSpacing = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                            val tappedIdx = (offset.x / pointSpacing).toInt().coerceIn(0, dataPoints.lastIndex)
                            scrubIndex = tappedIdx
                        }
                    }
                    .pointerInput(dataPoints.size) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val pointSpacing = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                            val draggedIdx = (change.position.x / pointSpacing).toInt().coerceIn(0, dataPoints.lastIndex)
                            scrubIndex = draggedIdx
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(210.dp)) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 26.dp.toPx()
                    val paddingTop = 16.dp.toPx()
                    val usableHeight = height - paddingTop - paddingBottom

                    if (dataPoints.isEmpty()) return@Canvas

                    // 1. Draw horizontal threshold grid lines
                    val thresholdLevels = listOf(25f to "Baseline", 50f to "Elevated", 75f to "High Alert", 90f to "Breach")
                    thresholdLevels.forEach { (pct, label) ->
                        val y = paddingTop + usableHeight * (1f - (pct / 100f))
                        val isCritical = pct >= 75f
                        val lineColor = if (isCritical) Color(0xFFEF4444).copy(alpha = 0.35f) else Color(0xFF334155).copy(alpha = 0.5f)

                        drawLine(
                            color = lineColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }

                    // 2. Build Cubic Bézier Spline for Smooth Threat Intensity Curve
                    val points = dataPoints.mapIndexed { index, pt ->
                        val x = if (dataPoints.size > 1) index * (width / (dataPoints.size - 1)) else width / 2f
                        val normalizedY = pt.intensityPct.coerceIn(0f, 100f) / 100f
                        val y = paddingTop + usableHeight * (1f - normalizedY)
                        Offset(x, y)
                    }

                    val strokePath = Path()
                    val fillPath = Path()

                    if (points.isNotEmpty()) {
                        strokePath.moveTo(points.first().x, points.first().y)
                        fillPath.moveTo(points.first().x, height - paddingBottom)
                        fillPath.lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                            val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                            strokePath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p1.x, p1.y
                            )
                            fillPath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p1.x, p1.y
                            )
                        }

                        fillPath.lineTo(points.last().x, height - paddingBottom)
                        fillPath.close()

                        // Draw Gradient Area Fill under Curve
                        val fillGradient = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFEF4444).copy(alpha = 0.45f),
                                Color(0xFFF97316).copy(alpha = 0.35f),
                                Color(0xFF38BDF8).copy(alpha = 0.20f),
                                Color(0xFF0284C7).copy(alpha = 0.02f)
                            ),
                            startY = paddingTop,
                            endY = height - paddingBottom
                        )
                        drawPath(path = fillPath, brush = fillGradient)

                        // Draw Main Spline Stroke
                        val strokeGradient = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF38BDF8),
                                Color(0xFF818CF8),
                                Color(0xFFF97316),
                                Color(0xFFEF4444),
                                Color(0xFF38BDF8)
                            )
                        )
                        drawPath(
                            path = strokePath,
                            brush = strokeGradient,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw Interactive Scrub Indicator & Glow
                        val currentScrubPoint = points.getOrNull(scrubIndex.coerceIn(0, points.lastIndex))
                        if (currentScrubPoint != null) {
                            // Vertical tracking dashed line
                            drawLine(
                                color = Color(0xFF38BDF8).copy(alpha = 0.7f),
                                start = Offset(currentScrubPoint.x, paddingTop),
                                end = Offset(currentScrubPoint.x, height - paddingBottom),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )

                            // Outer pulse glow circle
                            drawCircle(
                                color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                                radius = pulseGlow.dp.toPx(),
                                center = currentScrubPoint
                            )

                            // Inner focal point
                            drawCircle(
                                color = Color(0xFF38BDF8),
                                radius = 5.dp.toPx(),
                                center = currentScrubPoint
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = currentScrubPoint
                            )
                        }

                        // Highlight Peak Points with small alarm dots
                        points.forEachIndexed { idx, pt ->
                            val intensity = dataPoints[idx].intensityPct
                            if (intensity >= 80f) {
                                drawCircle(
                                    color = Color(0xFFEF4444).copy(alpha = 0.4f),
                                    radius = (pulseGlow * 0.8f).dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = Color(0xFFEF4444),
                                    radius = 4.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Point Inspector Card (Updates on scrub/tap)
            if (currentInspectedPoint != null) {
                Surface(
                    color = Color(0xFF131C31),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, getIntensityColor(currentInspectedPoint.intensityPct).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(getIntensityColor(currentInspectedPoint.intensityPct))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Timestamp: ${currentInspectedPoint.timeLabel}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFE2E8F0),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Surface(
                                color = getIntensityColor(currentInspectedPoint.intensityPct).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = currentInspectedPoint.severityLevel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = getIntensityColor(currentInspectedPoint.intensityPct),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Threat Intensity", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp))
                                Text(
                                    text = "${"%.1f".format(currentInspectedPoint.intensityPct)}%",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = getIntensityColor(currentInspectedPoint.intensityPct),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp
                                    )
                                )
                            }

                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Primary Threat Vector", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp))
                                Text(
                                    text = currentInspectedPoint.primaryVector,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Throughput", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp))
                                Text(
                                    text = "${"%.0f".format(currentInspectedPoint.packetThroughputKbps)} kbps",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF38BDF8),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Attack Vector Category Filter Chips
            Text(
                text = "Filter Intrusion Vector Stream:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ThreatCategoryFilter.values().forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat.label, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cat.color.copy(alpha = 0.3f),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) cat.color else Color(0xFF334155),
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Attack Burst Simulation Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        burstTriggeredCount++
                        Toast.makeText(context, "Simulated Quantum SYN-Flood Attack Spike injected into Canvas", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Inject Threat Spike", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val report = generateThreatIntensityReport(dataPoints)
                        clipboardManager.setText(AnnotatedString(report))
                        Toast.makeText(context, "Threat intensity telemetry copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Telemetry", fontSize = 11.sp)
                }
            }
        }
    }
}

private fun getIntensityColor(intensityPct: Float): Color {
    return when {
        intensityPct >= 85f -> Color(0xFFEF4444)
        intensityPct >= 65f -> Color(0xFFF97316)
        intensityPct >= 40f -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }
}

private fun generateThreatIntensityPoints(
    timeframe: ThreatIntensityTimeframe,
    filter: ThreatCategoryFilter,
    burstSeed: Int
): List<ThreatIntensityPoint> {
    val count = timeframe.pointCount
    val vectors = when (filter) {
        ThreatCategoryFilter.ALL -> listOf("Quantum Lattice SVP", "SYN Flood DDoS", "eBPF Packet Drop", "TLS 1.3 Downgrade", "DNS Tunneling", "MitM ARP Poison")
        ThreatCategoryFilter.QUANTUM_DECRYPT -> listOf("Kyber-1024 Quantum Probe", "Shor's Factorization Attempt", "Grover Search Anomaly")
        ThreatCategoryFilter.DDOS_FLOOD -> listOf("UDP Amplification", "TCP SYN Flood", "ICMP Ping Flood")
        ThreatCategoryFilter.EXFILTRATION -> listOf("Encrypted SSH Exfiltration", "Covert DNS Channel", "HTTP POST Burst")
        ThreatCategoryFilter.PORT_SCAN -> listOf("Stealth SYN Scan", "UDP Port Sweep", "Xmas Packet Scan")
    }

    return (0 until count).map { i ->
        val timeLabel = when (timeframe) {
            ThreatIntensityTimeframe.LIVE_15M -> "-${count - i}m"
            ThreatIntensityTimeframe.ROLLING_1H -> "-${(count - i) * 3}m"
            ThreatIntensityTimeframe.WINDOW_6H -> "-${(count - i) * 15}m"
            ThreatIntensityTimeframe.TIMELINE_24H -> "${(i + 1) % 24}:00"
        }

        val baseWave = (sin(i * 0.45) * 22f + 42f).toFloat()
        val burstSpike = if ((i == count - 3 || i == count - 8) && burstSeed > 0) 35f else 0f
        val randomVariation = ((i * 7 + burstSeed * 13) % 15).toFloat()
        val intensity = (baseWave + burstSpike + randomVariation).coerceIn(8f, 96f)

        val severity = when {
            intensity >= 80f -> "CRITICAL BREACH"
            intensity >= 60f -> "HIGH ALERT"
            intensity >= 35f -> "ELEVATED"
            else -> "NORMAL"
        }

        ThreatIntensityPoint(
            timeLabel = timeLabel,
            intensityPct = intensity,
            anomalyCount = (intensity * 0.4f).toInt() + 1,
            primaryVector = vectors[i % vectors.size],
            severityLevel = severity,
            packetThroughputKbps = (350f + intensity * 18.5f),
            quantumEntropyShift = (0.75f + (intensity / 100f) * 0.45f)
        )
    }
}

private fun shiftLivePoints(current: List<ThreatIntensityPoint>): List<ThreatIntensityPoint> {
    if (current.isEmpty()) return current
    val last = current.last()
    val newIntensity = (last.intensityPct + ((System.currentTimeMillis() % 11) - 5f)).coerceIn(12f, 92f)
    val newPoint = ThreatIntensityPoint(
        timeLabel = "Now",
        intensityPct = newIntensity,
        anomalyCount = (newIntensity * 0.35f).toInt() + 1,
        primaryVector = last.primaryVector,
        severityLevel = if (newIntensity >= 75f) "HIGH ALERT" else if (newIntensity >= 40f) "ELEVATED" else "NORMAL",
        packetThroughputKbps = (380f + newIntensity * 16f),
        quantumEntropyShift = 0.88f
    )
    return current.drop(1) + newPoint
}

private fun generateThreatIntensityReport(points: List<ThreatIntensityPoint>): String {
    val sb = StringBuilder()
    sb.append("=== NETSHIELD IDS THREAT INTENSITY TELEMETRY ===\n")
    sb.append("Timestamp: ${java.util.Date()}\n")
    sb.append("Total Samples: ${points.size}\n")
    sb.append("Average Intensity: ${"%.1f".format(points.map { it.intensityPct }.average())}%\n")
    sb.append("Peak Intensity: ${"%.1f".format(points.maxOfOrNull { it.intensityPct } ?: 0f)}%\n\n")
    sb.append("Time,Intensity,Severity,PrimaryVector,ThroughputKbps\n")
    points.forEach { p ->
        sb.append("${p.timeLabel},${"%.1f".format(p.intensityPct)}%,${p.severityLevel},${p.primaryVector},${"%.0f".format(p.packetThroughputKbps)}\n")
    }
    return sb.toString()
}
