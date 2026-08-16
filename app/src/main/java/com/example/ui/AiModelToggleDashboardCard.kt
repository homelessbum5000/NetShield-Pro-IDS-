package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Warning
import com.example.network.FallbackEngineMode
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiModelToggleDashboardCard(
    selectedModel: TrafficAnalysisAiModel,
    onSelectModel: (TrafficAnalysisAiModel) -> Unit,
    inspectionResult: AiModelInspectionResult?,
    isInspecting: Boolean,
    onRunInspection: (target: String?) -> Unit,
    onClearInspection: () -> Unit,
    fallbackMode: FallbackEngineMode = FallbackEngineMode.ACTIVE_AUTO_FALLBACK,
    onSelectFallbackMode: (FallbackEngineMode) -> Unit = {},
    isFallbackEngaged: Boolean = false,
    modifier: Modifier = Modifier
) {
    var customTargetInput by remember { mutableStateOf("") }

    val activePrimaryColor = when (selectedModel) {
        TrafficAnalysisAiModel.ON_DEVICE_NPU -> Color(0xFF10B981) // Green for on-device/offline efficiency
        TrafficAnalysisAiModel.CLOUD_GEMINI_DEEPSHIELD -> Color(0xFF38BDF8) // Cyan/Blue for Cloud Gemini
        TrafficAnalysisAiModel.DUAL_CONSENSUS -> Color(0xFFA855F7) // Purple for dual consensus
    }

    val cardBorderColor = activePrimaryColor.copy(alpha = 0.5f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_model_toggle_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(activePrimaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedModel) {
                                TrafficAnalysisAiModel.ON_DEVICE_NPU -> Icons.Default.Memory
                                TrafficAnalysisAiModel.CLOUD_GEMINI_DEEPSHIELD -> Icons.Default.Cloud
                                TrafficAnalysisAiModel.DUAL_CONSENSUS -> Icons.Default.Psychology
                            },
                            contentDescription = null,
                            tint = activePrimaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Traffic Analysis AI Model Engine",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            )
                        }
                        Text(
                            text = "Toggle active AI model for real-time packet inspection & IDS",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = activePrimaryColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, activePrimaryColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(activePrimaryColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedModel.modelNumber.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = activePrimaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI Model Segmented Toggle Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Model A: On-Device NPU
                    val isModelASelected = selectedModel == TrafficAnalysisAiModel.ON_DEVICE_NPU
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isModelASelected) Color(0xFF10B981).copy(alpha = 0.25f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (isModelASelected) 1.dp else 0.dp,
                                color = if (isModelASelected) Color(0xFF10B981) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectModel(TrafficAnalysisAiModel.ON_DEVICE_NPU) }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                            .testTag("toggle_model_a_npu"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = if (isModelASelected) Color(0xFF10B981) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Model A (NPU)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isModelASelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isModelASelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "4ms • On-Device",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isModelASelected) Color(0xFF6EE7B7) else Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Model B: Cloud Gemini / DeepShield
                    val isModelBSelected = selectedModel == TrafficAnalysisAiModel.CLOUD_GEMINI_DEEPSHIELD
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isModelBSelected) Color(0xFF0284C7).copy(alpha = 0.35f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (isModelBSelected) 1.dp else 0.dp,
                                color = if (isModelBSelected) Color(0xFF38BDF8) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectModel(TrafficAnalysisAiModel.CLOUD_GEMINI_DEEPSHIELD) }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                            .testTag("toggle_model_b_cloud"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = if (isModelBSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Model B (Cloud)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isModelBSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isModelBSelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gemini 3.5 Flash",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isModelBSelected) Color(0xFF7DD3FC) else Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    // Hybrid Dual Consensus Mode
                    val isDualSelected = selectedModel == TrafficAnalysisAiModel.DUAL_CONSENSUS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isDualSelected) Color(0xFFA855F7).copy(alpha = 0.25f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (isDualSelected) 1.dp else 0.dp,
                                color = if (isDualSelected) Color(0xFFA855F7) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectModel(TrafficAnalysisAiModel.DUAL_CONSENSUS) }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                            .testTag("toggle_model_dual_consensus"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                    contentDescription = null,
                                    tint = if (isDualSelected) Color(0xFFA855F7) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Dual Consensus",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isDualSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isDualSelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "99.9% Consensus",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isDualSelected) Color(0xFFE9D5FF) else Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Model Specifications & Benchmark Telemetry Card
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, activePrimaryColor.copy(alpha = 0.35f))
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${selectedModel.displayName} (${selectedModel.modelNumber})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = activePrimaryColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = selectedModel.tag,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = activePrimaryColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedModel.engineType,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = selectedModel.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metrics Row: Latency, Accuracy, Power
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Latency Metric
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("LATENCY", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = if (selectedModel.latencyMs < 10) Color(0xFF10B981) else Color(0xFF38BDF8),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${selectedModel.latencyMs} ms",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Accuracy Metric
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("ACCURACY", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${selectedModel.accuracyPct}%",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // Power / Efficiency Metric
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("POWER PROFILE", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.BatterySaver,
                                        contentDescription = null,
                                        tint = if (selectedModel == TrafficAnalysisAiModel.ON_DEVICE_NPU) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (selectedModel == TrafficAnalysisAiModel.ON_DEVICE_NPU) "Ultra-Low" else "Standard",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Feature Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        selectedModel.supportedFeatures.forEach { feature ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(activePrimaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = feature,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Local Heuristic Threat Detection Engine (Fallback Mechanism)
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("local_heuristic_fallback_card"),
                colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    if (isFallbackEngaged) Color(0xFFF59E0B) else Color(0xFF334155)
                )
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
                                imageVector = if (isFallbackEngaged) Icons.Default.CloudOff else Icons.Default.SyncAlt,
                                contentDescription = null,
                                tint = if (isFallbackEngaged) Color(0xFFF59E0B) else Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Local Heuristic Fallback Engine",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isFallbackEngaged) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, if (isFallbackEngaged) Color(0xFFF59E0B) else Color(0xFF10B981))
                        ) {
                            Text(
                                text = if (isFallbackEngaged) "FALLBACK ACTIVE" else "STANDBY / ARMED",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isFallbackEngaged) Color(0xFFFBBF24) else Color(0xFF6EE7B7),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Deterministic zero-network threat analysis via Shannon entropy algorithms & L4/L7 protocol signatures when cloud dual-LLM connectivity is severed.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fallback Mode Selector Chips
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
                                    .testTag("fallback_mode_${mode.name}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = when (mode) {
                                            FallbackEngineMode.ACTIVE_AUTO_FALLBACK -> "Auto-Fallback"
                                            FallbackEngineMode.FORCED_OFFLINE_HEURISTICS -> "Forced Offline"
                                            FallbackEngineMode.DISABLED -> "Disabled"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Live Inspection Test Bench
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = activePrimaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Traffic Inspection Test Bench",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Text(
                            text = "Engine: ${selectedModel.modelNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = activePrimaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset sample IP chips
                    Text(
                        text = "Quick Select Sample Packets:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontSize = 10.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val sampleTargets = listOf(
                            "185.220.101.5" to "SYN-Flood Threat",
                            "198.51.100.42" to "Quantum Harvest",
                            "45.154.255.88" to "HTTP/2 Rapid Reset",
                            "192.168.1.145" to "Benign LAN"
                        )
                        sampleTargets.forEach { (ip, label) ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (customTargetInput == ip) activePrimaryColor.copy(alpha = 0.2f) else Color(0xFF0F172A),
                                border = BorderStroke(1.dp, if (customTargetInput == ip) activePrimaryColor else Color(0xFF334155)),
                                modifier = Modifier
                                    .clickable { customTargetInput = ip }
                                    .testTag("sample_target_chip_$ip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$ip ($label)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (customTargetInput == ip) Color.White else Color(0xFF94A3B8),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input & Run Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customTargetInput,
                            onValueChange = { customTargetInput = it },
                            placeholder = { Text("Target IP / Stream e.g. 185.220.101.5", fontSize = 11.sp, color = Color(0xFF64748B)) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("custom_target_input"),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedBorderColor = activePrimaryColor,
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                onRunInspection(customTargetInput.ifBlank { null })
                            },
                            enabled = !isInspecting,
                            colors = ButtonDefaults.buttonColors(containerColor = activePrimaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("run_ai_model_inspection_button")
                        ) {
                            if (isInspecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Inspecting...", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            } else {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Inspect", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Loading State indicator
                    if (isInspecting) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = activePrimaryColor,
                                trackColor = Color(0xFF334155)
                            )
                            Text(
                                text = "Running ${selectedModel.displayName} inference on packet stream...",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                            )
                        }
                    }

                    // Inspection Result Card
                    AnimatedVisibility(
                        visible = inspectionResult != null && !isInspecting,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        inspectionResult?.let { res ->
                            Spacer(modifier = Modifier.height(12.dp))

                            val resultCardColor = if (res.isMalicious) Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFF10B981).copy(alpha = 0.12f)
                            val resultBorderColor = if (res.isMalicious) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFF10B981).copy(alpha = 0.4f)
                            val resultBadgeColor = if (res.isMalicious) Color(0xFFEF4444) else Color(0xFF10B981)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = resultCardColor,
                                border = BorderStroke(1.dp, resultBorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (res.isMalicious) Icons.Default.Warning else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = resultBadgeColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = res.analyzedTarget,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 13.sp
                                                )
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (res.isFallbackEngaged) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                                ) {
                                                    Text(
                                                        text = "LOCAL HEURISTIC FALLBACK",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = Color(0xFFFBBF24),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 9.sp
                                                        )
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = resultBadgeColor.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = if (res.isMalicious) "THREAT INTERCEPTED" else "BENIGN PASS",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = resultBadgeColor,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = res.verdictSummary,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Telemetry tags
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⏱ Inferred in ${res.inferenceTimeMs}ms • ${(res.confidenceScore * 100).roundToInt()}% Confidence",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = activePrimaryColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )

                                        IconButton(
                                            onClick = onClearInspection,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("clear_ai_inspection_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear Result",
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (res.shannonEntropyScore != null || res.heuristicRuleName != null) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF1E293B),
                                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Heuristic Signature: ${res.heuristicRuleName ?: "Statistical Threshold"}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFFFBBF24),
                                                        fontSize = 9.sp
                                                    )
                                                )
                                                if (res.shannonEntropyScore != null) {
                                                    Text(
                                                        text = "Entropy: ${"%.2f".format(res.shannonEntropyScore)}/8.0",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = Color(0xFFE2E8F0),
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 9.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF0F172A),
                                        border = BorderStroke(1.dp, Color(0xFF334155)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Kernel Action: ${res.kernelRuleGenerated}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF38BDF8),
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp
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
}
