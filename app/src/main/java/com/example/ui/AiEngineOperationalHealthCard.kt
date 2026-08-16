package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.FallbackEngineMode

/**
 * Summary Dashboard Component showing the current operational health of both
 * the dual-LLM cloud models and the local heuristic threat detection engine.
 */
@Composable
fun AiEngineOperationalHealthCard(
    healthState: EngineOperationalHealthState,
    fallbackMode: FallbackEngineMode,
    isFallbackEngaged: Boolean,
    totalFallbackAnalysesCount: Int,
    onRunDiagnostic: () -> Unit,
    onSelectFallbackMode: (FallbackEngineMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_health")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_engine_operational_health_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.6f),
                    Color(0xFFA855F7).copy(alpha = 0.6f),
                    Color(0xFF10B981).copy(alpha = 0.6f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Section with Title & Diagnostic Probe Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0284C7), Color(0xFF9333EA))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = "Operational Health",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI & Threat Engine Health",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "Dual-LLM Cloud & Local Heuristic Telemetry",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Button(
                    onClick = onRunDiagnostic,
                    enabled = !healthState.isDiagnosticRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color(0xFF38BDF8)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_run_health_diagnostic")
                ) {
                    if (healthState.isDiagnosticRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color(0xFF38BDF8),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Probing...", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Run Probe",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Probe Latency", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resilience Health Score Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155))
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
                        Column {
                            Text(
                                text = "SYSTEM RESILIENCE INDEX",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${healthState.overallHealthScore}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = if (healthState.overallHealthScore >= 95) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 26.sp
                                    )
                                )
                                Text(
                                    text = " / 100",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    ),
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isFallbackEngaged) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, if (isFallbackEngaged) Color(0xFFF59E0B) else Color(0xFF10B981))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                if (isFallbackEngaged) Color(0xFFFBBF24) else Color(0xFF34D399),
                                                CircleShape
                                            )
                                            .alpha(pulseAlpha)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isFallbackEngaged) "FALLBACK ENGAGED" else "ALL ENGINES OPTIMAL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isFallbackEngaged) Color(0xFFFBBF24) else Color(0xFF6EE7B7),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Last Verified: ${healthState.lastDiagnosticTimestamp}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (healthState.overallHealthScore / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = if (healthState.overallHealthScore >= 95) Color(0xFF10B981) else Color(0xFFF59E0B),
                        trackColor = Color(0xFF0F172A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subsystem Health Grid (4 Subsystems)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // 1. Primary Cloud LLM (Gemini 3.5 Flash)
                SubsystemHealthRow(
                    title = "Primary Cloud LLM (DeepShield)",
                    subtitle = "Gemini 3.5 Flash (us-central1)",
                    status = healthState.primaryCloudLlmStatus,
                    latencyText = if (healthState.primaryCloudLatencyMs > 0) "${healthState.primaryCloudLatencyMs} ms" else "N/A (Offline)",
                    metaDetail = "PQC ML-KEM TLS | 99.98% Uptime",
                    icon = Icons.Default.Cloud,
                    iconTint = Color(0xFF38BDF8)
                )

                // 2. Edge NPU On-Device Classifier
                SubsystemHealthRow(
                    title = "Secondary On-Device (EdgeShield)",
                    subtitle = "Dedicated NPU Tensor Accelerator",
                    status = healthState.edgeNpuStatus,
                    latencyText = "${healthState.edgeNpuLatencyMs} ms",
                    metaDetail = "${healthState.edgeNpuMemoryMb} MB VRAM | 0.8W Low Power",
                    icon = Icons.Default.Memory,
                    iconTint = Color(0xFF10B981)
                )

                // 3. Dual-LLM Consensus Pipeline
                SubsystemHealthRow(
                    title = "Dual-LLM Consensus Pipeline",
                    subtitle = healthState.arbitrationPolicy,
                    status = healthState.dualConsensusStatus,
                    latencyText = "${healthState.consensusAgreementRate}% Sync",
                    metaDetail = "Cross-Model Dual Quorum Gate",
                    icon = Icons.AutoMirrored.Filled.CompareArrows,
                    iconTint = Color(0xFFA855F7)
                )

                // 4. Local Heuristic Fallback Engine
                SubsystemHealthRow(
                    title = "Local Heuristic Engine (Offline)",
                    subtitle = "Shannon Entropy & Protocol Rules",
                    status = healthState.localHeuristicStatus,
                    latencyText = "${healthState.localHeuristicLatencyMs} ms",
                    metaDetail = "${healthState.loadedSignaturesCount} Signatures | $totalFallbackAnalysesCount Triggers",
                    icon = if (isFallbackEngaged) Icons.Default.CloudOff else Icons.Default.Security,
                    iconTint = Color(0xFFF59E0B)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fallback Policy Mode Quick Configuration
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fallback Arbitration Policy",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        )
                        Text(
                            text = when (fallbackMode) {
                                FallbackEngineMode.ACTIVE_AUTO_FALLBACK -> "Auto-Engage on Packet Timeout"
                                FallbackEngineMode.FORCED_OFFLINE_HEURISTICS -> "Forced Local Offline"
                                FallbackEngineMode.DISABLED -> "Fallback Inactive"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (fallbackMode == FallbackEngineMode.DISABLED) Color(0xFFEF4444) else Color(0xFFFBBF24),
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FallbackEngineMode.values().forEach { mode ->
                            val isSelected = fallbackMode == mode
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color(0xFF0F172A),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFFF59E0B) else Color(0xFF334155)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectFallbackMode(mode) }
                                    .testTag("health_fallback_mode_${mode.name}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = when (mode) {
                                            FallbackEngineMode.ACTIVE_AUTO_FALLBACK -> "Auto (Online)"
                                            FallbackEngineMode.FORCED_OFFLINE_HEURISTICS -> "Force Offline"
                                            FallbackEngineMode.DISABLED -> "Disabled"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 9.5.sp
                                        )
                                    )
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
private fun SubsystemHealthRow(
    title: String,
    subtitle: String,
    status: EngineStatus,
    latencyText: String,
    metaDetail: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    val statusColor = when (status) {
        EngineStatus.OPTIMAL -> Color(0xFF10B981)
        EngineStatus.ARMED_STANDBY -> Color(0xFF38BDF8)
        EngineStatus.ACTIVE_FALLBACK -> Color(0xFFF59E0B)
        EngineStatus.DEGRADED -> Color(0xFFF97316)
        EngineStatus.OFFLINE -> Color(0xFFEF4444)
    }

    val statusBadgeText = when (status) {
        EngineStatus.OPTIMAL -> "OPTIMAL"
        EngineStatus.ARMED_STANDBY -> "STANDBY"
        EngineStatus.ACTIVE_FALLBACK -> "ACTIVE FALLBACK"
        EngineStatus.DEGRADED -> "DEGRADED"
        EngineStatus.OFFLINE -> "OFFLINE"
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconTint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = statusColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = statusBadgeText,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    )

                    Text(
                        text = latencyText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFE2E8F0),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = metaDetail,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B),
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}
