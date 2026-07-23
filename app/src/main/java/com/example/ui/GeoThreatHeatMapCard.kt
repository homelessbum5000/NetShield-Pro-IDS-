package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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

data class ThreatGeoOriginNode(
    val id: String,
    val cityName: String,
    val country: String,
    val lat: Float, // -90..90
    val lng: Float, // -180..180
    val heatIntensity: Float, // 0.0..1.0
    val activeThreatCount: Int,
    val primaryVector: String,
    val topSubnet: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeoThreatHeatMapCard() {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, CRITICAL, HARVEST_NOW, BOTNET
    var pulsingRadiusPhase by remember { mutableStateOf(0f) }

    // Sample global threat origin hotspots
    val allNodes = remember {
        listOf(
            ThreatGeoOriginNode("NODE-1", "Frankfurt", "Germany", 50.11f, 8.68f, 0.92f, 4120, "HTTP/2 Rapid Reset", "185.220.101.0/24"),
            ThreatGeoOriginNode("NODE-2", "Tokyo", "Japan", 35.67f, 139.65f, 0.78f, 2840, "Quantum Kyber Re-key Probe", "103.251.167.0/24"),
            ThreatGeoOriginNode("NODE-3", "San Jose", "USA", 37.33f, -121.88f, 0.45f, 1290, "SYN-Flood Volumetric", "198.51.100.0/24"),
            ThreatGeoOriginNode("NODE-4", "Moscow", "Russia", 55.75f, 37.61f, 0.98f, 6890, "Harvest-Now Decrypt-Later", "45.154.255.0/24"),
            ThreatGeoOriginNode("NODE-5", "São Paulo", "Brazil", -23.55f, -46.63f, 0.62f, 1950, "UDP Amplification", "177.12.89.0/24"),
            ThreatGeoOriginNode("NODE-6", "Singapore", "Singapore", 1.35f, 103.81f, 0.81f, 3420, "TLS Handshake Flood", "118.189.22.0/24"),
            ThreatGeoOriginNode("NODE-7", "London", "UK", 51.50f, -0.12f, 0.52f, 1640, "DNS Tunneling Exfiltration", "81.2.114.0/24"),
            ThreatGeoOriginNode("NODE-8", "Sydney", "Australia", -33.86f, 151.20f, 0.35f, 820, "Port Scan Sweep", "139.130.4.0/24")
        )
    }

    var selectedNode by remember { mutableStateOf<ThreatGeoOriginNode?>(allNodes[0]) }

    val filteredNodes = remember(selectedFilter) {
        when (selectedFilter) {
            "CRITICAL" -> allNodes.filter { it.heatIntensity > 0.8f }
            "HARVEST_NOW" -> allNodes.filter { it.primaryVector.contains("Harvest", ignoreCase = true) || it.primaryVector.contains("Kyber", ignoreCase = true) }
            "BOTNET" -> allNodes.filter { it.activeThreatCount > 3000 }
            else -> allNodes
        }
    }

    // Pulse animation phase loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(50L)
            pulsingRadiusPhase = (pulsingRadiusPhase + 0.05f) % 1.0f
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("geo_threat_heat_map_card"),
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
                        imageVector = Icons.Default.Public,
                        contentDescription = "Geographical Threat Heat Map",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Geographical Threat Heat Map",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Global Mercator origin projection & heat density overlay",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                // Heat Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF78350F).copy(alpha = 0.3f))
                        .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${filteredNodes.size} Origin Hotspots",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFDE68A),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            // Filter Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "ALL" to "All Hotspots",
                    "CRITICAL" to "Critical Density (>80%)",
                    "HARVEST_NOW" to "Quantum Harvest Vectors",
                    "BOTNET" to "High-Volume Botnets (>3k)"
                ).forEach { (tag, label) ->
                    FilterChip(
                        selected = selectedFilter == tag,
                        onClick = { selectedFilter = tag },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD97706),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Map Box with Interactive Touch Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.8f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Box {
                    WorldMapHeatCanvas(
                        nodes = filteredNodes,
                        selectedNode = selectedNode,
                        pulsePhase = pulsingRadiusPhase,
                        onNodeClick = { selectedNode = it },
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Hotspot Detail Inspection Card
            selectedNode?.let { node ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${node.cityName}, ${node.country}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            // Heat Intensity Percentage
                            Text(
                                text = "Heat Score: ${(node.heatIntensity * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (node.heatIntensity > 0.8f) Color(0xFFEF4444) else Color(0xFFF59E0B),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFF1E293B))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Primary Vector:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                                Text(node.primaryVector, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
                            }

                            Column {
                                Text("Origin CIDR Subnet:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                                Text(node.topSubnet, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 11.sp))
                            }

                            Column {
                                Text("Active Attacks:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                                Text("${node.activeThreatCount} / hr", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorldMapHeatCanvas(
    nodes: List<ThreatGeoOriginNode>,
    selectedNode: ThreatGeoOriginNode?,
    pulsePhase: Float,
    onNodeClick: (ThreatGeoOriginNode) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.clickable {
            // Pick node nearest to click if within threshold
        }
    ) {
        val width = size.width
        val height = size.height

        // Draw simplified dark world continent silhouettes using Path
        val gridColor = Color(0xFF1E293B)
        val continentColor = Color(0xFF151D2A)

        // Draw Lat/Lng Grid Lines
        for (xStep in 0..6) {
            val x = width * (xStep / 6f)
            drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1.dp.toPx())
        }
        for (yStep in 0..4) {
            val y = height * (yStep / 4f)
            drawLine(color = gridColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1.dp.toPx())
        }

        // Simplified continent outline shapes projected onto 0..1 normalized map coords
        val continentPaths = listOf(
            // North America
            listOf(0.12f to 0.22f, 0.28f to 0.18f, 0.32f to 0.38f, 0.22f to 0.48f, 0.15f to 0.38f),
            // South America
            listOf(0.28f to 0.52f, 0.38f to 0.55f, 0.32f to 0.85f, 0.26f to 0.68f),
            // Europe
            listOf(0.48f to 0.20f, 0.58f to 0.18f, 0.59f to 0.35f, 0.49f to 0.38f),
            // Africa
            listOf(0.47f to 0.42f, 0.59f to 0.42f, 0.58f to 0.75f, 0.50f to 0.65f),
            // Asia
            listOf(0.60f to 0.18f, 0.88f to 0.20f, 0.85f to 0.52f, 0.65f to 0.48f),
            // Australia
            listOf(0.78f to 0.65f, 0.88f to 0.65f, 0.86f to 0.82f, 0.76f to 0.80f)
        )

        continentPaths.forEach { shape ->
            val path = Path().apply {
                moveTo(shape[0].first * width, shape[0].second * height)
                for (i in 1 until shape.size) {
                    lineTo(shape[i].first * width, shape[i].second * height)
                }
                close()
            }
            drawPath(path = path, color = continentColor)
            drawPath(path = path, color = Color(0xFF334155), style = Stroke(width = 1.dp.toPx()))
        }

        // Map lat/lng -> canvas x/y
        fun mapCoords(lat: Float, lng: Float): Offset {
            val x = ((lng + 180f) / 360f) * width
            val y = ((90f - lat) / 180f) * height
            return Offset(x, y)
        }

        // Draw heat origin halos and nodes
        nodes.forEach { node ->
            val center = mapCoords(node.lat, node.lng)
            val isSelected = selectedNode?.id == node.id

            // Heat Gradient Aura
            val heatRadius = (16.dp.toPx() + (node.heatIntensity * 28.dp.toPx()))
            val auraColor = when {
                node.heatIntensity > 0.8f -> Color(0xFFEF4444)
                node.heatIntensity > 0.5f -> Color(0xFFF59E0B)
                else -> Color(0xFF38BDF8)
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auraColor.copy(alpha = 0.55f), auraColor.copy(alpha = 0.05f)),
                    center = center,
                    radius = heatRadius
                ),
                center = center,
                radius = heatRadius
            )

            // Animated Pulsing Expansion Ring
            val pulseRadius = heatRadius * (0.6f + (pulsePhase * 0.8f))
            drawCircle(
                color = auraColor.copy(alpha = (1.0f - pulsePhase).coerceIn(0f, 1f)),
                radius = pulseRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Center Point Marker
            drawCircle(
                color = if (isSelected) Color.White else auraColor,
                radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                center = center
            )

            if (isSelected) {
                drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
