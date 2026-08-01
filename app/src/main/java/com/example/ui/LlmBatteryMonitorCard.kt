package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LlmBatteryMonitorCard(
    powerSavingState: PowerSavingState = PowerSavingState(),
    onTogglePowerSavingMode: (Boolean) -> Unit = {},
    onToggleAutoBatterySync: (Boolean) -> Unit = {},
    onToggleDualLlmEngine: (Boolean) -> Unit = {},
    onSetBatteryLevel: (Int) -> Unit = {},
    onSetThreshold: (Int) -> Unit = {},
    onSetEncryptionMode: (EncryptionEngineMode) -> Unit = {},
    onSetLlmIntensity: (DualLlmIntensity) -> Unit = {}
) {
    val isPowerSaving = powerSavingState.isPowerSavingEnabled
    val isAutoLowBattery = powerSavingState.isAutoBatterySyncEnabled && (powerSavingState.batteryLevelPct <= powerSavingState.autoSavingsThresholdPct)
    val isEfficiencyMode = powerSavingState.encryptionMode == EncryptionEngineMode.EFFICIENCY

    val statusColor = when {
        isPowerSaving || isAutoLowBattery -> Color(0xFF10B981) // Green for Eco
        powerSavingState.currentDrainRatePercentPerHour > 8.0f -> Color(0xFFEF4444) // Red for High Drain
        else -> Color(0xFFF59E0B) // Amber for Normal Performance
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("llm_battery_monitor_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPowerSaving || isAutoLowBattery) Color(0xFF10B981) else Color(0xFF334155)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPowerSaving || isAutoLowBattery) Icons.Default.Eco else Icons.Default.BatterySaver,
                            contentDescription = "Battery Power Saver",
                            tint = statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Dual-LLM Power & Battery Balancer",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = if (isPowerSaving || isAutoLowBattery) "Power-Saving Mode Active: Throttled NPU & Cipher" else "Performance Mode: High-precision Dual-LLM Analysis",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Master Power Saver Switch
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isPowerSaving) "POWER SAVER" else "PERFORMANCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Switch(
                        checked = isPowerSaving,
                        onCheckedChange = { onTogglePowerSavingMode(it) },
                        modifier = Modifier.testTag("power_saver_toggle_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            // Auto-Trigger Low Battery Banner
            if (isAutoLowBattery) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("auto_battery_saver_banner"),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ Low Battery Auto-Savings Triggered (${powerSavingState.batteryLevelPct}% <= ${powerSavingState.autoSavingsThresholdPct}%)",
                                style = MaterialTheme.typography.titleSmall.copy(color = Color(0xFFA7F3D0), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            )
                            Text(
                                text = "Encryption shifted to 'Efficiency Engine' & Dual-LLM intensity throttled to extend runtime by +${powerSavingState.powerSavedPct}%.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFD1FAE5), fontSize = 11.sp)
                            )
                        }
                    }
                }
            }

            // Simulated Battery Level & Quick Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (powerSavingState.batteryLevelPct <= 20) Icons.Default.BatteryAlert else Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = if (powerSavingState.batteryLevelPct <= 20) Color(0xFFEF4444) else Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Device Battery Level: ${powerSavingState.batteryLevelPct}%",
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "~${"%.1f".format(powerSavingState.estimatedHoursRemaining)} hrs remaining",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Battery Presets Row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(15, 25, 45, 75, 100).forEach { level ->
                    val isSelected = powerSavingState.batteryLevelPct == level
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetBatteryLevel(level) },
                        label = { Text("$level% Battery", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (level <= 20) Color(0xFFDC2626) else Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("battery_preset_$level")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 1: Encryption Engine Mode (Performance vs Efficiency)
            Text(
                text = "Encryption Engine Mode (Power vs Security Balance):",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Performance Mode Card
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSetEncryptionMode(EncryptionEngineMode.PERFORMANCE) }
                        .testTag("encryption_mode_performance_card"),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (!isEfficiencyMode) Color(0xFF1E1B4B) else Color(0xFF0F172A)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (!isEfficiencyMode) Color(0xFF818CF8) else Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Performance", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                            }
                            if (!isEfficiencyMode) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "256-bit AES-GCM + ML-KEM-1024 Dual-Pass",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFC7D2FE), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "100% NPU/GPU vector acceleration for maximum security throughput.",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                    }
                }

                // Efficiency Mode Card
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSetEncryptionMode(EncryptionEngineMode.EFFICIENCY) }
                        .testTag("encryption_mode_efficiency_card"),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isEfficiencyMode) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFF0F172A)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isEfficiencyMode) Color(0xFF10B981) else Color(0xFF334155)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Eco, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Efficiency", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                            }
                            if (isEfficiencyMode) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "128-bit ChaCha20-Poly1305 + ML-KEM-768",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFA7F3D0), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Low-power single-pass cipher. Saves 65% battery power.",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 2: Dual-LLM Neural Analysis Intensity Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dual-LLM Analysis Intensity:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = when (powerSavingState.llmIntensity) {
                        DualLlmIntensity.PERFORMANCE_MAX -> "100% (Parallel Dual)"
                        DualLlmIntensity.BALANCED_75 -> "75% (Sampled Secondary)"
                        DualLlmIntensity.EFFICIENCY_50 -> "50% (Single LLM + Cache)"
                        DualLlmIntensity.ECO_THROTTLED -> "25% (Eco Rule Filter)"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    DualLlmIntensity.PERFORMANCE_MAX to "🚀 Max (100%)",
                    DualLlmIntensity.BALANCED_75 to "⚖️ Balanced (75%)",
                    DualLlmIntensity.EFFICIENCY_50 to "⚡ Efficient (50%)",
                    DualLlmIntensity.ECO_THROTTLED to "🌿 Eco (25%)"
                ).forEach { (intensity, label) ->
                    val isSelected = powerSavingState.llmIntensity == intensity
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetLlmIntensity(intensity) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (intensity == DualLlmIntensity.ECO_THROTTLED || intensity == DualLlmIntensity.EFFICIENCY_50) Color(0xFF059669) else Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("llm_intensity_${intensity.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Power Drain Meter & Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Battery Consumption Rate:",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
                )
                Text(
                    text = "${"%.1f".format(powerSavingState.currentDrainRatePercentPerHour)}% / hr",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (powerSavingState.currentDrainRatePercentPerHour / 15.0f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = statusColor,
                trackColor = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Resource Metrics Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Energy Saved", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${powerSavingState.powerSavedPct}%", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                    }
                }

                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("NPU Clock", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val clockLabel = if (isPowerSaving || isEfficiencyMode) "450 MHz (Low)" else "1.2 GHz (Max)"
                        Text(clockLabel, style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace))
                    }
                }

                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cipher Stream", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val cipherLabel = if (isEfficiencyMode) "ChaCha20" else "AES-256"
                        Text(cipherLabel, style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Persistent Dual-LLM Engine Battery Toggle Row
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("persistent_dual_llm_toggle_card"),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (powerSavingState.isDualLlmEngineEnabled) Color(0xFF0F172A) else Color(0xFF062D4A).copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (powerSavingState.isDualLlmEngineEnabled) Color(0xFF334155) else Color(0xFF0284C7)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (powerSavingState.isDualLlmEngineEnabled) Color(0xFF10B981) else Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dual-LLM AI Threat Engine",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = if (powerSavingState.isDualLlmEngineEnabled) "Active (Full AI Scanning)" else "Disabled (Max Battery Savings: +58%)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (powerSavingState.isDualLlmEngineEnabled) Color(0xFF94A3B8) else Color(0xFF38BDF8),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = powerSavingState.isDualLlmEngineEnabled,
                        onCheckedChange = { onToggleDualLlmEngine(it) },
                        modifier = Modifier.testTag("dual_llm_battery_card_toggle"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                }
            }

            // Auto Battery Sync Threshold Configurator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Auto Power Saver Threshold:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Auto-Sync",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = powerSavingState.isAutoBatterySyncEnabled,
                        onCheckedChange = { onToggleAutoBatterySync(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7)
                        )
                    )
                }
            }

            Text(
                text = "Automatically activates Power Saver Mode when battery drops below ${powerSavingState.autoSavingsThresholdPct}%.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
            )

            Slider(
                value = powerSavingState.autoSavingsThresholdPct.toFloat(),
                onValueChange = { onSetThreshold(it.toInt()) },
                valueRange = 5.0f..50.0f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF38BDF8),
                    activeTrackColor = Color(0xFF0284C7),
                    inactiveTrackColor = Color(0xFF0F172A)
                ),
                modifier = Modifier.testTag("auto_battery_threshold_slider")
            )
        }
    }
}
