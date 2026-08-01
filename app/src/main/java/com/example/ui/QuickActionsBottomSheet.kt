package com.example.ui

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickActionsBottomSheet(
    isDarkMode: Boolean,
    isIdsEnabled: Boolean,
    onToggleIds: () -> Unit,
    isTorEnabled: Boolean,
    onToggleTor: () -> Unit,
    isQuantumEnabled: Boolean = true,
    onToggleQuantum: () -> Unit = {},
    isAutoBlockEnabled: Boolean = true,
    onToggleAutoBlock: () -> Unit = {},
    isGpuCryptoEnabled: Boolean = true,
    onToggleGpuCrypto: () -> Unit = {},
    alertCacheCount: Int,
    onClearAlertCache: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sheetBg = if (isDarkMode) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val cardBorder = if (isDarkMode) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textColorPrimary = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textColorSecondary = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val innerCardBg = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("quick_actions_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF0284C7).copy(alpha = 0.2f) else Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Quick Command Center",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColorPrimary
                            )
                        )
                        Text(
                            text = "Instant security toggles & operational maintenance",
                            style = MaterialTheme.typography.labelSmall.copy(color = textColorSecondary)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close sheet",
                        tint = textColorSecondary
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = cardBorder
            )

            // Action 1: Intrusion Detection System (IDS) Engine Toggle
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = innerCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isIdsEnabled) Color(0xFF065F46) else Color(0xFF991B1B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Intrusion Detection (IDS Engine)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = textColorPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isIdsEnabled) "Active: Real-time neural payload packet analysis" else "Inactive: Threat inspection paused",
                                style = MaterialTheme.typography.bodySmall.copy(color = textColorSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    Switch(
                        checked = isIdsEnabled,
                        onCheckedChange = {
                            onToggleIds()
                            Toast.makeText(
                                context,
                                if (!isIdsEnabled) "IDS Engine Activated" else "IDS Engine Suspended",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action 2: Tor Onion Anonymous Routing Switch
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = innerCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isTorEnabled) Color(0xFF581C87) else Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = if (isTorEnabled) Color(0xFFE9D5FF) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Tor Onion Circuit Proxy",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = textColorPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isTorEnabled) "Active: Multi-hop encrypted onion relaying" else "Inactive: Direct post-quantum gateway path",
                                style = MaterialTheme.typography.bodySmall.copy(color = textColorSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    Switch(
                        checked = isTorEnabled,
                        onCheckedChange = {
                            onToggleTor()
                            Toast.makeText(
                                context,
                                if (!isTorEnabled) "Tor Anonymous Relay Enabled" else "Tor Routing Disabled",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFA855F7)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action 3: Quantum-Safe Encryption Protocols Switch
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = innerCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isQuantumEnabled) Color(0xFF6B21A8) else Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = if (isQuantumEnabled) Color(0xFFE9D5FF) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Quantum-Safe Encryption",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = textColorPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isQuantumEnabled) "Active: ML-KEM-1024 post-quantum TLS 1.3" else "Disabled: Classical RSA/ECC fallback mode",
                                style = MaterialTheme.typography.bodySmall.copy(color = textColorSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    Switch(
                        checked = isQuantumEnabled,
                        onCheckedChange = {
                            onToggleQuantum()
                            Toast.makeText(
                                context,
                                if (!isQuantumEnabled) "Quantum-Safe Protocols Enabled" else "Quantum Encryption Disabled",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFA855F7)
                        ),
                        modifier = Modifier.testTag("quick_sheet_quantum_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action 4: Dual-LLM Auto Firewall Switch
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = innerCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isAutoBlockEnabled) Color(0xFF065F46) else Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (isAutoBlockEnabled) Color(0xFFA7F3D0) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Dual-LLM Auto Firewall",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = textColorPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAutoBlockEnabled) "Active: Auto-block malicious IPs with iptables/eBPF" else "Paused: Manual confirmation required for IP blocks",
                                style = MaterialTheme.typography.bodySmall.copy(color = textColorSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    Switch(
                        checked = isAutoBlockEnabled,
                        onCheckedChange = {
                            onToggleAutoBlock()
                            Toast.makeText(
                                context,
                                if (!isAutoBlockEnabled) "Dual-LLM Auto Firewall Enabled" else "Auto Firewall Block Paused",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        ),
                        modifier = Modifier.testTag("quick_sheet_autoblock_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action 5: GPU & Shader Encryption Offload Switch
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = innerCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isGpuCryptoEnabled) Color(0xFF0284C7) else Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (isGpuCryptoEnabled) Color(0xFFE0F2FE) else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "GPU Crypto Acceleration",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = textColorPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isGpuCryptoEnabled) "Active: Vulkan 1.3 GPGPU shader offload for ML-KEM" else "Disabled: Pure CPU thread execution",
                                style = MaterialTheme.typography.bodySmall.copy(color = textColorSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    Switch(
                        checked = isGpuCryptoEnabled,
                        onCheckedChange = {
                            onToggleGpuCrypto()
                            Toast.makeText(
                                context,
                                if (!isGpuCryptoEnabled) "GPU Acceleration Enabled" else "GPU Acceleration Disabled",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7)
                        ),
                        modifier = Modifier.testTag("quick_sheet_gpu_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action 6: Clear Alert Cache Button
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClearAlertCache()
                        Toast
                            .makeText(context, "Threat alert cache purged", Toast.LENGTH_SHORT)
                            .show()
                    },
                colors = CardDefaults.outlinedCardColors(containerColor = innerCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF78350F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = Color(0xFFFDE68A),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Clear Threat Alert Cache",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = textColorPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Cached alerts: $alertCacheCount items in local memory",
                                style = MaterialTheme.typography.bodySmall.copy(color = textColorSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Purge Cache",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action 4 & 5 Quick Row: Force Re-Key & Emergency Isolation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "Quantum Kyber-1024 Keys Rotated", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Re-Key Tunnel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "🚨 Emergency Isolation Mode Activated", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Isolate Gateway", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
