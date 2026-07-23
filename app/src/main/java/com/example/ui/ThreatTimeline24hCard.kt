package com.example.ui

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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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

data class HourlyThreatData(
    val hourLabel: String,       // e.g. "00:00", "01:00", ... "23:00"
    val hourIndex: Int,          // 0 to 23
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int
) {
    val total: Int get() = criticalCount + highCount + mediumCount + lowCount
}

enum class TimelineSeverityFilter {
    ALL,
    CRITICAL_ONLY,
    HIGH_ONLY,
    ANOMALIES_ONLY
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThreatTimeline24hCard() {
    var filterMode by remember { mutableStateOf(TimelineSeverityFilter.ALL) }
    var selectedHourIndex by remember { mutableStateOf(14) } // Default inspect hour 14:00 (peak hour)
    var isLiveTelemetryActive by remember { mutableStateOf(true) }

    // Generate initial 24 hour threat profile
    var hourlyData by remember {
        mutableStateOf(generateSample24hThreats())
    }

    // Live update simulation effect
    LaunchedEffect(isLiveTelemetryActive) {
        while (isLiveTelemetryActive) {
            delay(2000L)
            // Organic telemetry fluctuation on current hour
            val currentList = hourlyData.toMutableList()
            val lastIdx = currentList.lastIndex
            val item = currentList[lastIdx]
            val newCritical = (item.criticalCount + (if (Math.random() > 0.6) 1 else 0)).coerceIn(0, 30)
            val newHigh = (item.highCount + (if (Math.random() > 0.5) 1 else -1)).coerceIn(0, 45)
            currentList[lastIdx] = item.copy(criticalCount = newCritical, highCount = newHigh)
            hourlyData = currentList
        }
    }

    val activeItem = hourlyData.getOrElse(selectedHourIndex) { hourlyData[0] }
    val total24hAlerts = hourlyData.sumOf { it.total }
    val totalCritical24h = hourlyData.sumOf { it.criticalCount }
    val peakHour = hourlyData.maxByOrNull { it.total }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("threat_timeline_24h_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Threat Timeline",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "24-Hour Threat History",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Timeline Analytics & Hourly Incident Volume",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                // Total Alerts Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$total24hAlerts Events",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            // Severity Filter Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = filterMode == TimelineSeverityFilter.ALL,
                    onClick = { filterMode = TimelineSeverityFilter.ALL },
                    label = { Text("All Severities", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
                FilterChip(
                    selected = filterMode == TimelineSeverityFilter.CRITICAL_ONLY,
                    onClick = { filterMode = TimelineSeverityFilter.CRITICAL_ONLY },
                    label = { Text("Critical Only", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFEF4444),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
                FilterChip(
                    selected = filterMode == TimelineSeverityFilter.HIGH_ONLY,
                    onClick = { filterMode = TimelineSeverityFilter.HIGH_ONLY },
                    label = { Text("High Risk", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF59E0B),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFF94A3B8)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Canvas Chart for 24h Area & Line
            ThreatTimelineCanvasChart(
                hourlyData = hourlyData,
                filterMode = filterMode,
                selectedIndex = selectedHourIndex,
                onSelectHour = { selectedHourIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Time Selector Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inspect Time Window: ${activeItem.hourLabel}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                )
                Text(
                    text = "${activeItem.total} Alerts",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Slider(
                value = selectedHourIndex.toFloat(),
                onValueChange = { selectedHourIndex = it.toInt() },
                valueRange = 0f..23f,
                steps = 22,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF38BDF8),
                    activeTrackColor = Color(0xFF0284C7),
                    inactiveTrackColor = Color(0xFF334155)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Hourly Inspection Detail Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hour Window ${activeItem.hourLabel}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = if (activeItem.criticalCount > 10) "HIGH INCIDENT PERIOD" else "NORMAL TRAFFIC",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (activeItem.criticalCount > 10) Color(0xFFEF4444) else Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TimelineStatBadge("Critical", "${activeItem.criticalCount}", Color(0xFFEF4444))
                        TimelineStatBadge("High Risk", "${activeItem.highCount}", Color(0xFFF59E0B))
                        TimelineStatBadge("Medium", "${activeItem.mediumCount}", Color(0xFF38BDF8))
                        TimelineStatBadge("Low / Info", "${activeItem.lowCount}", Color(0xFF10B981))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Footer Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Peak Hour: ${peakHour?.hourLabel ?: "--"} (${peakHour?.total ?: 0} events)",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                )
                Text(
                    text = "Critical 24h: $totalCritical24h",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun ThreatTimelineCanvasChart(
    hourlyData: List<HourlyThreatData>,
    filterMode: TimelineSeverityFilter,
    selectedIndex: Int,
    onSelectHour: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.clickable {
            // Touch handler could be extended here if needed
        }
    ) {
        val width = size.width
        val height = size.height
        val paddingLeft = 40f
        val paddingRight = 20f
        val paddingTop = 20f
        val paddingBottom = 40f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Compute maximum value for scaling
        val maxVal = hourlyData.maxOf {
            when (filterMode) {
                TimelineSeverityFilter.ALL -> it.total
                TimelineSeverityFilter.CRITICAL_ONLY -> it.criticalCount
                TimelineSeverityFilter.HIGH_ONLY -> it.highCount
                TimelineSeverityFilter.ANOMALIES_ONLY -> it.criticalCount + it.highCount
            }
        }.coerceAtLeast(10).toFloat()

        // Draw Horizontal Grid Lines (0%, 25%, 50%, 75%, 100%)
        val gridStep = chartHeight / 4f
        for (i in 0..4) {
            val y = paddingTop + i * gridStep
            drawLine(
                color = Color(0xFF334155),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )

            val gridLabel = ((maxVal - (i * maxVal / 4f))).toInt().toString()
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#64748B")
                textSize = 22f
                textAlign = android.graphics.Paint.Align.RIGHT
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                gridLabel,
                paddingLeft - 8f,
                y + 8f,
                paint
            )
        }

        val stepX = chartWidth / (hourlyData.size - 1).coerceAtLeast(1)

        // Generate Path Points
        val totalPoints = mutableListOf<Offset>()
        val criticalPoints = mutableListOf<Offset>()

        hourlyData.forEachIndexed { i, item ->
            val x = paddingLeft + i * stepX

            val valForMode = when (filterMode) {
                TimelineSeverityFilter.ALL -> item.total
                TimelineSeverityFilter.CRITICAL_ONLY -> item.criticalCount
                TimelineSeverityFilter.HIGH_ONLY -> item.highCount
                TimelineSeverityFilter.ANOMALIES_ONLY -> item.criticalCount + item.highCount
            }

            val y = paddingTop + chartHeight * (1f - (valForMode / maxVal))
            totalPoints.add(Offset(x, y))

            val critY = paddingTop + chartHeight * (1f - (item.criticalCount / maxVal))
            criticalPoints.add(Offset(x, critY))

            // X-Axis Hour Labels (every 4 hours)
            if (i % 4 == 0 || i == hourlyData.lastIndex) {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#94A3B8")
                    textSize = 22f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    item.hourLabel,
                    x,
                    height - 8f,
                    paint
                )
            }
        }

        // Draw Area Fill Path under main line
        val areaPath = Path().apply {
            if (totalPoints.isNotEmpty()) {
                moveTo(totalPoints[0].x, height - paddingBottom)
                totalPoints.forEach { pt -> lineTo(pt.x, pt.y) }
                lineTo(totalPoints.last().x, height - paddingBottom)
                close()
            }
        }

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0284C7).copy(alpha = 0.45f), Color(0xFF0284C7).copy(alpha = 0.05f)),
                startY = paddingTop,
                endY = height - paddingBottom
            )
        )

        // Draw Main Line Path
        val linePath = Path().apply {
            if (totalPoints.isNotEmpty()) {
                moveTo(totalPoints[0].x, totalPoints[0].y)
                for (i in 1 until totalPoints.size) {
                    lineTo(totalPoints[i].x, totalPoints[i].y)
                }
            }
        }

        drawPath(
            path = linePath,
            color = Color(0xFF38BDF8),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Critical Layer Path (Red) if in ALL mode
        if (filterMode == TimelineSeverityFilter.ALL) {
            val critPath = Path().apply {
                if (criticalPoints.isNotEmpty()) {
                    moveTo(criticalPoints[0].x, criticalPoints[0].y)
                    for (i in 1 until criticalPoints.size) {
                        lineTo(criticalPoints[i].x, criticalPoints[i].y)
                    }
                }
            }

            drawPath(
                path = critPath,
                color = Color(0xFFEF4444),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Draw Highlight Marker for Selected Hour
        if (selectedIndex in totalPoints.indices) {
            val selectedPt = totalPoints[selectedIndex]

            // Vertical indicator line
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(selectedPt.x, paddingTop),
                end = Offset(selectedPt.x, height - paddingBottom),
                strokeWidth = 1.5.dp.toPx()
            )

            // Pulse Outer Circle
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.35f),
                radius = 8.dp.toPx(),
                center = selectedPt
            )

            // Inner Core Dot
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = selectedPt
            )
        }
    }
}

@Composable
fun TimelineStatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                color = color,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
        )
    }
}

private fun generateSample24hThreats(): List<HourlyThreatData> {
    return (0..23).map { hour ->
        val hourFormatted = String.format("%02d:00", hour)

        // Simulate realistic threat volume curves (peak around 14:00 - 16:00)
        val isPeakWindow = hour in 13..16
        val isNightWindow = hour in 1..5

        val crit = when {
            isPeakWindow -> (12..25).random()
            isNightWindow -> (1..4).random()
            else -> (3..10).random()
        }

        val high = when {
            isPeakWindow -> (15..32).random()
            isNightWindow -> (2..8).random()
            else -> (8..18).random()
        }

        val med = (10..30).random()
        val low = (20..50).random()

        HourlyThreatData(
            hourLabel = hourFormatted,
            hourIndex = hour,
            criticalCount = crit,
            highCount = high,
            mediumCount = med,
            lowCount = low
        )
    }
}
