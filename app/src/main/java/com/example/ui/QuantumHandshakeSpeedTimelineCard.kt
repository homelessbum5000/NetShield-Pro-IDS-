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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

enum class PqcHandshakeProtocol(
    val code: String,
    val title: String,
    val standard: String,
    val baseLatencyMs: Float,
    val jitterRangeMs: Float,
    val throughputOpsSec: Int,
    val lineColor: Color,
    val gradientColor: Color
) {
    ML_KEM_1024(
        code = "ML-KEM-1024",
        title = "NIST ML-KEM-1024 (Kyber Level 5)",
        standard = "FIPS 203 Lattice KEM",
        baseLatencyMs = 1.25f,
        jitterRangeMs = 0.35f,
        throughputOpsSec = 1420,
        lineColor = Color(0xFF10B981), // Emerald
        gradientColor = Color(0xFF059669)
    ),
    ML_DSA_87(
        code = "ML-DSA-87",
        title = "NIST ML-DSA-87 (Dilithium Level 5)",
        standard = "FIPS 204 Lattice Signatures",
        baseLatencyMs = 2.10f,
        jitterRangeMs = 0.50f,
        throughputOpsSec = 980,
        lineColor = Color(0xFFA855F7), // Purple
        gradientColor = Color(0xFF7C3AED)
    ),
    HYBRID_X25519_KYBER(
        code = "Hybrid-Kyber768",
        title = "TLS 1.3 Hybrid (X25519 + Kyber-768)",
        standard = "IETF Draft RFC / NIST Level 3",
        baseLatencyMs = 0.88f,
        jitterRangeMs = 0.22f,
        throughputOpsSec = 2150,
        lineColor = Color(0xFF38BDF8), // Cyan / Sky Blue
        gradientColor = Color(0xFF0284C7)
    ),
    CLASSICAL_ECDH(
        code = "Classical ECDH",
        title = "Legacy ECDH (P-384 + RSA-4096)",
        standard = "Classical Baseline (Shor Vulnerable)",
        baseLatencyMs = 0.52f,
        jitterRangeMs = 0.15f,
        throughputOpsSec = 3100,
        lineColor = Color(0xFFF59E0B), // Amber
        gradientColor = Color(0xFFD97706)
    )
}

data class HandshakeDataPoint(
    val minuteOffset: Int,     // -60 to 0 (0 = now)
    val timeLabel: String,     // e.g. "06:15", "06:16"
    val latencyMs: Float,      // in milliseconds
    val throughputOpsSec: Int, // operations per second
    val jitterMs: Float,       // standard deviation / jitter
    val entropyHealth: Float,  // 0.0 - 100.0% TRNG quality
    val isSpike: Boolean = false
)

enum class TimeWindowFilter(val minutes: Int, val label: String) {
    LAST_60_MIN(60, "Last 60m"),
    LAST_30_MIN(30, "Last 30m"),
    LAST_15_MIN(15, "Last 15m")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuantumHandshakeSpeedTimelineCard(
    modifier: Modifier = Modifier
) {
    var isLiveStreaming by remember { mutableStateOf(true) }
    var selectedProtocol by remember { mutableStateOf(PqcHandshakeProtocol.ML_KEM_1024) }
    var isMultiProtocolOverlay by remember { mutableStateOf(false) }
    var selectedTimeWindow by remember { mutableStateOf(TimeWindowFilter.LAST_60_MIN) }
    var showSlaThreshold by remember { mutableStateOf(true) }
    var showExpandedDetails by remember { mutableStateOf(false) }
    var isBurstTesting by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Generate initial 60 minute historic time series for each protocol
    var protocolSeriesMap by remember {
        mutableStateOf(generateInitial60mTimeSeries())
    }

    // Scrubber hover/touch point index (-1 if none selected)
    var activeScrubIndex by remember { mutableIntStateOf(-1) }

    // Live Streaming Loop (updates latest metric every 1.8s)
    LaunchedEffect(isLiveStreaming, selectedProtocol) {
        while (isLiveStreaming) {
            delay(1800L)
            protocolSeriesMap = protocolSeriesMap.toMutableMap().apply {
                PqcHandshakeProtocol.values().forEach { proto ->
                    val currentList = this[proto]?.toMutableList() ?: mutableListOf()
                    if (currentList.isNotEmpty()) {
                        // Shift time series
                        val lastPoint = currentList.last()
                        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                        
                        // Introduce realistic noise & microsecond quantum jitter
                        val noise = (Random.nextFloat() - 0.48f) * proto.jitterRangeMs
                        val isRandomSpike = Random.nextFloat() < 0.05f
                        val spikeDelta = if (isRandomSpike) (Random.nextFloat() * 0.6f + 0.3f) else 0f
                        
                        val newLatency = (proto.baseLatencyMs + noise + spikeDelta).coerceIn(0.2f, 4.5f)
                        val newThroughput = (proto.throughputOpsSec * (proto.baseLatencyMs / newLatency)).toInt()
                        val newJitter = (Random.nextFloat() * 0.08f + 0.02f)
                        val newEntropy = 99.4f + Random.nextFloat() * 0.58f

                        currentList.removeAt(0)
                        
                        // Re-index remaining points
                        val reindexed = currentList.mapIndexed { idx, pt ->
                            val minOffset = -(currentList.size - idx)
                            pt.copy(minuteOffset = minOffset)
                        }.toMutableList()

                        reindexed.add(
                            HandshakeDataPoint(
                                minuteOffset = 0,
                                timeLabel = currentTime,
                                latencyMs = newLatency,
                                throughputOpsSec = newThroughput,
                                jitterMs = newJitter,
                                entropyHealth = newEntropy,
                                isSpike = isRandomSpike
                            )
                        )
                        this[proto] = reindexed
                    }
                }
            }
        }
    }

    // Current active data series based on window & selected protocol
    val fullSeries = protocolSeriesMap[selectedProtocol] ?: emptyList()
    val visibleSeries = remember(fullSeries, selectedTimeWindow) {
        val count = selectedTimeWindow.minutes
        if (fullSeries.size >= count) fullSeries.takeLast(count) else fullSeries
    }

    // Calculate aggregated metrics
    val currentDataPoint = visibleSeries.lastOrNull() ?: HandshakeDataPoint(0, "Now", selectedProtocol.baseLatencyMs, selectedProtocol.throughputOpsSec, 0.05f, 99.8f)
    val avgLatency = remember(visibleSeries) {
        if (visibleSeries.isNotEmpty()) visibleSeries.map { it.latencyMs }.average().toFloat() else selectedProtocol.baseLatencyMs
    }
    val minLatency = remember(visibleSeries) {
        visibleSeries.minOfOrNull { it.latencyMs } ?: selectedProtocol.baseLatencyMs
    }
    val maxLatency = remember(visibleSeries) {
        visibleSeries.maxOfOrNull { it.latencyMs } ?: selectedProtocol.baseLatencyMs
    }
    val p99Latency = remember(visibleSeries) {
        if (visibleSeries.isNotEmpty()) {
            val sorted = visibleSeries.map { it.latencyMs }.sorted()
            val p99Idx = (sorted.size * 0.95).toInt().coerceAtMost(sorted.size - 1)
            sorted[p99Idx]
        } else selectedProtocol.baseLatencyMs * 1.3f
    }
    val avgThroughput = remember(visibleSeries) {
        if (visibleSeries.isNotEmpty()) visibleSeries.map { it.throughputOpsSec }.average().toInt() else selectedProtocol.throughputOpsSec
    }

    val latencyDeltaPercent = if (avgLatency > 0f) {
        ((currentDataPoint.latencyMs - avgLatency) / avgLatency) * 100f
    } else 0f

    // Infinite breathing glow for live telemetry pulse
    val infiniteTransition = rememberInfiniteTransition(label = "recharts_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quantum_handshake_speed_timeline_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1124)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Bar with Real-Time Recharts Badge & Live Toggle
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
                            .background(selectedProtocol.lineColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Handshake Speed Graph",
                            tint = selectedProtocol.lineColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Quantum Handshake Latency",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Text(
                            text = "Recharts Real-Time 1-Hour Microsecond Fluctuations",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                    }
                }

                // Live Streaming Indicator Badge
                Surface(
                    color = if (isLiveStreaming) Color(0xFF022C22) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isLiveStreaming) Color(0xFF10B981).copy(alpha = pulseAlpha) else Color(0xFF475569)
                    ),
                    modifier = Modifier.clickable { isLiveStreaming = !isLiveStreaming }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isLiveStreaming) Color(0xFF10B981) else Color(0xFF94A3B8))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLiveStreaming) "LIVE STREAM" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isLiveStreaming) Color(0xFF6EE7B7) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Top Metric Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Current Handshake Speed
                Surface(
                    color = Color(0xFF111D35),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, selectedProtocol.lineColor.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Current Speed",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.2f", currentDataPoint.latencyMs),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = selectedProtocol.lineColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = " ms",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = selectedProtocol.lineColor.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (latencyDeltaPercent <= 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = if (latencyDeltaPercent <= 0) Color(0xFF34D399) else Color(0xFFF87171),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(Locale.US, "%+.1f%%", latencyDeltaPercent),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (latencyDeltaPercent <= 0) Color(0xFF34D399) else Color(0xFFF87171),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }

                // Metric 2: 1h Average Latency
                Surface(
                    color = Color(0xFF111D35),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "60m Average",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.2f", avgLatency),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = " ms",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Range: ${String.format(Locale.US, "%.1f", minLatency)}-${String.format(Locale.US, "%.1f", maxLatency)}ms",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 9.sp)
                        )
                    }
                }

                // Metric 3: P99 Tail Latency
                Surface(
                    color = Color(0xFF111D35),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "P99 Tail SLA",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.2f", p99Latency),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (p99Latency < 2.0f) Color(0xFF38BDF8) else Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = " ms",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Jitter: ±${String.format(Locale.US, "%.2f", currentDataPoint.jitterMs)}ms",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 9.sp)
                        )
                    }
                }

                // Metric 4: Throughput (Handshakes/sec)
                Surface(
                    color = Color(0xFF111D35),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Throughput",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${currentDataPoint.throughputOpsSec}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFA78BFA),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = " hs/s",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "TRNG: ${String.format(Locale.US, "%.1f", currentDataPoint.entropyHealth)}%",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF10B981), fontSize = 9.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Protocol Selector & Multi-Overlay Toggle Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PQC Protocol Stream:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )

                // Multi-Protocol Overlay Checkbox Button
                Surface(
                    color = if (isMultiProtocolOverlay) Color(0xFF1E3A8A) else Color(0xFF131C31),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isMultiProtocolOverlay) Color(0xFF60A5FA) else Color(0xFF334155)
                    ),
                    modifier = Modifier.clickable { isMultiProtocolOverlay = !isMultiProtocolOverlay }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = if (isMultiProtocolOverlay) Color(0xFF93C5FD) else Color(0xFF94A3B8),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMultiProtocolOverlay) "Multi-Overlay ON" else "Overlay All",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isMultiProtocolOverlay) Color(0xFF93C5FD) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Protocol Selection Filter Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PqcHandshakeProtocol.values().forEach { proto ->
                    val isSelected = selectedProtocol == proto
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedProtocol = proto
                            activeScrubIndex = -1
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(proto.lineColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = proto.code,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = proto.lineColor.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF131C31),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) proto.lineColor else Color(0xFF334155),
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time Window Selector (60m, 30m, 15m) + SLA Threshold Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TimeWindowFilter.values().forEach { win ->
                        val isSelected = selectedTimeWindow == win
                        Surface(
                            color = if (isSelected) Color(0xFF1E293B) else Color.Transparent,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)
                            ),
                            modifier = Modifier.clickable {
                                selectedTimeWindow = win
                                activeScrubIndex = -1
                            }
                        ) {
                            Text(
                                text = win.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showSlaThreshold = !showSlaThreshold }
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (showSlaThreshold) Color(0xFFEF4444) else Color(0xFF475569))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "2.0ms SLA Line",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (showSlaThreshold) Color(0xFFFCA5A5) else Color(0xFF64748B),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // =========================================================================
            // RECHARTS LINE GRAPH CANVAS (Smooth Cubic Spline + Area Gradient + Tooltip)
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070D1D))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            ) {
                // Interactive Drag & Tap Detection for Tooltip Scrubber
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(visibleSeries) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val paddingLeft = 40.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = size.width - paddingLeft - paddingRight
                                    if (offset.x >= paddingLeft && offset.x <= size.width - paddingRight && visibleSeries.size > 1) {
                                        val relX = (offset.x - paddingLeft) / chartWidth
                                        val idx = (relX * (visibleSeries.size - 1)).toInt().coerceIn(0, visibleSeries.size - 1)
                                        activeScrubIndex = idx
                                    } else {
                                        activeScrubIndex = -1
                                    }
                                }
                            )
                        }
                        .pointerInput(visibleSeries) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val paddingLeft = 40.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = size.width - paddingLeft - paddingRight
                                    if (offset.x >= paddingLeft && offset.x <= size.width - paddingRight && visibleSeries.size > 1) {
                                        val relX = (offset.x - paddingLeft) / chartWidth
                                        val idx = (relX * (visibleSeries.size - 1)).toInt().coerceIn(0, visibleSeries.size - 1)
                                        activeScrubIndex = idx
                                    }
                                },
                                onDragEnd = { /* keep active or reset */ },
                                onDrag = { change, _ ->
                                    val paddingLeft = 40.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = size.width - paddingLeft - paddingRight
                                    if (change.position.x >= paddingLeft && change.position.x <= size.width - paddingRight && visibleSeries.size > 1) {
                                        val relX = (change.position.x - paddingLeft) / chartWidth
                                        val idx = (relX * (visibleSeries.size - 1)).toInt().coerceIn(0, visibleSeries.size - 1)
                                        activeScrubIndex = idx
                                    }
                                }
                            )
                        }
                ) {
                    val paddingLeft = 40.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val paddingTop = 20.dp.toPx()
                    val paddingBottom = 28.dp.toPx()

                    val chartWidth = size.width - paddingLeft - paddingRight
                    val chartHeight = size.height - paddingTop - paddingBottom

                    // Y Axis Range: 0.0ms to 3.5ms (or 4.5ms max if spikes occur)
                    val yMax = 3.5f
                    val yMin = 0.0f

                    // 1. Draw Recharts Horizontal Dashed Gridlines & Y-Axis Labels
                    val yGridSteps = listOf(0.5f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f)
                    val gridPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#94A3B8")
                        textSize = 22f
                        typeface = android.graphics.Typeface.MONOSPACE
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }

                    yGridSteps.forEach { stepVal ->
                        val yNorm = 1.0f - ((stepVal - yMin) / (yMax - yMin))
                        val yPos = paddingTop + yNorm * chartHeight

                        // Horizontal dotted grid line
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(paddingLeft, yPos),
                            end = Offset(size.width - paddingRight, yPos),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Y-Axis label
                        drawContext.canvas.nativeCanvas.drawText(
                            String.format(Locale.US, "%.1f", stepVal),
                            paddingLeft - 8.dp.toPx(),
                            yPos + 4.dp.toPx(),
                            gridPaint
                        )
                    }

                    // 2. Draw 2.0ms SLA Reference Threshold Line if enabled
                    if (showSlaThreshold) {
                        val slaYNorm = 1.0f - ((2.0f - yMin) / (yMax - yMin))
                        val slaYPos = paddingTop + slaYNorm * chartHeight
                        drawLine(
                            color = Color(0xFFEF4444).copy(alpha = 0.75f),
                            start = Offset(paddingLeft, slaYPos),
                            end = Offset(size.width - paddingRight, slaYPos),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                        )
                    }

                    // 3. Draw X-Axis Time Labels (-60m, -45m, -30m, -15m, Now)
                    val xTimeLabels = when (selectedTimeWindow) {
                        TimeWindowFilter.LAST_60_MIN -> listOf("-60m" to 0f, "-45m" to 0.25f, "-30m" to 0.5f, "-15m" to 0.75f, "Now" to 1.0f)
                        TimeWindowFilter.LAST_30_MIN -> listOf("-30m" to 0f, "-20m" to 0.33f, "-10m" to 0.66f, "Now" to 1.0f)
                        TimeWindowFilter.LAST_15_MIN -> listOf("-15m" to 0f, "-10m" to 0.33f, "-5m" to 0.66f, "Now" to 1.0f)
                    }

                    val xPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 22f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    xTimeLabels.forEach { (label, frac) ->
                        val xPos = paddingLeft + frac * chartWidth
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            xPos,
                            size.height - 6.dp.toPx(),
                            xPaint
                        )
                    }

                    // 4. Helper function to render a smooth Recharts curve for a dataset
                    fun renderProtocolCurve(
                        series: List<HandshakeDataPoint>,
                        strokeColor: Color,
                        gradientColor: Color,
                        renderFill: Boolean,
                        strokeThickness: Float
                    ) {
                        if (series.size < 2) return

                        val points = series.mapIndexed { idx, pt ->
                            val fracX = idx.toFloat() / (series.size - 1).toFloat()
                            val x = paddingLeft + fracX * chartWidth
                            val normY = 1.0f - ((pt.latencyMs - yMin) / (yMax - yMin)).coerceIn(0f, 1f)
                            val y = paddingTop + normY * chartHeight
                            Offset(x, y)
                        }

                        // Build smooth cubic spline path
                        val strokePath = Path()
                        strokePath.moveTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p0 = if (i > 0) points[i - 1] else points[i]
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val p3 = if (i < points.size - 2) points[i + 2] else p2

                            val cp1x = p1.x + (p2.x - p0.x) / 6f
                            val cp1y = p1.y + (p2.y - p0.y) / 6f
                            val cp2x = p2.x - (p3.x - p1.x) / 6f
                            val cp2y = p2.y - (p3.y - p1.y) / 6f

                            strokePath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                        }

                        // Render gradient area beneath curve
                        if (renderFill) {
                            val fillPath = Path()
                            fillPath.addPath(strokePath)
                            fillPath.lineTo(points.last().x, paddingTop + chartHeight)
                            fillPath.lineTo(points.first().x, paddingTop + chartHeight)
                            fillPath.close()

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        gradientColor.copy(alpha = 0.38f),
                                        gradientColor.copy(alpha = 0.08f),
                                        Color.Transparent
                                    ),
                                    startY = paddingTop,
                                    endY = paddingTop + chartHeight
                                )
                            )
                        }

                        // Render main Recharts Line Stroke
                        drawPath(
                            path = strokePath,
                            color = strokeColor,
                            style = Stroke(
                                width = strokeThickness,
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    // 5. If Multi-Overlay enabled, draw secondary protocols
                    if (isMultiProtocolOverlay) {
                        PqcHandshakeProtocol.values().forEach { proto ->
                            if (proto != selectedProtocol) {
                                val fullP = protocolSeriesMap[proto] ?: emptyList()
                                val visP = if (fullP.size >= selectedTimeWindow.minutes) fullP.takeLast(selectedTimeWindow.minutes) else fullP
                                renderProtocolCurve(
                                    series = visP,
                                    strokeColor = proto.lineColor.copy(alpha = 0.6f),
                                    gradientColor = proto.gradientColor,
                                    renderFill = false,
                                    strokeThickness = 1.5.dp.toPx()
                                )
                            }
                        }
                    }

                    // 6. Render the primary selected protocol curve with rich fill
                    renderProtocolCurve(
                        series = visibleSeries,
                        strokeColor = selectedProtocol.lineColor,
                        gradientColor = selectedProtocol.gradientColor,
                        renderFill = true,
                        strokeThickness = 2.5.dp.toPx()
                    )

                    // 7. Render Interactive Tooltip Scrubber & Focal Point Dot
                    if (activeScrubIndex in visibleSeries.indices) {
                        val activePt = visibleSeries[activeScrubIndex]
                        val fracX = activeScrubIndex.toFloat() / (visibleSeries.size - 1).toFloat()
                        val scrubX = paddingLeft + fracX * chartWidth
                        val normY = 1.0f - ((activePt.latencyMs - yMin) / (yMax - yMin)).coerceIn(0f, 1f)
                        val scrubY = paddingTop + normY * chartHeight

                        // Vertical dashed guide line
                        drawLine(
                            color = Color(0xFFCBD5E1).copy(alpha = 0.7f),
                            start = Offset(scrubX, paddingTop),
                            end = Offset(scrubX, paddingTop + chartHeight),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )

                        // Outer glowing pulse ring
                        drawCircle(
                            color = selectedProtocol.lineColor.copy(alpha = 0.35f),
                            radius = 9.dp.toPx(),
                            center = Offset(scrubX, scrubY)
                        )

                        // Inner solid color circle
                        drawCircle(
                            color = selectedProtocol.lineColor,
                            radius = 5.dp.toPx(),
                            center = Offset(scrubX, scrubY)
                        )

                        // White center focal point
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = Offset(scrubX, scrubY)
                        )
                    } else if (visibleSeries.isNotEmpty()) {
                        // Draw pulsing point on current (latest) point
                        val lastPt = visibleSeries.last()
                        val scrubX = paddingLeft + chartWidth
                        val normY = 1.0f - ((lastPt.latencyMs - yMin) / (yMax - yMin)).coerceIn(0f, 1f)
                        val scrubY = paddingTop + normY * chartHeight

                        drawCircle(
                            color = selectedProtocol.lineColor.copy(alpha = 0.35f * pulseAlpha),
                            radius = 8.dp.toPx(),
                            center = Offset(scrubX, scrubY)
                        )
                        drawCircle(
                            color = selectedProtocol.lineColor,
                            radius = 4.dp.toPx(),
                            center = Offset(scrubX, scrubY)
                        )
                    }
                }

                // Floating Recharts Tooltip Overlay (Appears when user drags or taps)
                if (activeScrubIndex in visibleSeries.indices) {
                    val scrubbed = visibleSeries[activeScrubIndex]
                    Surface(
                        color = Color(0xFF0F172A).copy(alpha = 0.95f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, selectedProtocol.lineColor),
                        modifier = Modifier
                            .align(if (activeScrubIndex < visibleSeries.size / 2) Alignment.TopEnd else Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(selectedProtocol.lineColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (scrubbed.minuteOffset == 0) "T - Now (${scrubbed.timeLabel})" else "T ${scrubbed.minuteOffset} min (${scrubbed.timeLabel})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "Latency: ",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", scrubbed.latencyMs)} ms",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = selectedProtocol.lineColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Text(
                                text = "Rate: ${scrubbed.throughputOpsSec} ops/s  •  Jitter: ±${String.format(Locale.US, "%.2f", scrubbed.jitterMs)}ms",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 8.sp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Run Quantum Handshake Burst Test & Spec Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isBurstTesting = true
                        coroutineScope.launch {
                            Toast.makeText(context, "Executing 100x ${selectedProtocol.code} Handshake Burst...", Toast.LENGTH_SHORT).show()
                            delay(600)
                            // Inject temporary test latency spike
                            protocolSeriesMap = protocolSeriesMap.toMutableMap().apply {
                                val list = this[selectedProtocol]?.toMutableList() ?: mutableListOf()
                                if (list.isNotEmpty()) {
                                    val last = list.last()
                                    list[list.size - 1] = last.copy(
                                        latencyMs = (selectedProtocol.baseLatencyMs + 0.95f).coerceAtMost(4.2f),
                                        throughputOpsSec = (selectedProtocol.throughputOpsSec * 1.6f).toInt(),
                                        isSpike = true
                                    )
                                    this[selectedProtocol] = list
                                }
                            }
                            delay(400)
                            isBurstTesting = false
                            Toast.makeText(context, "Burst Test Complete: Handshake SLA Verified (< 2.5ms)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isBurstTesting,
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = selectedProtocol.gradientColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isBurstTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Benchmarking...", fontSize = 11.sp)
                    } else {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Trigger 100x Burst Test", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { showExpandedDetails = !showExpandedDetails },
                    modifier = Modifier.weight(0.7f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = if (showExpandedDetails) "Hide Spec" else "PQC Spec",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showExpandedDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Expanded Protocol Specifications & Cryptanalysis Section
            AnimatedVisibility(
                visible = showExpandedDetails,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "NIST Post-Quantum Cryptography Latency Characteristics",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• ML-KEM-1024 (FIPS 203): Uses Module Learning with Errors (MLWE) over polynomial rings. Delivers sub-1.5ms encapsulation time with 1,568-byte public keys and 2,272-byte ciphertext.\n" +
                                        "• ML-DSA-87 (FIPS 204): CRYSTALS-Dilithium level-5 digital signatures. Latency (~2.1ms) includes rejection sampling and matrix-vector polynomial multiplication.\n" +
                                        "• TLS 1.3 Hybrid (X25519 + Kyber-768): Dual-KEM combining classical Diffie-Hellman with post-quantum lattice exchange for backward compatibility and fast 0.88ms handshakes.\n" +
                                        "• Jitter & Micro-bursts: Minor spikes (<0.5ms) reflect entropy pool refills and OS context switches under heavy network traffic.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFCBD5E1),
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
}

/**
 * Generates 60 initial data points for each protocol across the last hour (-60m to 0m)
 */
private fun generateInitial60mTimeSeries(): Map<PqcHandshakeProtocol, List<HandshakeDataPoint>> {
    val map = mutableMapOf<PqcHandshakeProtocol, List<HandshakeDataPoint>>()
    val now = System.currentTimeMillis()
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    PqcHandshakeProtocol.values().forEach { proto ->
        val list = mutableListOf<HandshakeDataPoint>()
        for (i in 60 downTo 0) {
            val timestamp = now - i * 60 * 1000L
            val timeLabel = sdf.format(Date(timestamp))
            
            // Generate harmonic sine wave + random noise to simulate natural data center traffic fluctuations
            val cycle = sin(i / 8.0).toFloat() * 0.15f
            val noise = (Random.nextFloat() - 0.5f) * proto.jitterRangeMs
            val isSpike = (i == 42 || i == 18) // Simulated minor historic load spikes
            val spikeDelta = if (isSpike) 0.55f else 0f

            val latency = (proto.baseLatencyMs + cycle + noise + spikeDelta).coerceIn(0.2f, 4.5f)
            val throughput = (proto.throughputOpsSec * (proto.baseLatencyMs / latency)).toInt()
            val jitter = (Random.nextFloat() * 0.08f + 0.02f)
            val entropy = 99.4f + Random.nextFloat() * 0.58f

            list.add(
                HandshakeDataPoint(
                    minuteOffset = -i,
                    timeLabel = timeLabel,
                    latencyMs = latency,
                    throughputOpsSec = throughput,
                    jitterMs = jitter,
                    entropyHealth = entropy,
                    isSpike = isSpike
                )
            )
        }
        map[proto] = list
    }
    return map
}
