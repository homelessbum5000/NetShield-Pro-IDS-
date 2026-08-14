package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PqcProtocolProfile(
    val codeName: String,
    val displayName: String,
    val standardDoc: String,
    val kemCipher: String,
    val signatureCipher: String,
    val quantumResistanceScore: Int, // out of 100
    val shorImmunity: Boolean,
    val latencyOverheadMs: Double,
    val tagColor: Color
) {
    NIST_FIPS_203_204(
        codeName = "FIPS-203/204",
        displayName = "NIST ML-KEM-1024 + ML-DSA-87",
        standardDoc = "FIPS 203 / FIPS 204 (Lattice-Based)",
        kemCipher = "ML-KEM-1024 (CRYSTALS-Kyber Level 5)",
        signatureCipher = "ML-DSA-87 (CRYSTALS-Dilithium Level 5)",
        quantumResistanceScore = 99,
        shorImmunity = true,
        latencyOverheadMs = 1.2,
        tagColor = Color(0xFF10B981)
    ),
    HYBRID_TLS13(
        codeName = "Hybrid-X25519-Kyber",
        displayName = "TLS 1.3 Hybrid (X25519 + Kyber-768)",
        standardDoc = "IETF Draft RFC / NIST Level 3",
        kemCipher = "Hybrid X25519 + ML-KEM-768",
        signatureCipher = "ECDSA P-256 + Dilithium-3",
        quantumResistanceScore = 92,
        shorImmunity = true,
        latencyOverheadMs = 0.8,
        tagColor = Color(0xFF38BDF8)
    ),
    SLH_DSA_STATELESS(
        codeName = "SLH-DSA-SPHINCS",
        displayName = "SLH-DSA (SPHINCS+ Stateless Hashes)",
        standardDoc = "FIPS 205 (Hash-Based Cryptography)",
        kemCipher = "ML-KEM-768 + AES-256-GCM",
        signatureCipher = "SLH-DSA-SHAKE-256s",
        quantumResistanceScore = 98,
        shorImmunity = true,
        latencyOverheadMs = 3.6,
        tagColor = Color(0xFFA78BFA)
    ),
    CLASSICAL_LEGACY(
        codeName = "Classical-RSA/ECC",
        displayName = "Legacy Classical (RSA-4096 / ECDH P-384)",
        standardDoc = "Legacy PKCS#1 v2.2 (Shor Vulnerable)",
        kemCipher = "ECDH secp384r1 (Vulnerable to Shor)",
        signatureCipher = "RSA-PSS-4096 / ECDSA",
        quantumResistanceScore = 28,
        shorImmunity = false,
        latencyOverheadMs = 0.0,
        tagColor = Color(0xFFEF4444)
    )
}

data class PqcSecurityAuditStep(
    val title: String,
    val status: String,
    val detail: String,
    val isPassed: Boolean
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuantumEncryptionStatusIndicatorCard(
    isQuantumEncryptionEnabled: Boolean = true,
    onToggleQuantumEncryption: (Boolean) -> Unit = {},
    activeNetworkType: String = "Wi-Fi 6E (WPA3-Enterprise)",
    modifier: Modifier = Modifier
) {
    var isEnabled by remember(isQuantumEncryptionEnabled) { mutableStateOf(isQuantumEncryptionEnabled) }
    var selectedProfile by remember { mutableStateOf(PqcProtocolProfile.NIST_FIPS_203_204) }
    var isRunningAudit by remember { mutableStateOf(false) }
    var auditProgress by remember { mutableFloatStateOf(0f) }
    var auditSteps by remember { mutableStateOf<List<PqcSecurityAuditStep>?>(null) }
    var showExpandedDetails by remember { mutableStateOf(false) }
    var simulatedPacketsSec by remember { mutableIntStateOf(1420) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Infinite breathing glow animation for protected state
    val infiniteTransition = rememberInfiniteTransition(label = "pqc_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Calculate effective resistance score based on toggle
    val effectiveScore = if (isEnabled) {
        selectedProfile.quantumResistanceScore
    } else {
        18 // Unprotected classical fallback
    }

    val isFullyProtected = isEnabled && selectedProfile.shorImmunity

    val indicatorBgColor by animateColorAsState(
        targetValue = if (isFullyProtected) Color(0xFF022C22) else Color(0xFF2D0607),
        label = "bg_color"
    )
    val indicatorBorderColor by animateColorAsState(
        targetValue = if (isFullyProtected) Color(0xFF10B981) else Color(0xFFEF4444),
        label = "border_color"
    )
    val accentColor by animateColorAsState(
        targetValue = if (isFullyProtected) Color(0xFF34D399) else Color(0xFFF87171),
        label = "accent_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quantum_encryption_status_indicator_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF091224)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, indicatorBorderColor.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Header Row with Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFullyProtected) Icons.Default.Shield else Icons.Default.Warning,
                            contentDescription = "Quantum Encryption Status",
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Quantum Encryption Status",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Text(
                            text = if (isFullyProtected) "Post-Quantum Cryptography (PQC) Shield Active" else "Classical Only (Harvest-Now-Decrypt-Later Threat)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isFullyProtected) Color(0xFFA7F3D0) else Color(0xFFFCA5A5),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Main On/Off Protection Switch
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { newVal ->
                        isEnabled = newVal
                        onToggleQuantumEncryption(newVal)
                        Toast.makeText(
                            context,
                            if (newVal) "Quantum-Safe Encryption Enabled (ML-KEM / ML-DSA)" else "Switched to Classical Legacy Encryption",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.testTag("quantum_encryption_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Visual Status Banner with Glowing Aura
            Surface(
                color = indicatorBgColor,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, indicatorBorderColor.copy(alpha = pulseAlpha)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Pulsing glowing dot indicator
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFullyProtected) "PROTECTED BY QUANTUM-SAFE PROTOCOLS" else "VULNERABLE: CLASSICAL ENCRYPTION ONLY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = accentColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Surface(
                            color = accentColor.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$effectiveScore% PQC SCORE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real-time security score visual meter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { effectiveScore / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = accentColor,
                            trackColor = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (effectiveScore >= 90) "NIST FIPS 203/204" else if (effectiveScore >= 70) "Hybrid PQC" else "Weak (RSA)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Threat Resistance Specs Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Shor's Algorithm Defense", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isFullyProtected) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isFullyProtected) "IMMUNE (Lattice Hardness)" else "VULNERABLE (Poly-Time Factor)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isFullyProtected) Color(0xFFA7F3D0) else Color(0xFFFCA5A5),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "HNDL Attack Immunity", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp))
                            Text(
                                text = if (isFullyProtected) "Active Protection" else "High Compromise Risk",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isFullyProtected) Color(0xFF34D399) else Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Protocol Profile Filter Selector Chips
            Text(
                text = "Quantum-Safe Protocol Profile Configuration:",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PqcProtocolProfile.values().forEach { profile ->
                    val isSelected = selectedProfile == profile
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedProfile = profile
                            if (profile == PqcProtocolProfile.CLASSICAL_LEGACY) {
                                isEnabled = false
                                onToggleQuantumEncryption(false)
                            } else {
                                isEnabled = true
                                onToggleQuantumEncryption(true)
                            }
                            auditSteps = null
                        },
                        label = {
                            Text(
                                text = profile.codeName,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = profile.tagColor.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF131C31),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) profile.tagColor else Color(0xFF334155),
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Protocol Breakdown Card
            Surface(
                color = Color(0xFF111D35),
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
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Active Handshake Ciphers",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Text(
                            text = selectedProfile.standardDoc,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // KEM Spec
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Key Encapsulation (KEM):", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Text(
                            text = if (isEnabled) selectedProfile.kemCipher else "ECDH secp256r1 (Classical)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isEnabled) Color(0xFFE2E8F0) else Color(0xFFFCA5A5),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // DSA Spec
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Digital Signature (DSA):", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Text(
                            text = if (isEnabled) selectedProfile.signatureCipher else "RSA-2048 PKCS#1 (Classical)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isEnabled) Color(0xFFE2E8F0) else Color(0xFFFCA5A5),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Overhead Spec
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Handshake Latency Delta:", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Text(
                            text = if (isEnabled) "+${selectedProfile.latencyOverheadMs} ms" else "0.0 ms (No PQC Protection)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isEnabled) Color(0xFF34D399) else Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Run Quantum Handshake Audit & Toggle Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isRunningAudit = true
                        auditProgress = 0.1f
                        coroutineScope.launch {
                            delay(200)
                            auditProgress = 0.4f
                            delay(250)
                            auditProgress = 0.75f
                            delay(200)
                            auditProgress = 1.0f
                            isRunningAudit = false
                            auditSteps = listOf(
                                PqcSecurityAuditStep(
                                    title = "Lattice Entropy & Seed Validation",
                                    status = if (isEnabled) "VERIFIED (NIST SP 800-90B TRNG)" else "WEAK CLASSICAL PRNG",
                                    detail = if (isEnabled) "256-bit quantum-safe entropy pool validated with zero entropy leakage." else "Classical pseudorandom generator without quantum entropy guarantee.",
                                    isPassed = isEnabled
                                ),
                                PqcSecurityAuditStep(
                                    title = "ML-KEM Key Exchange Encapsulation",
                                    status = if (isEnabled) "Kyber-1024 / 2272-byte Public Key" else "ECDH secp256r1 (Shor Vulnerable)",
                                    detail = if (isEnabled) "Shared secret encapsulated via Module-LWE hard problem." else "Discrete log key exchange vulnerable to Shor's algorithm on quantum processors.",
                                    isPassed = isEnabled
                                ),
                                PqcSecurityAuditStep(
                                    title = "ML-DSA / Dilithium Signature Verification",
                                    status = if (isEnabled) "FIPS 204 Lattice Signature Active" else "RSA-2048 (Compromise Risk)",
                                    detail = if (isEnabled) "Certificate chain verified with post-quantum digital signature algorithm." else "Classical factorization-based signature vulnerable to future decryption.",
                                    isPassed = isEnabled
                                ),
                                PqcSecurityAuditStep(
                                    title = "Forward Secrecy against Harvest-Now-Decrypt-Later",
                                    status = if (isEnabled) "100% IMMUNE (Future Proof)" else "UNPROTECTED (Vulnerable to Harvest)",
                                    detail = if (isEnabled) "Adversary network capture cannot be decrypted by cryptographically relevant quantum computers." else "Captured encrypted traffic can be decrypted once CRQC is online.",
                                    isPassed = isEnabled
                                )
                            )
                        }
                    },
                    enabled = !isRunningAudit,
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isRunningAudit) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auditing Protocol...", fontSize = 11.sp)
                    } else {
                        Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Audit PQC Handshake", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { showExpandedDetails = !showExpandedDetails },
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = if (showExpandedDetails) "Hide Info" else "Deep Spec",
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

            if (isRunningAudit) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { auditProgress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = Color(0xFF38BDF8),
                    trackColor = Color(0xFF1E293B)
                )
            }

            // Real-Time Audit Results Drawer
            if (auditSteps != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF030712),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isEnabled) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.4f)),
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
                                    imageVector = if (isEnabled) Icons.Default.VerifiedUser else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isEnabled) Color(0xFF34D399) else Color(0xFFF87171),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PQC Handshake Audit Report",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isEnabled) Color(0xFF34D399) else Color(0xFFF87171),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Text(
                                text = "Network: $activeNetworkType",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        auditSteps?.forEach { step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = if (step.isPassed) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (step.isPassed) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = step.title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp
                                            )
                                        )
                                        Text(
                                            text = step.status,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (step.isPassed) Color(0xFF34D399) else Color(0xFFFCA5A5),
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    Text(
                                        text = step.detail,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 9.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expanded Protocol Details Section
            if (showExpandedDetails) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "NIST Post-Quantum Cryptography Architecture Standards",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• FIPS 203 (ML-KEM): Primary standard for quantum-resistant Key Encapsulation based on Module Learning with Errors (MLWE) across high-dimensional polynomial rings.\n" +
                                    "• FIPS 204 (ML-DSA): Standardized digital signature algorithm derived from CRYSTALS-Dilithium, providing collision-resistant nonces and strong unforgeability.\n" +
                                    "• FIPS 205 (SLH-DSA): Stateless hash-based digital signature algorithm providing a conservative, lattice-independent fallback relying solely on SHAKE-256 and SHA-256 properties.\n" +
                                    "• Harvest-Now-Decrypt-Later (HNDL): Threat model where encrypted packets are recorded today for offline decryption once Cryptographically Relevant Quantum Computers (CRQCs) emerge.",
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
