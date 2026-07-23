package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuantumTunnelState(
    val id: String,
    val name: String,
    val region: String,
    val cipherSuite: String, // e.g. "ML-KEM-1024 + AES-256-GCM"
    var latencyMs: Int,
    var jitterMs: Int,
    var packetLossPct: Float,
    var healthStatus: String, // "OPTIMAL", "DEGRADED", "REKEYING"
    val uptimePct: String = "99.99%",
    var lastKeyRotation: String,
    val latencyHistory: List<Int> // 10 historic latency samples for sparkline
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuantumTunnelHealthWidgetCard() {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, OPTIMAL, DEGRADED
    var isPingTestRunning by remember { mutableStateOf(false) }

    // Initial tunnel health state
    val tunnels = remember {
        mutableStateListOf(
            QuantumTunnelState(
                id = "TUN-US-EAST",
                name = "US-East (Virginia Primary)",
                region = "us-east-1",
                cipherSuite = "ML-KEM-1024 (Kyber)",
                latencyMs = 14,
                jitterMs = 1,
                packetLossPct = 0.0f,
                healthStatus = "OPTIMAL",
                lastKeyRotation = "2 mins ago",
                latencyHistory = listOf(16, 15, 14, 14, 13, 15, 14, 14, 15, 14)
            ),
            QuantumTunnelState(
                id = "TUN-EU-WEST",
                name = "EU-Central (Frankfurt)",
                region = "eu-central-1",
                cipherSuite = "ML-KEM-768 + Dilithium-3",
                latencyMs = 78,
                jitterMs = 4,
                packetLossPct = 0.1f,
                healthStatus = "OPTIMAL",
                lastKeyRotation = "8 mins ago",
                latencyHistory = listOf(82, 80, 79, 78, 81, 77, 78, 79, 78, 78)
            ),
            QuantumTunnelState(
                id = "TUN-AP-EAST",
                name = "AP-East (Tokyo Mesh)",
                region = "ap-northeast-1",
                cipherSuite = "ML-KEM-1024 (Kyber)",
                latencyMs = 142,
                jitterMs = 18,
                packetLossPct = 1.2f,
                healthStatus = "DEGRADED",
                lastKeyRotation = "14 mins ago",
                latencyHistory = listOf(120, 135, 140, 155, 142, 160, 145, 142, 150, 142)
            ),
            QuantumTunnelState(
                id = "TUN-SA-EAST",
                name = "SA-East (São Paulo)",
                region = "sa-east-1",
                cipherSuite = "Kyber-1024 Hybrid",
                latencyMs = 118,
                jitterMs = 3,
                packetLossPct = 0.0f,
                healthStatus = "OPTIMAL",
                lastKeyRotation = "5 mins ago",
                latencyHistory = listOf(122, 120, 119, 118, 121, 118, 117, 118, 119, 118)
            )
        )
    }

    // Real-time jitter and ping fluctuation simulation loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(2500L)
            tunnels.indices.forEach { index ->
                val curr = tunnels[index]
                val jitterDelta = (-3..3).random()
                val newLat = (curr.latencyMs + jitterDelta).coerceIn(8, 250)
                val newHistory = curr.latencyHistory.drop(1) + newLat
                tunnels[index] = curr.copy(
                    latencyMs = newLat,
                    jitterMs = (curr.jitterMs + (-1..1).random()).coerceIn(0, 25),
                    latencyHistory = newHistory
                )
            }
        }
    }

    val filteredTunnels = remember(selectedFilter, tunnels) {
        when (selectedFilter) {
            "OPTIMAL" -> tunnels.filter { it.healthStatus == "OPTIMAL" }
            "DEGRADED" -> tunnels.filter { it.healthStatus == "DEGRADED" }
            else -> tunnels
        }
    }

    val averageLatency = remember(tunnels) {
        if (tunnels.isEmpty()) 0 else tunnels.map { it.latencyMs }.average().toInt()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quantum_tunnel_health_widget_card"),
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
                        imageVector = Icons.Default.Router,
                        contentDescription = "Quantum Tunnel Health",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Quantum Tunnel Health & Latency",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Real-time post-quantum VPN connection telemetry",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                // Average Latency Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Avg ${averageLatency}ms",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            // Top Quick Metrics Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active Tunnels
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Active Tunnels", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${tunnels.size} Mesh Nodes", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold))
                    }
                }

                // Post-Quantum Cipher
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Encryption Standard", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("ML-KEM-1024", style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFA855F7), fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips + Ping Trigger Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL", "OPTIMAL", "DEGRADED").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                IconButton(
                    onClick = {
                        isPingTestRunning = true
                        // Trigger fast latency re-check animation
                        tunnels.indices.forEach { idx ->
                            val item = tunnels[idx]
                            tunnels[idx] = item.copy(latencyMs = (10..120).random())
                        }
                        isPingTestRunning = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Ping",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tunnel List Items
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredTunnels.forEach { tunnel ->
                    val statusColor = when (tunnel.healthStatus) {
                        "OPTIMAL" -> Color(0xFF10B981)
                        "DEGRADED" -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Row 1: Name & Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(statusColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tunnel.name,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Text(
                                    text = "${tunnel.latencyMs} ms",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = if (tunnel.latencyMs < 50) Color(0xFF10B981) else if (tunnel.latencyMs < 100) Color(0xFFF59E0B) else Color(0xFFEF4444),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Row 2: Cipher Suite & Jitter / Packet Loss
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Cipher: ${tunnel.cipherSuite}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )

                                Text(
                                    text = "Jitter: ${tunnel.jitterMs}ms | Loss: ${tunnel.packetLossPct}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFCBD5E1),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Row 3: Sparkline Latency Graph + Re-Key Trigger Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sparkline Graph
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(24.dp)
                                        .padding(end = 12.dp)
                                ) {
                                    TunnelSparklineCanvas(
                                        latencyHistory = tunnel.latencyHistory,
                                        lineColor = statusColor,
                                        modifier = Modifier.matchParentSize()
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        // Simulate manual Quantum Re-Keying
                                        val idx = tunnels.indexOf(tunnel)
                                        if (idx != -1) {
                                            tunnels[idx] = tunnel.copy(
                                                healthStatus = "OPTIMAL",
                                                lastKeyRotation = "Just now",
                                                packetLossPct = 0.0f
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                                ) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Re-Key", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TunnelSparklineCanvas(
    latencyHistory: List<Int>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (latencyHistory.size < 2) return@Canvas

        val width = size.width
        val height = size.height

        val maxVal = (latencyHistory.maxOrNull() ?: 100).toFloat().coerceAtLeast(10f)
        val minVal = (latencyHistory.minOrNull() ?: 0).toFloat().coerceAtMost(maxVal - 5f)

        val points = latencyHistory.mapIndexed { index, value ->
            val x = (index.toFloat() / (latencyHistory.size - 1)) * width
            val normalizedY = (value.toFloat() - minVal) / (maxVal - minVal)
            val y = height - (normalizedY * height)
            Offset(x, y)
        }

        val strokePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
