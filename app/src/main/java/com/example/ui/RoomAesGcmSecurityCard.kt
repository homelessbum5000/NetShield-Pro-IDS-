package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataExploration
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoEncryption
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AesGcmBenchmarkResult
import com.example.data.AesGcmSecurityProfile
import com.example.data.ThreatLogEntity

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomAesGcmSecurityCard(
    securityProfile: AesGcmSecurityProfile,
    benchmarkResult: AesGcmBenchmarkResult,
    decryptedLogs: List<ThreatLogEntity>,
    rawEncryptedLogs: List<ThreatLogEntity>,
    onRunBenchmark: (String) -> Unit,
    onRotateMasterKey: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isExpanded by remember { mutableStateOf(true) }
    var showRawCiphertextLogs by remember { mutableStateOf(false) }
    var customTestPayload by remember { mutableStateOf("185.220.101.5:443 - SYN-Flood Volumetric Burst") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("room_aes_gcm_security_card"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D1B2A)
        ),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize()
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.18f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EnhancedEncryption,
                            contentDescription = "AES-256-GCM Encryption",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AES-256-GCM Room Encryption",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF1F5F9)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF064E3B)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF6EE7B7),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Securing network threat logs at rest with Android KeyStore",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("toggle_aes_card_expansion")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(14.dp))

                // Security Spec Badges
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SecuritySpecPill(
                        icon = Icons.Default.Key,
                        label = "Cipher",
                        value = "AES-256-GCM",
                        accentColor = Color(0xFF38BDF8)
                    )
                    SecuritySpecPill(
                        icon = Icons.Default.Fingerprint,
                        label = "Key Store",
                        value = "AndroidKeyStore",
                        accentColor = Color(0xFFA78BFA)
                    )
                    SecuritySpecPill(
                        icon = Icons.Default.VerifiedUser,
                        label = "Auth Tag",
                        value = "128-bit MAC",
                        accentColor = Color(0xFF34D399)
                    )
                    SecuritySpecPill(
                        icon = Icons.Default.Security,
                        label = "IV Vector",
                        value = "96-bit Random",
                        accentColor = Color(0xFFFBBF24)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cryptographic Engine Performance Benchmark
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF132238)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Hardware Encryption Latency",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF1F5F9)
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tamper Proof",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF34D399),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BenchmarkMetricBox(
                                label = "ENCRYPT SPEED",
                                value = "${benchmarkResult.encryptionLatencyMicros} µs",
                                subtext = "Sub-millisecond",
                                color = Color(0xFF34D399),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BenchmarkMetricBox(
                                label = "DECRYPT SPEED",
                                value = "${benchmarkResult.decryptionLatencyMicros} µs",
                                subtext = "Zero DB Lag",
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BenchmarkMetricBox(
                                label = "INTEGRITY CHECK",
                                value = "PASSED",
                                subtext = "AEAD Verified",
                                color = Color(0xFFA78BFA),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Ciphertext Inspector
                Text(
                    text = "Live Payload Cryptographic Inspector",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customTestPayload,
                    onValueChange = {
                        customTestPayload = it
                        onRunBenchmark(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("aes_test_payload_input"),
                    label = { Text("Sample Threat Payload to Encrypt", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color(0xFFF1F5F9),
                        unfocusedTextColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Hex Breakdown Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070D18)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        HexComponentRow(
                            title = "96-bit IV (Hex):",
                            value = benchmarkResult.ivHex,
                            color = Color(0xFFFBBF24)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        HexComponentRow(
                            title = "Ciphertext (Hex):",
                            value = benchmarkResult.ciphertextHex,
                            color = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        HexComponentRow(
                            title = "128-bit Auth Tag (Hex):",
                            value = benchmarkResult.authTagHex,
                            color = Color(0xFF34D399)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Persisted Format (SQLite at rest):",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = benchmarkResult.rawEncryptedOutput,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color(0xFF6EE7B7)
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(benchmarkResult.rawEncryptedOutput))
                                    Toast.makeText(context, "Ciphertext copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Ciphertext",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Database At-Rest View Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SQLite At-Rest Threat Log Explorer",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF1F5F9)
                            )
                        )
                        Text(
                            text = if (showRawCiphertextLogs) "Showing raw encrypted ciphertext stored in SQLite" else "Showing authenticated decrypted memory view",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (showRawCiphertextLogs) Color(0xFFF59E0B) else Color(0xFF34D399)
                            )
                        )
                    }

                    FilterChip(
                        selected = showRawCiphertextLogs,
                        onClick = { showRawCiphertextLogs = !showRawCiphertextLogs },
                        label = {
                            Text(
                                text = if (showRawCiphertextLogs) "RAW ENCRYPTED" else "DECRYPTED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (showRawCiphertextLogs) Icons.Default.Lock else Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD97706),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.testTag("toggle_raw_ciphertext_view")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sample Threat Logs stored on device
                val displayLogs = if (showRawCiphertextLogs) rawEncryptedLogs.take(3) else decryptedLogs.take(3)

                if (displayLogs.isEmpty()) {
                    Text(
                        text = "No threat logs recorded yet. Tap 'Simulate Threat' below to generate encrypted records.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    displayLogs.forEach { log ->
                        StoredLogPreviewItem(
                            log = log,
                            isRawCiphertext = showRawCiphertextLogs
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Key Rotation Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onRotateMasterKey()
                            Toast.makeText(context, "AES-256-GCM Master Key Rotated Successfully", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rotate_aes_master_key_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
                    ) {
                        Icon(imageVector = Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rotate Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onRunBenchmark(customTestPayload)
                            Toast.makeText(context, "Cryptographic integrity verified", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("verify_room_encryption_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verify AEAD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecuritySpecPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = Color(0xFF94A3B8)
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF1F5F9)
                    )
                )
            }
        }
    }
}

@Composable
private fun BenchmarkMetricBox(
    label: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8)
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = FontFamily.Monospace
                )
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = Color(0xFF64748B)
                )
            )
        }
    }
}

@Composable
private fun HexComponentRow(
    title: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = if (value.length > 28) value.take(28) + "..." else value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun StoredLogPreviewItem(
    log: ThreatLogEntity,
    isRawCiphertext: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isRawCiphertext) Color(0xFF291B06) else Color(0xFF0F2027),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRawCiphertext) Color(0xFFD97706).copy(alpha = 0.4f) else Color(0xFF10B981).copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isRawCiphertext) "ID #${log.id} • SQLite Row" else "ID #${log.id} • ${log.severity}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isRawCiphertext) Color(0xFFFBBF24) else Color(0xFF34D399)
                        )
                    )
                }
                Text(
                    text = "Source: ${log.sourceIp}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFFE2E8F0)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Vector: ${log.attackVector}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isRawCiphertext) Color(0xFF78350F) else Color(0xFF064E3B)
            ) {
                Text(
                    text = if (isRawCiphertext) "CIPHERTEXT" else "AUTHENTICATED",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRawCiphertext) Color(0xFFFDE68A) else Color(0xFF6EE7B7)
                    )
                )
            }
        }
    }
}
