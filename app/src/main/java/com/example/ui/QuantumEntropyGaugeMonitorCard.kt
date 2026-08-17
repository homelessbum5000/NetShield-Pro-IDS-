package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class HandshakeCipherMode(
    val title: String,
    val securityLevel: String,
    val baseStrengthPct: Float,
    val minEntropyTarget: Float, // bits per byte
    val color: Color,
    val isQuantumSafe: Boolean,
    val description: String
) {
    ML_KEM_1024(
        title = "ML-KEM-1024 (Kyber)",
        securityLevel = "NIST Level 5 (256-bit PQC)",
        baseStrengthPct = 99.4f,
        minEntropyTarget = 7.999f,
        color = Color(0xFF10B981),
        isQuantumSafe = true,
        description = "Highest NIST post-quantum category. Impervious to Grover's & Shor's algorithm attacks."
    ),
    ML_KEM_768_X25519(
        title = "Hybrid Kyber-768 + X25519",
        securityLevel = "NIST Level 3 Hybrid",
        baseStrengthPct = 94.8f,
        minEntropyTarget = 7.985f,
        color = Color(0xFF38BDF8),
        isQuantumSafe = true,
        description = "Dual-layer hybrid handshake balancing post-quantum security with classical Elliptic Curve Diffie-Hellman."
    ),
    ML_DSA_87(
        title = "ML-DSA-87 (Dilithium-5)",
        securityLevel = "NIST Level 5 Signature",
        baseStrengthPct = 98.2f,
        minEntropyTarget = 7.994f,
        color = Color(0xFFA78BFA),
        isQuantumSafe = true,
        description = "Lattice-based digital signature algorithm providing maximum unforgeable authentication."
    ),
    CLASSIC_RSA_4096(
        title = "Legacy RSA-4096",
        securityLevel = "Vulnerable to CRQC",
        baseStrengthPct = 42.0f,
        minEntropyTarget = 7.210f,
        color = Color(0xFFEF4444),
        isQuantumSafe = false,
        description = "Classical discrete logarithm / prime factorization cipher vulnerable to Cryptanalytically Relevant Quantum Computers."
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuantumEntropyGaugeMonitorCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLiveMonitoring by remember { mutableStateOf(true) }
    var selectedCipher by remember { mutableStateOf(HandshakeCipherMode.ML_KEM_1024) }
    var isEntropyDepletionSimulated by remember { mutableStateOf(false) }
    var isReseeding by remember { mutableStateOf(false) }
    var showTelemetryDetails by remember { mutableStateOf(false) }
    var reseedCounter by remember { mutableIntStateOf(14) }

    // Dynamic Live Values
    var currentStrengthPct by remember { mutableFloatStateOf(selectedCipher.baseStrengthPct) }
    var currentMinEntropy by remember { mutableFloatStateOf(selectedCipher.minEntropyTarget) }
    var currentPoolBits by remember { mutableIntStateOf(4096) }
    var qrngHarvestRateMbps by remember { mutableFloatStateOf(128.4f) }
    var chiSquareUniformityPValue by remember { mutableFloatStateOf(0.9982f) }

    // Live Ticker for Real-Time Entropy Flux
    LaunchedEffect(isLiveMonitoring, selectedCipher, isEntropyDepletionSimulated, isReseeding) {
        while (isLiveMonitoring) {
            delay(1000)
            if (isReseeding) {
                // Re-seeding rapidly spikes entropy back to maximum
                currentStrengthPct = 99.8f
                currentMinEntropy = 7.999f
                currentPoolBits = 4096
                qrngHarvestRateMbps = 240.0f
                chiSquareUniformityPValue = 0.9995f
                delay(800)
                isReseeding = false
            } else if (isEntropyDepletionSimulated) {
                // Entropy drain condition
                val targetStrength = 28.5f + (Random.nextFloat() * 6f)
                val targetEntropy = 5.820f + (Random.nextFloat() * 0.35f)
                currentStrengthPct = (currentStrengthPct * 0.75f + targetStrength * 0.25f).coerceIn(10f, 100f)
                currentMinEntropy = (currentMinEntropy * 0.75f + targetEntropy * 0.25f).coerceIn(4f, 8f)
                currentPoolBits = (currentPoolBits - Random.nextInt(120, 250)).coerceAtLeast(384)
                qrngHarvestRateMbps = 18.2f + (Random.nextFloat() * 5f)
                chiSquareUniformityPValue = 0.042f + (Random.nextFloat() * 0.03f)
            } else {
                // Normal live fluctuating entropy
                val baseStr = selectedCipher.baseStrengthPct
                val baseEnt = selectedCipher.minEntropyTarget
                val jitterStr = (Random.nextFloat() - 0.48f) * 1.6f
                val jitterEnt = (Random.nextFloat() - 0.48f) * 0.005f
                val jitterQrng = (Random.nextFloat() - 0.5f) * 8.0f

                currentStrengthPct = (baseStr + jitterStr).coerceIn(10f, 100f)
                currentMinEntropy = (baseEnt + jitterEnt).coerceIn(4.0f, 8.0f)
                currentPoolBits = (4096 - Random.nextInt(0, 16)).coerceAtLeast(3000)
                qrngHarvestRateMbps = (128f + jitterQrng).coerceAtLeast(50f)
                chiSquareUniformityPValue = (0.9980f + (Random.nextFloat() * 0.0018f)).coerceIn(0f, 1f)
            }
        }
    }

    // Animated strength percentage for smooth needle animation
    val animatedStrength by animateFloatAsState(
        targetValue = currentStrengthPct,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gaugeNeedleAnimation"
    )

    // Animated color transition for the gauge
    val gaugeColor by animateColorAsState(
        targetValue = when {
            animatedStrength >= 85f -> Color(0xFF10B981) // Green
            animatedStrength >= 65f -> Color(0xFF38BDF8) // Blue
            animatedStrength >= 45f -> Color(0xFFF59E0B) // Amber
            else -> Color(0xFFEF4444)                    // Red
        },
        animationSpec = tween(600),
        label = "gaugeColorAnimation"
    )

    // Pulsing indicator for active live stream
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quantum_entropy_gauge_monitor_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090E1A)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (animatedStrength < 50f) Color(0xFFEF4444).copy(alpha = 0.8f) else Color(0xFF10B981).copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Title, Live Status & Stream Toggle
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
                            .background(gaugeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Quantum Entropy Gauge",
                            tint = gaugeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Quantum Entropy & Handshake Monitor",
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
                                        if (isLiveMonitoring) gaugeColor.copy(alpha = pulseGlow)
                                        else Color(0xFF64748B)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLiveMonitoring) "QRNG POOL ACTIVE • 1.0s SAMPLING" else "SAMPLING PAUSED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isLiveMonitoring) gaugeColor else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Pause / Play Ticker
                IconButton(
                    onClick = { isLiveMonitoring = !isLiveMonitoring },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .testTag("toggle_quantum_entropy_stream_button")
                ) {
                    Icon(
                        imageVector = if (isLiveMonitoring) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Entropy Monitor",
                        tint = if (isLiveMonitoring) Color(0xFF38BDF8) else Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E293B))

            // Central Canvas Gauge Visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF040811))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                QuantumEntropyArcGaugeCanvas(
                    strengthPct = animatedStrength,
                    minEntropy = currentMinEntropy,
                    gaugeColor = gaugeColor,
                    isLive = isLiveMonitoring,
                    pulseAlpha = pulseGlow,
                    modifier = Modifier.fillMaxSize()
                )

                // Central Readout Overlay
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 70.dp)
                ) {
                    Text(
                        text = "${animatedStrength.toInt()}%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 32.sp
                        )
                    )
                    Text(
                        text = if (animatedStrength >= 85f) "QUANTUM IMMUNE"
                        else if (animatedStrength >= 65f) "HYBRID SECURE"
                        else if (animatedStrength >= 45f) "CLASSICAL STRONG"
                        else "CRQC VULNERABLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = gaugeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Shannon: ${String.format("%.3f", currentMinEntropy)} / 8.000 bits",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 High-Density Key Entropy KPI Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EntropyMetricKpi(
                    title = "Min-Entropy (H∞)",
                    value = String.format("%.3f", currentMinEntropy),
                    unit = "b/B",
                    accentColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                EntropyMetricKpi(
                    title = "QRNG Harvest",
                    value = String.format("%.1f", qrngHarvestRateMbps),
                    unit = "Mbps",
                    accentColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                EntropyMetricKpi(
                    title = "Pool Reserve",
                    value = "$currentPoolBits",
                    unit = "bits",
                    accentColor = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
                EntropyMetricKpi(
                    title = "Uniformity p",
                    value = String.format("%.4f", chiSquareUniformityPValue),
                    unit = "χ²",
                    accentColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Handshake Cipher Protocol Selector
            Text(
                text = "Active Handshake Cipher Suite:",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HandshakeCipherMode.values().forEach { cipher ->
                    val isSelected = selectedCipher == cipher
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCipher = cipher
                            isEntropyDepletionSimulated = false
                            currentStrengthPct = cipher.baseStrengthPct
                            currentMinEntropy = cipher.minEntropyTarget
                            Toast.makeText(context, "Handshake negotiated with ${cipher.title}", Toast.LENGTH_SHORT).show()
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (cipher.isQuantumSafe) Icons.Default.EnhancedEncryption else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else cipher.color,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cipher.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = cipher.color.copy(alpha = 0.28f),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF131C31),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) cipher.color else Color(0xFF334155),
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cipher Description Box
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCipher.securityLevel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = selectedCipher.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = if (selectedCipher.isQuantumSafe) "NIST FIPS-203 STANDARDIZED" else "LEGACY CRYPTO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (selectedCipher.isQuantumSafe) Color(0xFF34D399) else Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedCipher.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Re-Seed QRNG & Entropy Drain Simulation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isReseeding = true
                        isEntropyDepletionSimulated = false
                        reseedCounter++
                        Toast.makeText(context, "Quantum Optical Noise Injected: Entropy Pool Re-Seeded #$reseedCounter", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isReseeding
                ) {
                    Icon(imageVector = Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isReseeding) "Injecting QRNG..." else "Re-Seed QRNG Noise", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        isEntropyDepletionSimulated = !isEntropyDepletionSimulated
                        val msg = if (isEntropyDepletionSimulated) "Entropy drain attack simulated" else "Entropy drain stopped"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isEntropyDepletionSimulated) Color(0xFFEF4444) else Color(0xFFCBD5E1)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isEntropyDepletionSimulated) Color(0xFFEF4444) else Color(0xFF334155)
                    )
                ) {
                    Icon(
                        imageVector = if (isEntropyDepletionSimulated) Icons.Default.Warning else Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEntropyDepletionSimulated) "Halt Drain" else "Simulate Drain",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = { showTelemetryDetails = !showTelemetryDetails },
                    modifier = Modifier.weight(0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = if (showTelemetryDetails) "Hide" else "Raw",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Raw Telemetry Metadata
            if (showTelemetryDetails) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF030712),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Hardware TRNG & Post-Quantum Key Exchange Telemetry",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Entropy Source: Quantum Photonic Beam Splitter & Ring Oscillator Jitter\n" +
                                    "• NIST SP 800-90B Health Tests: RCT (Repetition Count) & APT (Adaptive Proportion) PASSED\n" +
                                    "• Key Exchange Round-Trip: 0.84ms (ML-KEM Encapsulation + Decapsulation)\n" +
                                    "• Forward Secrecy: Ephemeral KEM Re-Keyed every 60s (Next in 38s)\n" +
                                    "• Re-seed Generation Cycles: $reseedCounter Successful Harvester Cycles",
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
fun QuantumEntropyArcGaugeCanvas(
    strengthPct: Float,
    minEntropy: Float,
    gaugeColor: Color,
    isLive: Boolean,
    pulseAlpha: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h * 0.72f)
        val radius = (w * 0.38f).coerceAtMost(h * 0.65f)

        val startAngle = 145f
        val sweepAngle = 250f

        // Draw Outer Tick Marks & Numbers
        val totalTicks = 25
        for (i in 0..totalTicks) {
            val tickNorm = i.toFloat() / totalTicks
            val angleDeg = startAngle + (sweepAngle * tickNorm)
            val angleRad = (angleDeg * PI / 180.0).toFloat()

            val isMajor = i % 5 == 0
            val tickLength = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
            val tickInnerRadius = radius - 4.dp.toPx()
            val tickOuterRadius = tickInnerRadius + tickLength

            val startPoint = Offset(
                center.x + (radius + 2.dp.toPx()) * cos(angleRad),
                center.y + (radius + 2.dp.toPx()) * sin(angleRad)
            )
            val endPoint = Offset(
                center.x + (radius + 2.dp.toPx() + tickLength) * cos(angleRad),
                center.y + (radius + 2.dp.toPx() + tickLength) * sin(angleRad)
            )

            val tickColor = if (tickNorm <= (strengthPct / 100f)) {
                gaugeColor.copy(alpha = if (isMajor) 0.9f else 0.5f)
            } else {
                Color(0xFF334155).copy(alpha = 0.5f)
            }

            drawLine(
                color = tickColor,
                start = startPoint,
                end = endPoint,
                strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Major Tick Labels (0%, 25%, 50%, 75%, 100%)
            // Tick lines are rendered with primary/accent color
        }

        // Draw Background Gauge Track (Dashed Dark Arc)
        drawArc(
            color = Color(0xFF1E293B).copy(alpha = 0.8f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(
                width = 14.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Draw Multi-Zone Gradient Active Arc
        val currentSweep = sweepAngle * (strengthPct / 100f).coerceIn(0f, 1f)
        if (currentSweep > 0f) {
            val gradientBrush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFFEF4444), // Red zone (0-30%)
                    Color(0xFFF59E0B), // Orange/Amber zone (30-60%)
                    Color(0xFF38BDF8), // Blue zone (60-80%)
                    Color(0xFF10B981), // Emerald Green (80-100%)
                    Color(0xFF34D399)
                ),
                center = center
            )

            drawArc(
                brush = gradientBrush,
                startAngle = startAngle,
                sweepAngle = currentSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(
                    width = 14.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Glowing Leading Tip
            val tipAngleDeg = startAngle + currentSweep
            val tipAngleRad = (tipAngleDeg * PI / 180.0).toFloat()
            val tipX = center.x + radius * cos(tipAngleRad)
            val tipY = center.y + radius * sin(tipAngleRad)

            drawCircle(
                color = gaugeColor.copy(alpha = 0.4f * pulseAlpha),
                radius = 12.dp.toPx(),
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(tipX, tipY)
            )
        }

        // Draw Needle / Pointer
        val needleAngleDeg = startAngle + (sweepAngle * (strengthPct / 100f).coerceIn(0f, 1f))
        val needleAngleRad = (needleAngleDeg * PI / 180.0).toFloat()
        val needleLength = radius * 0.72f
        val needleEnd = Offset(
            center.x + needleLength * cos(needleAngleRad),
            center.y + needleLength * sin(needleAngleRad)
        )

        // Needle Line
        drawLine(
            color = Color.White,
            start = center,
            end = needleEnd,
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Needle Accent Core Line
        drawLine(
            color = gaugeColor,
            start = center,
            end = needleEnd,
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center Needle Pivot Hub
        drawCircle(
            color = Color(0xFF0F172A),
            radius = 12.dp.toPx(),
            center = center
        )
        drawCircle(
            color = gaugeColor,
            radius = 7.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = center
        )
    }
}

@Composable
fun EntropyMetricKpi(
    title: String,
    value: String,
    unit: String,
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
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
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
