package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thunderstorm
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ThreatLogEntity
import kotlin.math.roundToInt

data class HeatmapCellData(
    val dayIndex: Int,       // 0..6 (Mon..Sun)
    val hourBlockIndex: Int, // 0..5 (00-04, 04-08, 08-12, 12-16, 16-20, 20-24)
    val densityScore: Float, // 0.0..1.0
    val eventCount: Int,
    val primaryVector: String,
    val peakSeverity: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThreatDensityHeatmapCard(
    threatLogs: List<ThreatLogEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTimeWindow by remember { mutableStateOf("7_DAYS") } // 24_HOURS, 7_DAYS, 30_DAYS
    var selectedViewMode by remember { mutableStateOf("DAYS_VS_HOURS") } // DAYS_VS_HOURS, VECTORS_VS_HOURS
    var selectedCell by remember { mutableStateOf<HeatmapCellData?>(null) }
    var pulseAnimationPhase by remember { mutableStateOf(0f) }

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val hourBlockLabels = listOf("00-04", "04-08", "08-12", "12-16", "16-20", "20-24")

    val vectorLabels = listOf(
        "SYN-Flood",
        "Quantum-Probe",
        "HTTP/2 Reset",
        "DNS Amplification",
        "BGP Hijack",
        "XSS Payload"
    )

    // Generate/calculate matrix heatmap cells incorporating Room DB threat logs
    val heatmapMatrix = remember(threatLogs, selectedTimeWindow, selectedViewMode, pulseAnimationPhase) {
        val rows = if (selectedViewMode == "DAYS_VS_HOURS") 7 else 6
        val cols = 6
        val matrix = mutableListOf<HeatmapCellData>()

        // Base realistic baseline seeds
        val baseSeed = listOf(
            listOf(0.15f, 0.22f, 0.45f, 0.88f, 0.95f, 0.62f),
            listOf(0.10f, 0.18f, 0.52f, 0.91f, 0.78f, 0.44f),
            listOf(0.25f, 0.30f, 0.68f, 0.98f, 0.85f, 0.55f),
            listOf(0.12f, 0.20f, 0.48f, 0.82f, 0.92f, 0.60f),
            listOf(0.35f, 0.42f, 0.75f, 0.99f, 0.89f, 0.71f),
            listOf(0.08f, 0.15f, 0.32f, 0.65f, 0.58f, 0.38f),
            listOf(0.05f, 0.12f, 0.28f, 0.50f, 0.45f, 0.30f)
        )

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val seedVal = baseSeed.getOrNull(r % 7)?.getOrNull(c) ?: 0.3f
                // Augment density based on actual Room DB logs count
                val extraLogDensity = (threatLogs.size * 0.02f).coerceAtMost(0.2f)
                val density = (seedVal + extraLogDensity).coerceIn(0.05f, 1.0f)
                val count = (density * 1800 + (r + c * 15)).toInt()

                val vector = vectorLabels[(r + c) % vectorLabels.size]
                val severity = when {
                    density > 0.8f -> "CRITICAL"
                    density > 0.5f -> "HIGH"
                    density > 0.25f -> "MEDIUM"
                    else -> "LOW"
                }

                matrix.add(
                    HeatmapCellData(
                        dayIndex = r,
                        hourBlockIndex = c,
                        densityScore = density,
                        eventCount = count,
                        primaryVector = vector,
                        peakSeverity = severity
                    )
                )
            }
        }
        matrix
    }

    // Default selection to peak cell if none selected
    LaunchedEffect(heatmapMatrix) {
        if (selectedCell == null) {
            selectedCell = heatmapMatrix.maxByOrNull { it.densityScore }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("threat_density_heatmap_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A) // Sleek cyber dark background
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title & Heatmap Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Threat Density Temporal Heatmap",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                        }
                        Text(
                            text = "NetShield Pro density matrix & temporal distribution",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                // Peak Indicator Pill
                val peakCell = heatmapMatrix.maxByOrNull { it.densityScore }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFDC2626).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PEAK: ${(peakCell?.densityScore ?: 0.95f * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFCA5A5),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Control Bar: View Mode & Time Range Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Mode Selectors
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val viewModes = listOf(
                        "DAYS_VS_HOURS" to "Days x Hours",
                        "VECTORS_VS_HOURS" to "Vectors x Hours"
                    )

                    viewModes.forEach { (modeKey, label) ->
                        val isSelected = selectedViewMode == modeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedViewMode = modeKey
                                selectedCell = null
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFF334155),
                                selectedBorderColor = Color(0xFF38BDF8)
                            ),
                            modifier = Modifier.testTag("view_mode_chip_$modeKey")
                        )
                    }
                }

                // Refresh Button
                IconButton(
                    onClick = {
                        pulseAnimationPhase += 0.1f
                        Toast.makeText(context, "Refreshed threat density heatmap calculations", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Matrix",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Heatmap Canvas Matrix
            HeatmapGridCanvas(
                matrixData = heatmapMatrix,
                rows = if (selectedViewMode == "DAYS_VS_HOURS") 7 else 6,
                cols = 6,
                rowLabels = if (selectedViewMode == "DAYS_VS_HOURS") dayLabels else vectorLabels,
                colLabels = hourBlockLabels,
                selectedCell = selectedCell,
                onSelectCell = { selectedCell = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Color Gradient Key / Legend
            HeatmapGradientLegendBar()

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Cell Detail Inspector Banner
            selectedCell?.let { cell ->
                CellDetailInspectorCard(
                    cell = cell,
                    rowLabel = if (selectedViewMode == "DAYS_VS_HOURS")
                        dayLabels.getOrElse(cell.dayIndex) { "Day ${cell.dayIndex}" }
                    else vectorLabels.getOrElse(cell.dayIndex) { "Vector ${cell.dayIndex}" },
                    colLabel = hourBlockLabels.getOrElse(cell.hourBlockIndex) { "Time Slot" }
                )
            }
        }
    }
}

@Composable
fun HeatmapGridCanvas(
    matrixData: List<HeatmapCellData>,
    rows: Int,
    cols: Int,
    rowLabels: List<String>,
    colLabels: List<String>,
    selectedCell: HeatmapCellData?,
    onSelectCell: (HeatmapCellData) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    val labelTextStyle = TextStyle(
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF94A3B8),
        fontWeight = FontWeight.Medium
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF020617))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .testTag("threat_heatmap_canvas_box")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .pointerInput(matrixData, rows, cols) {
                    detectTapGestures { offset ->
                        val leftPadding = 80f // space for row Y axis labels
                        val topPadding = 30f  // space for column X axis labels
                        val cellWidth = (size.width - leftPadding - 16f) / cols
                        val cellHeight = (size.height - topPadding - 16f) / rows

                        val tappedCol = ((offset.x - leftPadding) / cellWidth).toInt()
                        val tappedRow = ((offset.y - topPadding) / cellHeight).toInt()

                        if (tappedRow in 0 until rows && tappedCol in 0 until cols) {
                            val clickedCell = matrixData.find {
                                it.dayIndex == tappedRow && it.hourBlockIndex == tappedCol
                            }
                            clickedCell?.let { onSelectCell(it) }
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val leftPadding = 80f
            val topPadding = 30f
            val rightPadding = 16f
            val bottomPadding = 16f

            val gridWidth = canvasWidth - leftPadding - rightPadding
            val gridHeight = canvasHeight - topPadding - bottomPadding

            val cellW = gridWidth / cols
            val cellH = gridHeight / rows

            // 1. Draw Column Labels (X Axis - Hours)
            for (c in 0 until cols) {
                val label = colLabels.getOrElse(c) { "" }
                val measured = textMeasurer.measure(label, labelTextStyle)
                val xPos = leftPadding + (c * cellW) + (cellW - measured.size.width) / 2f
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    topLeft = Offset(xPos, 6f),
                    style = labelTextStyle
                )
            }

            // 2. Draw Row Labels & Grid Cells
            for (r in 0 until rows) {
                // Row Label (Y Axis)
                val rowLabel = rowLabels.getOrElse(r) { "" }
                val measuredRow = textMeasurer.measure(rowLabel, labelTextStyle)
                val yPosLabel = topPadding + (r * cellH) + (cellH - measuredRow.size.height) / 2f
                drawText(
                    textMeasurer = textMeasurer,
                    text = rowLabel,
                    topLeft = Offset(12f, yPosLabel),
                    style = labelTextStyle
                )

                // Cells in Row
                for (c in 0 until cols) {
                    val cellData = matrixData.find { it.dayIndex == r && it.hourBlockIndex == c }
                    val density = cellData?.densityScore ?: 0.1f

                    val cellX = leftPadding + (c * cellW) + 2f
                    val cellY = topPadding + (r * cellH) + 2f
                    val cellWidthActual = cellW - 4f
                    val cellHeightActual = cellH - 4f

                    // Color mapping based on density
                    val cellColor = getDensityColor(density)

                    // Draw Cell Box
                    drawRoundRect(
                        color = cellColor,
                        topLeft = Offset(cellX, cellY),
                        size = Size(cellWidthActual, cellHeightActual),
                        cornerRadius = CornerRadius(4f, 4f)
                    )

                    // Highlight if selected
                    val isSelected = selectedCell?.dayIndex == r && selectedCell.hourBlockIndex == c
                    if (isSelected) {
                        drawRoundRect(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(cellX - 1f, cellY - 1f),
                            size = Size(cellWidthActual + 2f, cellHeightActual + 2f),
                            cornerRadius = CornerRadius(5f, 5f),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeatmapGradientLegendBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Low Density (0%)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )
        )

        Box(
            modifier = Modifier
                .width(160.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A), // Low - Blue
                            Color(0xFF0284C7), // Light Blue
                            Color(0xFFF59E0B), // Yellow / Orange
                            Color(0xFFEF4444), // Bright Red
                            Color(0xFF7F1D1D)  // Deep Glowing Crimson
                        )
                    )
                )
        )

        Text(
            text = "Critical Density (100%)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun CellDetailInspectorCard(
    cell: HeatmapCellData,
    rowLabel: String,
    colLabel: String
) {
    val densityPct = (cell.densityScore * 100).roundToInt()
    val badgeBg = when (cell.peakSeverity.uppercase()) {
        "CRITICAL" -> Color(0xFF7F1D1D)
        "HIGH" -> Color(0xFF7C2D12)
        "MEDIUM" -> Color(0xFF78350F)
        else -> Color(0xFF1E3A8A)
    }

    val badgeText = when (cell.peakSeverity.uppercase()) {
        "CRITICAL" -> Color(0xFFFCA5A5)
        "HIGH" -> Color(0xFFFDBA74)
        "MEDIUM" -> Color(0xFFFDE68A)
        else -> Color(0xFF93C5FD)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("heatmap_cell_inspector_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$rowLabel ($colLabel UTC)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeText.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = cell.peakSeverity,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color(0xFF334155)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Density Index Column
                Column {
                    Text(
                        text = "Threat Density Index",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "$densityPct%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = getDensityColor(cell.densityScore),
                            fontSize = 16.sp
                        )
                    )
                }

                // Total Logged Interceptions
                Column {
                    Text(
                        text = "Intercepted Events",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "${cell.eventCount} packets",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Primary Threat Vector
                Column {
                    Text(
                        text = "Dominant Vector",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = cell.primaryVector,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

private fun getDensityColor(density: Float): Color {
    return when {
        density >= 0.85f -> Color(0xFFDC2626) // Glowing Red
        density >= 0.70f -> Color(0xFFEF4444) // High Red
        density >= 0.50f -> Color(0xFFF97316) // Orange
        density >= 0.35f -> Color(0xFFF59E0B) // Yellow / Gold
        density >= 0.20f -> Color(0xFF0284C7) // Light Cyber Blue
        else -> Color(0xFF1E3A8A)            // Deep Slate Blue
    }
}
