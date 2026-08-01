package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PqcAlgorithmInfo(
    val id: String,
    val name: String,
    val nistStandard: String, // e.g. "FIPS 203 (ML-KEM)"
    val category: String, // "Key Exchange (KEM)" or "Digital Signature (DSA)"
    val securityLevel: String, // "NIST Level 1 (AES-128)", "NIST Level 3", "NIST Level 5 (AES-256)"
    val pubKeyBytes: String,
    val cipherTextBytes: String,
    val latencyMs: String,
    val isHybridMode: Boolean = false,
    val description: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuantumCryptoPanelCard(
    isQuantumEncryptionEnabled: Boolean = true,
    onToggleQuantumEncryption: (Boolean) -> Unit = {}
) {
    var isPqcEnabled by remember(isQuantumEncryptionEnabled) { mutableStateOf(isQuantumEncryptionEnabled) }
    var isHybridModeActive by remember { mutableStateOf(true) }

    val kemAlgorithms = remember {
        listOf(
            PqcAlgorithmInfo(
                id = "ML_KEM_768",
                name = "CRYSTALS-Kyber-768 (ML-KEM)",
                nistStandard = "FIPS 203",
                category = "Key Encapsulation (KEM)",
                securityLevel = "NIST Level 3 (AES-192 equivalent)",
                pubKeyBytes = "1,184 Bytes",
                cipherTextBytes = "1,088 Bytes",
                latencyMs = "0.8 ms",
                description = "Primary lattice-based key encapsulation algorithm standardized by NIST for quantum-safe key exchange."
            ),
            PqcAlgorithmInfo(
                id = "ML_KEM_1024",
                name = "CRYSTALS-Kyber-1024 (ML-KEM)",
                nistStandard = "FIPS 203",
                category = "Key Encapsulation (KEM)",
                securityLevel = "NIST Level 5 (AES-256 maximum security)",
                pubKeyBytes = "1,568 Bytes",
                cipherTextBytes = "1,568 Bytes",
                latencyMs = "1.2 ms",
                description = "Maximum security lattice-based KEM variant designed to withstand quantum attacks past 2050."
            ),
            PqcAlgorithmInfo(
                id = "MCELIECE_348864",
                name = "Classic McEliece-348864",
                nistStandard = "NIST Round 4",
                category = "Code-Based KEM",
                securityLevel = "NIST Level 1",
                pubKeyBytes = "261,120 Bytes",
                cipherTextBytes = "128 Bytes",
                latencyMs = "3.4 ms",
                description = "Ultra-conservative code-based cipher with very small ciphertext payloads, suitable for ultra-high security nodes."
            )
        )
    }

    val dsaAlgorithms = remember {
        listOf(
            PqcAlgorithmInfo(
                id = "ML_DSA_65",
                name = "CRYSTALS-Dilithium3 (ML-DSA)",
                nistStandard = "FIPS 204",
                category = "Digital Signature (DSA)",
                securityLevel = "NIST Level 3",
                pubKeyBytes = "1,952 Bytes",
                cipherTextBytes = "3,293 Bytes",
                latencyMs = "1.1 ms",
                description = "Lattice-based digital signature standard providing high efficiency and strong security proofs."
            ),
            PqcAlgorithmInfo(
                id = "FALCON_512",
                name = "Falcon-512 (NTRU-Lattice)",
                nistStandard = "NIST Standardized",
                category = "Digital Signature (DSA)",
                securityLevel = "NIST Level 1",
                pubKeyBytes = "897 Bytes",
                cipherTextBytes = "666 Bytes",
                latencyMs = "0.6 ms",
                description = "NTRU lattice signature with ultra-compact public keys and low memory footprint."
            ),
            PqcAlgorithmInfo(
                id = "SPHINCS_SHA2",
                name = "SLH-DSA (SPHINCS+-SHA2-128f)",
                nistStandard = "FIPS 205",
                category = "Stateless Hash-Based DSA",
                securityLevel = "NIST Level 1",
                pubKeyBytes = "32 Bytes",
                cipherTextBytes = "17,088 Bytes",
                latencyMs = "4.8 ms",
                description = "Stateless hash-based signature mechanism relying solely on SHA-256 collision resistance."
            )
        )
    }

    var selectedKem by remember { mutableStateOf(kemAlgorithms[0]) }
    var selectedDsa by remember { mutableStateOf(dsaAlgorithms[0]) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quantum_crypto_panel_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
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
                        imageVector = Icons.Default.EnhancedEncryption,
                        contentDescription = "Quantum Crypto",
                        tint = Color(0xFFA855F7),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Post-Quantum Cryptography (PQC)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "NIST FIPS 203/204/205 Quantum-Resistant Suite",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                // Active Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPqcEnabled) Color(0xFF7E22CE).copy(alpha = 0.3f) else Color(0xFF475569).copy(alpha = 0.3f))
                        .border(1.dp, if (isPqcEnabled) Color(0xFFA855F7) else Color(0xFF64748B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPqcEnabled) "PQC ACTIVE" else "CLASSICAL ONLY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isPqcEnabled) Color(0xFFE9D5FF) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            // Main PQC Toggle Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Quantum-Resistant Handshakes",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "Protects TLS 1.3 gateway tunnels against future quantum decryption threats",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                    )
                }
                Switch(
                    checked = isPqcEnabled,
                    onCheckedChange = {
                        isPqcEnabled = it
                        onToggleQuantumEncryption(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFA855F7)
                    ),
                    modifier = Modifier.testTag("quantum_encryption_switch")
                )
            }

            if (isPqcEnabled) {
                Spacer(modifier = Modifier.height(10.dp))

                // Hybrid Dual-Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hybrid Classical + Post-Quantum (X25519 + ML-KEM)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Combines Elliptic Curve X25519 with PQC algorithms for fail-safe backwards protection",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                    Switch(
                        checked = isHybridModeActive,
                        onCheckedChange = { isHybridModeActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Key Encapsulation (KEM) Protocol Selector
                Text(
                    text = "1. Key Encapsulation Mechanism (KEM):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    kemAlgorithms.forEach { algo ->
                        val isSelected = selectedKem.id == algo.id

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedKem = algo },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) Color(0xFF2E1065).copy(alpha = 0.5f) else Color(0xFF0F172A)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFFA855F7) else Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedKem = algo },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFA855F7))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = algo.name,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = algo.nistStandard,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFA855F7),
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = algo.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text("PubKey: ${algo.pubKeyBytes}", fontSize = 10.sp, color = Color(0xFFCBD5E1), fontFamily = FontFamily.Monospace)
                                        Text("Ciphertext: ${algo.cipherTextBytes}", fontSize = 10.sp, color = Color(0xFFCBD5E1), fontFamily = FontFamily.Monospace)
                                        Text("Latency: ${algo.latencyMs}", fontSize = 10.sp, color = Color(0xFF34D399), fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Digital Signature (DSA) Protocol Selector
                Text(
                    text = "2. Digital Signature Algorithm (DSA):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dsaAlgorithms.forEach { algo ->
                        val isSelected = selectedDsa.id == algo.id

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDsa = algo },
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) Color(0xFF0C4A6E).copy(alpha = 0.5f) else Color(0xFF0F172A)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedDsa = algo },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF38BDF8))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = algo.name,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = algo.nistStandard,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF38BDF8),
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = algo.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text("Signature Size: ${algo.cipherTextBytes}", fontSize = 10.sp, color = Color(0xFFCBD5E1), fontFamily = FontFamily.Monospace)
                                        Text("Latency: ${algo.latencyMs}", fontSize = 10.sp, color = Color(0xFF34D399), fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selected Active Suite Summary Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE GATEWAY HANDSHAKE SUITE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFA855F7),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        PqcSuiteDetailRow("KEM Exchange", selectedKem.name)
                        PqcSuiteDetailRow("Digital Signature", selectedDsa.name)
                        PqcSuiteDetailRow(
                            "Hybrid Protection",
                            if (isHybridModeActive) "Enabled (X25519 + PQC Dual Layer)" else "PQC Only"
                        )
                        PqcSuiteDetailRow("Estimated Handshake Latency", "${(selectedKem.latencyMs.replace(" ms","").toFloat() + selectedDsa.latencyMs.replace(" ms","").toFloat())} ms")
                    }
                }
            }
        }
    }
}

@Composable
fun PqcSuiteDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        )
    }
}
