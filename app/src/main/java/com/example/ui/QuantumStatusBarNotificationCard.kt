package com.example.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.QuantumTunnelNotificationService
import com.example.network.QuantumTunnelStatusManager

@Composable
fun QuantumStatusBarNotificationCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tunnelState by QuantumTunnelStatusManager.state.collectAsState()
    var isNotificationActive by remember { mutableStateOf(true) }

    val primaryCyan = Color(0xFF06B6D4)
    val emeraldGreen = Color(0xFF10B981)
    val amberWarning = Color(0xFFF59E0B)
    val cardBackground = Color(0xFF0F172A)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quantum_status_bar_notification_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.5.dp, primaryCyan.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0891B2), Color(0xFF0F172A))
                                )
                            )
                            .border(1.dp, primaryCyan.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnLock,
                            contentDescription = null,
                            tint = primaryCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Persistent Status Bar Quick Controls",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC),
                                    fontSize = 14.sp
                                )
                            )
                        }
                        Text(
                            text = "Live notification with Quick Actions & PQC rotation",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Switch(
                    checked = isNotificationActive,
                    onCheckedChange = { active ->
                        isNotificationActive = active
                        if (active) {
                            QuantumTunnelStatusManager.updateNotification(context)
                            Toast.makeText(context, "Status bar notification enabled", Toast.LENGTH_SHORT).show()
                        } else {
                            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                            manager.cancel(QuantumTunnelStatusManager.NOTIFICATION_ID)
                            Toast.makeText(context, "Status bar notification hidden", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("toggle_status_bar_notification_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFF8FAFC),
                        checkedTrackColor = primaryCyan,
                        uncheckedThumbColor = Color(0xFF64748B),
                        uncheckedTrackColor = Color(0xFF1E293B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notification Preview Mock Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF030712),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (tunnelState.isProtectionEnabled) Icons.Default.Lock else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (tunnelState.isProtectionEnabled) emeraldGreen else amberWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (tunnelState.isProtectionEnabled) "NetShield Pro: Quantum Tunnel Active" else "NetShield Pro: Protection Paused",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (tunnelState.isProtectionEnabled) Color(0xFF064E3B) else Color(0xFF78350F)
                        ) {
                            Text(
                                text = if (tunnelState.isProtectionEnabled) "LIVE NOTIFICATION" else "PAUSED",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (tunnelState.isProtectionEnabled) Color(0xFF6EE7B7) else Color(0xFFFDE68A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (tunnelState.isProtectionEnabled) {
                            "${tunnelState.activeKeyAlgorithm} • Latency ${tunnelState.latencyMs}ms • 0 DNS/IPv6 Leaks"
                        } else {
                            "Tap 'Resume Shield' below or in the status bar to restore quantum tunnel"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons in the notification simulation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                QuantumTunnelStatusManager.toggleProtection(context)
                                val enabled = QuantumTunnelStatusManager.state.value.isProtectionEnabled
                                Toast.makeText(context, if (enabled) "Shield Protection Resumed" else "Shield Protection Paused", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tunnelState.isProtectionEnabled) Color(0xFFDC2626) else emeraldGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("notification_toggle_protection_action")
                        ) {
                            Icon(
                                imageVector = if (tunnelState.isProtectionEnabled) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (tunnelState.isProtectionEnabled) "Pause Shield" else "Resume Shield",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                QuantumTunnelStatusManager.rotateQuantumKeys(context)
                                Toast.makeText(context, "Rotated to: ${QuantumTunnelStatusManager.state.value.activeKeyAlgorithm}", Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("notification_rotate_keys_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFC084FC)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Rotate PQC Keys",
                                color = Color(0xFFC084FC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
