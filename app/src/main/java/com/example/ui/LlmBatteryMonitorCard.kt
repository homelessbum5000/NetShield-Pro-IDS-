package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun LlmBatteryMonitorCard() {
    // Battery Impact Threshold setting (% per hour)
    var batteryThreshold by remember { mutableFloatStateOf(8.0f) }

    // Simulation state
    var isBackgroundMonitorActive by remember { mutableStateOf(true) }
    var isEcoModeEnabled by remember { mutableStateOf(false) }

    // Dynamic metrics based on Eco Mode vs High-Performance Dual-LLM Mode
    val currentDrainRate = remember(isEcoModeEnabled, isBackgroundMonitorActive) {
        if (!isBackgroundMonitorActive) 0.5f
        else if (isEcoModeEnabled) 4.2f
        else 11.8f // Exceeds default 8.0f threshold!
    }

    val npuUsagePct = if (!isBackgroundMonitorActive) 2 else if (isEcoModeEnabled) 28 else 84
    val cpuUsagePct = if (!isBackgroundMonitorActive) 5 else if (isEcoModeEnabled) 18 else 52
    val estimatedMahConsumed = if (!isBackgroundMonitorActive) 15 else if (isEcoModeEnabled) 140 else 410

    val isExceedingThreshold = isBackgroundMonitorActive && (currentDrainRate > batteryThreshold)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("llm_battery_monitor_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExceedingThreshold) Color(0xFFEF4444) else Color(0xFF334155)
        )
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
                        imageVector = if (isEcoModeEnabled) Icons.Default.Eco else Icons.Default.BatterySaver,
                        contentDescription = "Battery Monitor",
                        tint = if (isExceedingThreshold) Color(0xFFEF4444) else if (isEcoModeEnabled) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Dual-LLM Power & Battery Monitor",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Background service tracking AI neural threat analysis drain",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                // Service Status Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isBackgroundMonitorActive) "ACTIVE" else "OFF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isBackgroundMonitorActive) Color(0xFF10B981) else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isBackgroundMonitorActive,
                        onCheckedChange = { isBackgroundMonitorActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            // Performance Warning Banner if threshold exceeded
            AnimatedVisibility(
                visible = isExceedingThreshold,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Performance Warning",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚠️ HIGH BATTERY CONSUMPTION WARNING",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Dual-LLM active threat scanning is drawing ${"%.1f".format(currentDrainRate)}%/hr, exceeding your set maximum threshold of ${"%.1f".format(batteryThreshold)}%/hr.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFEE2E2), fontSize = 12.sp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { isEcoModeEnabled = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enable LLM Eco-Mode (Throttle NPU)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Real-Time Drain Meter & Progress Indicator
            Text(
                text = "Real-Time Battery Impact Metrics:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Drain Rate:",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
                )
                Text(
                    text = "${"%.1f".format(currentDrainRate)}% / hour",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = if (isExceedingThreshold) Color(0xFFEF4444) else if (isEcoModeEnabled) Color(0xFF10B981) else Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (currentDrainRate / 20.0f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isExceedingThreshold) Color(0xFFEF4444) else if (isEcoModeEnabled) Color(0xFF10B981) else Color(0xFFF59E0B),
                trackColor = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Resource Allocation Grid (NPU vs CPU vs Energy)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // NPU Usage
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
                            Text("NPU Load", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$npuUsagePct%", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                    }
                }

                // CPU Usage
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CPU Load", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$cpuUsagePct%", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                    }
                }

                // Energy Consumed
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
                            Text("Session", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${estimatedMahConsumed}mAh", style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Threshold Configurator Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Max Battery Drain Threshold:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "${"%.1f".format(batteryThreshold)}% / hr",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            Slider(
                value = batteryThreshold,
                onValueChange = { batteryThreshold = it },
                valueRange = 2.0f..20.0f,
                steps = 17,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF38BDF8),
                    activeTrackColor = Color(0xFF0284C7),
                    inactiveTrackColor = Color(0xFF0F172A)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Eco Mode Throttle Toggle Card
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isEcoModeEnabled) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFF0F172A)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isEcoModeEnabled) Color(0xFF10B981) else Color(0xFF334155)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Adaptive Eco-Mode (Fallback Single LLM)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isEcoModeEnabled) "Active: Throttle secondary LLM & dynamically reduce NPU clock speed" else "Inactive: Running full dual-LLM neural validation",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                    }

                    Switch(
                        checked = isEcoModeEnabled,
                        onCheckedChange = { isEcoModeEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        )
                    )
                }
            }
        }
    }
}
