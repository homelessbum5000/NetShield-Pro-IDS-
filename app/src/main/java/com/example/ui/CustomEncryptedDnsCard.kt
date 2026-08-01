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
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Https
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomEncryptedDnsCard(
    dnsState: EncryptedDnsState = EncryptedDnsState(),
    onSetProtocol: (DnsProtocol) -> Unit = {},
    onSetPreset: (presetName: String, primary: String, secondary: String, dohUrl: String, dotHost: String) -> Unit = { _, _, _, _, _ -> },
    onUpdateCustomDns: (primary: String, secondary: String, dohUrl: String, dotHost: String, dnscryptProvider: String) -> Unit = { _, _, _, _, _ -> },
    onToggleLeakProtection: (Boolean) -> Unit = {},
    onToggleDnsSec: (Boolean) -> Unit = {},
    onToggleFallback: (Boolean) -> Unit = {},
    onRunDiagnosticTest: () -> Unit = {}
) {
    var primaryIp by remember(dnsState.primaryDnsIp) { mutableStateOf(dnsState.primaryDnsIp) }
    var secondaryIp by remember(dnsState.secondaryDnsIp) { mutableStateOf(dnsState.secondaryDnsIp) }
    var dohUrl by remember(dnsState.dohEndpointUrl) { mutableStateOf(dnsState.dohEndpointUrl) }
    var dotHost by remember(dnsState.dotHostname) { mutableStateOf(dnsState.dotHostname) }
    var dnscryptProvider by remember(dnsState.dnscryptProviderName) { mutableStateOf(dnsState.dnscryptProviderName) }

    var isEditingCustom by remember { mutableStateOf(false) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFF10B981),
        unfocusedBorderColor = Color(0xFF334155),
        focusedLabelColor = Color(0xFF10B981),
        unfocusedLabelColor = Color(0xFF94A3B8),
        focusedContainerColor = Color(0xFF1E293B),
        unfocusedContainerColor = Color(0xFF1E293B)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_encrypted_dns_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Https,
                            contentDescription = "Encrypted & Custom DNS Engine",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Custom & Encrypted DNS Engine",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "DoH • DoT • DoQ • DNSCrypt v2 • TOR • ODoH",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                OutlinedButton(
                    onClick = onRunDiagnosticTest,
                    modifier = Modifier.testTag("run_dns_test_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF34D399)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test DNS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E293B))

            // Current Active DNS Status Bar
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF059669)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF047857))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(dnsState.activeProtocol.name.replace("_", "-"), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dnsState.selectedPresetName, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${dnsState.currentLatencyMs} ms", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF34D399), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Primary: ${dnsState.primaryDnsIp}  |  Secondary: ${dnsState.secondaryDnsIp}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    )

                    if (dnsState.lastTestResult.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dnsState.lastTestResult,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF38BDF8), fontSize = 10.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Encryption Protocol Selector (DoH, DoT, DoQ, DNSCrypt, TOR, ODoH, Plaintext)
            Text("Select Encrypted DNS Protocol:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DnsProtocol.values().forEach { proto ->
                    val isSelected = dnsState.activeProtocol == proto
                    val label = when (proto) {
                        DnsProtocol.DOH_HTTPS -> "DoH (HTTPS)"
                        DnsProtocol.DOT_TLS -> "DoT (TLS)"
                        DnsProtocol.DOQ_QUIC -> "DoQ (QUIC)"
                        DnsProtocol.DNSCRYPT_V2 -> "DNSCrypt v2"
                        DnsProtocol.DNS_OVER_TOR -> "DNS-over-TOR"
                        DnsProtocol.ODOH_OBLIVIOUS -> "Oblivious DoH"
                        DnsProtocol.PLAIN_UDP_TCP -> "Plaintext (53)"
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onSetProtocol(proto) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("select_dns_proto_${proto.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. DNS Server Presets (Cloudflare, Quad9, AdGuard, NextDNS, Local Pi-hole, Custom)
            Text("DNS Server Presets:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    Triple("Cloudflare", Pair("1.1.1.1", "1.0.0.1"), Pair("https://cloudflare-dns.com/dns-query", "one.one.one.one")),
                    Triple("Quad9 (Malware Block)", Pair("9.9.9.9", "149.112.112.112"), Pair("https://dns.quad9.net/dns-query", "dns.quad9.net")),
                    Triple("AdGuard DNS", Pair("94.140.14.14", "94.140.15.15"), Pair("https://dns.adguard-dns.com/dns-query", "dns.adguard-dns.com")),
                    Triple("NextDNS", Pair("45.90.28.0", "45.90.30.0"), Pair("https://dns.nextdns.io/custom-id", "dns.nextdns.io")),
                    Triple("Local Pi-hole / Unbound", Pair("192.168.1.253", "192.168.1.1"), Pair("https://pihole.local/dns-query", "pihole.local"))
                )

                presets.forEach { (name, ips, endpoints) ->
                    val isSelected = dnsState.selectedPresetName.contains(name.take(6), ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            primaryIp = ips.first
                            secondaryIp = ips.second
                            dohUrl = endpoints.first
                            dotHost = endpoints.second
                            onSetPreset(name, ips.first, ips.second, endpoints.first, endpoints.second)
                        },
                        label = { Text(name, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("preset_dns_${name.replace(" ", "_")}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Custom DNS Server Configuration Input Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Custom DNS Server Addresses:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold))
                TextButton(onClick = { isEditingCustom = !isEditingCustom }) {
                    Text(if (isEditingCustom) "Collapse" else "Edit Custom", fontSize = 11.sp, color = Color(0xFF38BDF8))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = primaryIp,
                        onValueChange = { primaryIp = it },
                        label = { Text("Primary DNS IP") },
                        colors = tfColors,
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_primary_dns_input")
                    )
                    OutlinedTextField(
                        value = secondaryIp,
                        onValueChange = { secondaryIp = it },
                        label = { Text("Secondary DNS IP") },
                        colors = tfColors,
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_secondary_dns_input")
                    )
                }

                AnimatedVisibility(visible = isEditingCustom) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        OutlinedTextField(
                            value = dohUrl,
                            onValueChange = { dohUrl = it },
                            label = { Text("DoH HTTPS Endpoint URL") },
                            colors = tfColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = dotHost,
                            onValueChange = { dotHost = it },
                            label = { Text("DoT TLS Hostname") },
                            colors = tfColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = dnscryptProvider,
                            onValueChange = { dnscryptProvider = it },
                            label = { Text("DNSCrypt Provider Name / Stamp") },
                            colors = tfColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                onUpdateCustomDns(primaryIp, secondaryIp, dohUrl, dotHost, dnscryptProvider)
                                isEditingCustom = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_custom_dns_button")
                        ) {
                            Text("Apply Custom DNS Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. DNS Security Switches
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DNS Leak Protection", style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        Text("Forces all socket lookups through encrypted resolver tunnel.", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                    }
                    Switch(
                        checked = dnsState.isDnsLeakProtectionEnabled,
                        onCheckedChange = onToggleLeakProtection,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981)),
                        modifier = Modifier.testTag("toggle_dns_leak_protection")
                    )
                }

                HorizontalDivider(color = Color(0xFF334155))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DNSSEC Validation Engine", style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        Text("Verifies cryptographic signatures against RRSIG / DS keys.", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                    }
                    Switch(
                        checked = dnsState.isDnsSecValidationEnabled,
                        onCheckedChange = onToggleDnsSec,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981)),
                        modifier = Modifier.testTag("toggle_dnssec_validation")
                    )
                }

                HorizontalDivider(color = Color(0xFF334155))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow Plaintext Fallback on Failure", style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        Text("If encrypted query fails, fallback to unencrypted UDP 53.", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                    }
                    Switch(
                        checked = dnsState.allowFallbackToPlaintext,
                        onCheckedChange = onToggleFallback,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFF59E0B)),
                        modifier = Modifier.testTag("toggle_plaintext_fallback")
                    )
                }
            }
        }
    }
}
