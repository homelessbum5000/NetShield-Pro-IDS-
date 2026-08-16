package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DataThresholding
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ChartMetricType(val title: String, val unit: String, val primaryColor: Color, val secondaryColor: Color) {
    BANDWIDTH("Bandwidth (In / Out)", "Mbps", Color(0xFF38BDF8), Color(0xFF818CF8)),
    PACKET_RATE("Packet Rate", "Kpps", Color(0xFF34D399), Color(0xFF10B981)),
    THREAT_INDEX("Threat Risk Level", "Score", Color(0xFFF43F5E), Color(0xFFFB923C)),
    LATENCY("Network Latency & Jitter", "ms", Color(0xFFA78BFA), Color(0xFFC084FC))
}

enum class DashboardTrafficScenario(val label: String, val description: String, val baseInbound: Float, val baseOutbound: Float, val threatLevel: Int) {
    NORMAL_ENTERPRISE("Normal Enterprise", "Steady encrypted traffic with standard telemetry", 42.5f, 18.2f, 4),
    DDOS_SPIKE("DDoS Syn Flood Attack", "Volumetric burst on port 443 with anomalous packet spikes", 380.0f, 125.0f, 94),
    C2_BEACONING("C2 Beaconing Detected", "Periodic covert exfiltration pulses on DNS tunneling", 68.0f, 92.4f, 78),
    RECON_PORT_SCAN("Mass Reconnaissance Scan", "Distributed horizontal SYN scanning detected across IP ranges", 85.0f, 22.0f, 62)
}

data class RechartsTrafficDataPoint(
    val id: Int,
    val timeLabel: String,
    val inboundMbps: Float,
    val outboundMbps: Float,
    val threatScore: Float,
    val latencyMs: Float,
    val activeThreatClass: String? = null
)

data class ThreatClassDistribution(
    val category: String,
    val blockedCount: Int,
    val percentage: Float,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RealtimeNetworkThreatRechartsDashboardCard(
    modifier: Modifier = Modifier
) {
    var isLiveStreaming by remember { mutableStateOf(true) }
    var selectedMetric by remember { mutableStateOf(ChartMetricType.BANDWIDTH) }
    var activeScenario by remember { mutableStateOf(DashboardTrafficScenario.NORMAL_ENTERPRISE) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var isSimulatingAttack by remember { mutableStateOf(false) }
    var showThreatDetails by remember { mutableStateOf(false) }

    // Stream of real-time 30-sample sliding window
    var trafficDataPoints by remember {
        mutableStateOf(generateInitialTrafficPoints(activeScenario))
    }

    val context = LocalContext.current

    // Live update ticker effect
    LaunchedEffect(isLiveStreaming, activeScenario) {
        var counter = trafficDataPoints.size
        while (isLiveStreaming) {
            delay(1200)
            counter++
            val timeString = "${(counter % 60).toString().padStart(2, '0')}s"

            val baseIn = activeScenario.baseInbound
            val baseOut = activeScenario.baseOutbound
            val threatBase = activeScenario.threatLevel.toFloat()

            val jitterIn = (Random.nextFloat() - 0.45f) * (baseIn * 0.25f)
            val jitterOut = (Random.nextFloat() - 0.45f) * (baseOut * 0.25f)
            val jitterThreat = (Random.nextFloat() - 0.4f) * 12f
            val jitterLatency = 12f + (Random.nextFloat() * 8f) + (if (threatBase > 50) 25f else 0f)

            val newPoint = RechartsTrafficDataPoint(
                id = counter,
                timeLabel = timeString,
                inboundMbps = (baseIn + jitterIn).coerceAtLeast(2.0f),
                outboundMbps = (baseOut + jitterOut).coerceAtLeast(1.0f),
                threatScore = (threatBase + jitterThreat).coerceIn(0f, 100f),
                latencyMs = jitterLatency,
                activeThreatClass = if (threatBase > 60) activeScenario.label else null
            )

            trafficDataPoints = (trafficDataPoints.drop(1) + newPoint)
        }
    }

    // Threat distribution categories
    val threatCategories = remember(activeScenario, trafficDataPoints) {
        val totalThreats = if (activeScenario == DashboardTrafficScenario.NORMAL_ENTERPRISE) 18 else 142
        listOf(
            ThreatClassDistribution("DDoS / Syn Flood", if (activeScenario == DashboardTrafficScenario.DDOS_SPIKE) 85 else 4, 0.45f, Color(0xFFEF4444)),
            ThreatClassDistribution("C2 Beaconing", if (activeScenario == DashboardTrafficScenario.C2_BEACONING) 42 else 3, 0.25f, Color(0xFFF59E0B)),
            ThreatClassDistribution("Port Scanning", if (activeScenario == DashboardTrafficScenario.RECON_PORT_SCAN) 38 else 6, 0.20f, Color(0xFF8B5CF6)),
            ThreatClassDistribution("DNS Exfiltration", 5, 0.10f, Color(0xFF06B6D4))
        )
    }

    val latestPoint = trafficDataPoints.lastOrNull() ?: RechartsTrafficDataPoint(0, "00s", 42f, 18f, 5f, 14f)
    val currentThreatScore = latestPoint.threatScore
    val isThreatCritical = currentThreatScore >= 60f

    // Pulsing indicator for active live stream
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("realtime_network_threat_recharts_dashboard_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090E1A)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isThreatCritical) Color(0xFFEF4444).copy(alpha = 0.8f) else Color(0xFF38BDF8).copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Dashboard Title & Live Stream Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isThreatCritical) Color(0xFFEF4444).copy(alpha = 0.15f)
                                else Color(0xFF38BDF8).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isThreatCritical) Icons.Default.CrisisAlert else Icons.Default.ShowChart,
                            contentDescription = "Real-Time Traffic Dashboard",
                            tint = if (isThreatCritical) Color(0xFFEF4444) else Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Real-Time Network & Threat Dashboard",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isLiveStreaming) (if (isThreatCritical) Color(0xFFEF4444) else Color(0xFF10B981)).copy(alpha = pulseAlpha)
                                        else Color(0xFF64748B)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLiveStreaming) "LIVE STREAM (1.2s Interval)" else "STREAM PAUSED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isLiveStreaming) (if (isThreatCritical) Color(0xFFF87171) else Color(0xFF34D399)) else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Pause / Resume Stream Button
                IconButton(
                    onClick = { isLiveStreaming = !isLiveStreaming },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .testTag("toggle_recharts_live_stream_button")
                ) {
                    Icon(
                        imageVector = if (isLiveStreaming) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Stream",
                        tint = if (isLiveStreaming) Color(0xFF38BDF8) else Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E293B))

            // Real-Time High-Level Metric Tiles (Bandwidth, Packets, Threat Score, Status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricKpiCard(
                    title = "Inbound Flow",
                    value = String.format("%.1f", latestPoint.inboundMbps),
                    unit = "Mbps",
                    trendIcon = Icons.Default.TrendingUp,
                    accentColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Outbound Flow",
                    value = String.format("%.1f", latestPoint.outboundMbps),
                    unit = "Mbps",
                    trendIcon = Icons.Default.TrendingDown,
                    accentColor = Color(0xFF818CF8),
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Threat Index",
                    value = "${latestPoint.threatScore.toInt()}",
                    unit = "/ 100",
                    trendIcon = if (isThreatCritical) Icons.Default.Warning else Icons.Default.CheckCircle,
                    accentColor = if (isThreatCritical) Color(0xFFEF4444) else Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = "Latency",
                    value = String.format("%.1f", latestPoint.latencyMs),
                    unit = "ms",
                    trendIcon = Icons.Default.Speed,
                    accentColor = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Selector Tabs (Bandwidth, Packet Rate, Threat Risk, Latency)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChartMetricType.values().forEach { metric ->
                    val isSelected = selectedMetric == metric
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedMetric = metric
                            selectedPointIndex = null
                        },
                        label = {
                            Text(
                                text = metric.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = metric.primaryColor.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF131C31),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) metric.primaryColor else Color(0xFF334155),
                            enabled = true,
                            selected = isSelected
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Interactive Recharts-Style Chart Container
            Surface(
                color = Color(0xFF060B14),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Chart Top Legend & Tooltip readout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(selectedMetric.primaryColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (selectedMetric == ChartMetricType.BANDWIDTH) "Inbound" else "Primary",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                )
                            }
                            if (selectedMetric == ChartMetricType.BANDWIDTH) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(selectedMetric.secondaryColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Outbound",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                    )
                                }
                            }
                        }

                        // Scrubber Tooltip Hover Value
                        val hoveredPoint = selectedPointIndex?.let { trafficDataPoints.getOrNull(it) } ?: latestPoint
                        Text(
                            text = "T=${hoveredPoint.timeLabel} | " + when (selectedMetric) {
                                ChartMetricType.BANDWIDTH -> "In: ${String.format("%.1f", hoveredPoint.inboundMbps)} / Out: ${String.format("%.1f", hoveredPoint.outboundMbps)} Mbps"
                                ChartMetricType.PACKET_RATE -> "Rate: ${String.format("%.1f", hoveredPoint.inboundMbps * 1.8f)} Kpps"
                                ChartMetricType.THREAT_INDEX -> "Risk: ${hoveredPoint.threatScore.toInt()}/100"
                                ChartMetricType.LATENCY -> "RTT: ${String.format("%.1f", hoveredPoint.latencyMs)} ms"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = selectedMetric.primaryColor,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Compose Canvas Recharts Engine
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(trafficDataPoints) {
                                detectTapGestures { offset ->
                                    val count = trafficDataPoints.size
                                    if (count > 1) {
                                        val step = size.width / (count - 1)
                                        val idx = (offset.x / step).toInt().coerceIn(0, count - 1)
                                        selectedPointIndex = idx
                                    }
                                }
                            }
                            .pointerInput(trafficDataPoints) {
                                detectDragGestures { change, _ ->
                                    val count = trafficDataPoints.size
                                    if (count > 1) {
                                        val step = size.width / (count - 1)
                                        val idx = (change.position.x / step).toInt().coerceIn(0, count - 1)
                                        selectedPointIndex = idx
                                    }
                                }
                            }
                    ) {
                        RechartsNetworkCanvasChart(
                            dataPoints = trafficDataPoints,
                            metricType = selectedMetric,
                            selectedIndex = selectedPointIndex,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Threat Scenario Simulation Selector
            Text(
                text = "Live Traffic Scenario Simulation:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold, fontSize = 10.sp)
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DashboardTrafficScenario.values().forEach { scen ->
                    val isSelected = activeScenario == scen
                    val chipColor = when (scen) {
                        DashboardTrafficScenario.NORMAL_ENTERPRISE -> Color(0xFF10B981)
                        DashboardTrafficScenario.DDOS_SPIKE -> Color(0xFFEF4444)
                        DashboardTrafficScenario.C2_BEACONING -> Color(0xFFF59E0B)
                        DashboardTrafficScenario.RECON_PORT_SCAN -> Color(0xFF8B5CF6)
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            activeScenario = scen
                            trafficDataPoints = generateInitialTrafficPoints(scen)
                            selectedPointIndex = null
                            Toast.makeText(context, "Scenario switched: ${scen.label}", Toast.LENGTH_SHORT).show()
                        },
                        label = {
                            Text(
                                text = scen.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF131C31),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) chipColor else Color(0xFF334155),
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Threat Classification Breakdown Bar
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Threat Detection Status Breakdown",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            )
                        }
                        Text(
                            text = if (isThreatCritical) "ATTACK MITIGATION IN PROGRESS" else "ALL PROTOCOLS CLEAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isThreatCritical) Color(0xFFEF4444) else Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Multi-Segment Recharts Stacked Horizontal Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    ) {
                        threatCategories.forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .weight(cat.percentage)
                                    .background(cat.color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Legend Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        threatCategories.forEach { cat ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(cat.color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${cat.category}: ${cat.blockedCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Interactive Threat Details Toggle & Instant Threat Mitigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        activeScenario = DashboardTrafficScenario.NORMAL_ENTERPRISE
                        trafficDataPoints = generateInitialTrafficPoints(DashboardTrafficScenario.NORMAL_ENTERPRISE)
                        Toast.makeText(context, "Shield Applied: Threat mitigation verified. Baselining clean traffic.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.EnhancedEncryption, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Quantum Shield", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showThreatDetails = !showThreatDetails },
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = if (showThreatDetails) "Hide Telemetry" else "Telemetry Details",
                        fontSize = 10.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            // Expanded Telemetry & Metric Logs
            if (showThreatDetails) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF030712),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Recharts Live Telemetry Stream Metadata",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Active Stream Window: 30 Real-Time sliding ticks (1.2s tick cadence)\n" +
                                    "• Current Bandwidth Aggregate: In ${String.format("%.1f", latestPoint.inboundMbps)} Mbps | Out ${String.format("%.1f", latestPoint.outboundMbps)} Mbps\n" +
                                    "• Packet Loss: 0.002% | Jitter: 1.1ms | TLS 1.3 ML-KEM Key Exchange: ACTIVE\n" +
                                    "• Threat Status: ${if (isThreatCritical) "ANOMALY ISOLATION TRIGGERED (${activeScenario.label})" else "ALL SYSTEM FLOWS NORMAL"}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                lineHeight = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricKpiCard(
    title: String,
    value: String,
    unit: String,
    trendIcon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF131C31)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp)
                )
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
fun RechartsNetworkCanvasChart(
    dataPoints: List<RechartsTrafficDataPoint>,
    metricType: ChartMetricType,
    selectedIndex: Int?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (dataPoints.size < 2) return@Canvas

        val w = size.width
        val h = size.height
        val paddingLeft = 24.dp.toPx()
        val paddingBottom = 20.dp.toPx()
        val paddingTop = 10.dp.toPx()
        val paddingRight = 10.dp.toPx()

        val chartWidth = w - paddingLeft - paddingRight
        val chartHeight = h - paddingTop - paddingBottom

        // Compute dynamic Max Y
        val maxY = when (metricType) {
            ChartMetricType.BANDWIDTH -> {
                val maxVal = dataPoints.maxOf { maxOf(it.inboundMbps, it.outboundMbps) }
                (maxVal * 1.25f).coerceAtLeast(50f)
            }
            ChartMetricType.PACKET_RATE -> {
                val maxVal = dataPoints.maxOf { it.inboundMbps * 1.8f }
                (maxVal * 1.25f).coerceAtLeast(60f)
            }
            ChartMetricType.THREAT_INDEX -> 100f
            ChartMetricType.LATENCY -> {
                val maxVal = dataPoints.maxOf { it.latencyMs }
                (maxVal * 1.3f).coerceAtLeast(30f)
            }
        }

        // Draw Subtle Recharts Horizontal Gridlines
        val gridLines = 4
        for (i in 0..gridLines) {
            val yNorm = i.toFloat() / gridLines
            val yPos = paddingTop + (chartHeight * (1f - yNorm))

            drawLine(
                color = Color(0xFF1E293B).copy(alpha = 0.7f),
                start = Offset(paddingLeft, yPos),
                end = Offset(w - paddingRight, yPos),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            // Y-Axis Labels
            val yLabelValue = (maxY * yNorm).toInt()
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#64748B")
                    textSize = 8.dp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                drawText("$yLabelValue", paddingLeft - 4.dp.toPx(), yPos + 3.dp.toPx(), paint)
            }
        }

        // Extract series values
        val primaryValues = dataPoints.map {
            when (metricType) {
                ChartMetricType.BANDWIDTH -> it.inboundMbps
                ChartMetricType.PACKET_RATE -> it.inboundMbps * 1.8f
                ChartMetricType.THREAT_INDEX -> it.threatScore
                ChartMetricType.LATENCY -> it.latencyMs
            }
        }

        val secondaryValues = if (metricType == ChartMetricType.BANDWIDTH) {
            dataPoints.map { it.outboundMbps }
        } else null

        val stepX = chartWidth / (dataPoints.size - 1)

        // Helper to build smooth Bezier Path
        fun buildSmoothPath(values: List<Float>): Path {
            val path = Path()
            val points = values.mapIndexed { idx, v ->
                val x = paddingLeft + (idx * stepX)
                val yNorm = (v / maxY).coerceIn(0f, 1f)
                val y = paddingTop + (chartHeight * (1f - yNorm))
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                path.moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                    val controlY2 = p1.y
                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }
            }
            return path
        }

        // Draw Secondary Series (Outbound for Bandwidth)
        if (secondaryValues != null) {
            val secPath = buildSmoothPath(secondaryValues)
            val secAreaPath = Path().apply {
                addPath(secPath)
                lineTo(paddingLeft + chartWidth, paddingTop + chartHeight)
                lineTo(paddingLeft, paddingTop + chartHeight)
                close()
            }

            drawPath(
                path = secAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        metricType.secondaryColor.copy(alpha = 0.25f),
                        metricType.secondaryColor.copy(alpha = 0.02f)
                    ),
                    startY = paddingTop,
                    endY = paddingTop + chartHeight
                )
            )

            drawPath(
                path = secPath,
                color = metricType.secondaryColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Draw Primary Series (Inbound / Main Metric)
        val primaryPath = buildSmoothPath(primaryValues)
        val primaryAreaPath = Path().apply {
            addPath(primaryPath)
            lineTo(paddingLeft + chartWidth, paddingTop + chartHeight)
            lineTo(paddingLeft, paddingTop + chartHeight)
            close()
        }

        drawPath(
            path = primaryAreaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    metricType.primaryColor.copy(alpha = 0.35f),
                    metricType.primaryColor.copy(alpha = 0.02f)
                ),
                startY = paddingTop,
                endY = paddingTop + chartHeight
            )
        )

        drawPath(
            path = primaryPath,
            color = metricType.primaryColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw X-Axis Ticks
        val stepLabelInterval = 5
        dataPoints.forEachIndexed { idx, pt ->
            if (idx % stepLabelInterval == 0 || idx == dataPoints.size - 1) {
                val x = paddingLeft + (idx * stepX)
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 8.dp.toPx()
                        isAntiAlias = true
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(pt.timeLabel, x, h - 2.dp.toPx(), paint)
                }
            }
        }

        // Draw Scrubber Line & Active Data Point Ring if selected
        selectedIndex?.let { idx ->
            if (idx in dataPoints.indices) {
                val x = paddingLeft + (idx * stepX)
                val pVal = primaryValues[idx]
                val yNorm = (pVal / maxY).coerceIn(0f, 1f)
                val y = paddingTop + (chartHeight * (1f - yNorm))

                // Vertical Scrubber Line
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(x, paddingTop),
                    end = Offset(x, paddingTop + chartHeight),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )

                // Outer Highlight Circle
                drawCircle(
                    color = metricType.primaryColor.copy(alpha = 0.3f),
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )

                // Inner Solid Dot
                drawCircle(
                    color = metricType.primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}

private fun generateInitialTrafficPoints(scenario: DashboardTrafficScenario): List<RechartsTrafficDataPoint> {
    val list = mutableListOf<RechartsTrafficDataPoint>()
    val baseIn = scenario.baseInbound
    val baseOut = scenario.baseOutbound
    val threatBase = scenario.threatLevel.toFloat()

    for (i in 0 until 30) {
        val timeLabel = "${(i * 2).toString().padStart(2, '0')}s"
        val jitterIn = (Random.nextFloat() - 0.45f) * (baseIn * 0.2f)
        val jitterOut = (Random.nextFloat() - 0.45f) * (baseOut * 0.2f)
        val jitterThreat = (Random.nextFloat() - 0.4f) * 10f
        val latency = 12f + (Random.nextFloat() * 6f) + (if (threatBase > 50) 20f else 0f)

        list.add(
            RechartsTrafficDataPoint(
                id = i,
                timeLabel = timeLabel,
                inboundMbps = (baseIn + jitterIn).coerceAtLeast(2.0f),
                outboundMbps = (baseOut + jitterOut).coerceAtLeast(1.0f),
                threatScore = (threatBase + jitterThreat).coerceIn(0f, 100f),
                latencyMs = latency,
                activeThreatClass = if (threatBase > 60) scenario.label else null
            )
        )
    }
    return list
}
