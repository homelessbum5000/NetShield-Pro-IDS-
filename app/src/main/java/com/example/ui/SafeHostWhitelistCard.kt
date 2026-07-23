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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SafeHostEntry(
    val id: String,
    val hostAddress: String, // IP or Domain e.g. "192.168.1.100" or "api.trusted-gateway.io"
    val description: String,
    val category: String, // "Local Subnet", "Trusted API", "VPN Gateway", "DNS Resolver"
    val dateAdded: String,
    var isEnabled: Boolean = true
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SafeHostWhitelistCard() {
    var hostInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Trusted API") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Initial default whitelisted hosts
    val whitelist = remember {
        mutableStateListOf(
            SafeHostEntry(
                id = "HOST-001",
                hostAddress = "192.168.1.0/24",
                description = "Internal Office LAN Subnet",
                category = "Local Subnet",
                dateAdded = "2026-07-20",
                isEnabled = true
            ),
            SafeHostEntry(
                id = "HOST-002",
                hostAddress = "gateway-alpha.netshield.io",
                description = "Quantum Mesh Gateway Endpoint",
                category = "VPN Gateway",
                dateAdded = "2026-07-21",
                isEnabled = true
            ),
            SafeHostEntry(
                id = "HOST-003",
                hostAddress = "1.1.1.1",
                description = "Cloudflare Secure DNS Resolver",
                category = "DNS Resolver",
                dateAdded = "2026-07-22",
                isEnabled = true
            )
        )
    }

    val categories = remember {
        listOf("Local Subnet", "Trusted API", "VPN Gateway", "DNS Resolver", "Dev Server")
    }

    fun validateAndAddHost() {
        val trimmedHost = hostInput.trim()
        if (trimmedHost.isEmpty()) {
            errorMessage = "Please enter a valid IP address, CIDR range, or domain name."
            return
        }

        if (whitelist.any { it.hostAddress.equals(trimmedHost, ignoreCase = true) }) {
            errorMessage = "Host '$trimmedHost' is already in the whitelist."
            return
        }

        val newEntry = SafeHostEntry(
            id = "HOST-${(100..999).random()}",
            hostAddress = trimmedHost,
            description = noteInput.ifBlank { "Custom Whitelisted Host" },
            category = selectedCategory,
            dateAdded = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            isEnabled = true
        )

        whitelist.add(0, newEntry)
        successMessage = "Added '$trimmedHost' to Safe Host Whitelist!"
        errorMessage = null
        hostInput = ""
        noteInput = ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("safe_host_whitelist_card"),
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
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Safe Host Whitelist",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Safe Host Whitelist",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Prevent false-positive intrusion alerts for trusted IPs/domains",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                // Total Rules Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF065F46).copy(alpha = 0.3f))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${whitelist.count { it.isEnabled }} Active Rules",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            // Input Form to Add New Safe Host
            Text(
                text = "Add Trusted IP Address, CIDR, or Domain Name:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = hostInput,
                onValueChange = {
                    hostInput = it
                    errorMessage = null
                    successMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 10.0.0.15 or api.internal-server.net", color = Color(0xFF64748B), fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0F172A)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Optional Note (e.g. Staging Server / Dev Cluster)", color = Color(0xFF64748B), fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0F172A)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF0F172A))
                            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(6.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Button
            Button(
                onClick = { validateAndAddHost() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Whitelist", fontWeight = FontWeight.Bold)
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFEF4444))
                )
            }

            if (successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = successMessage!!,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF34D399), fontFamily = FontFamily.Monospace)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Presets
            Text(
                text = "Quick Presets:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        hostInput = "10.0.0.0/8"
                        noteInput = "Private Enterprise Network CIDR"
                        selectedCategory = "Local Subnet"
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ 10.0.0.0/8 Subnet", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        hostInput = "8.8.8.8"
                        noteInput = "Google Primary Public DNS"
                        selectedCategory = "DNS Resolver"
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ 8.8.8.8 DNS", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of Whitelisted Entries
            Text(
                text = "Whitelisted Safe Hosts (${whitelist.size}):",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                whitelist.forEach { entry ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (entry.isEnabled) Color(0xFF0F172A) else Color(0xFF0F172A).copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (entry.isEnabled) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = if (entry.isEnabled) Color(0xFF10B981) else Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = entry.hostAddress,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = if (entry.isEnabled) Color.White else Color(0xFF94A3B8),
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                    Text(
                                        text = entry.category,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF38BDF8),
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = entry.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Enable Toggle Switch
                            Switch(
                                checked = entry.isEnabled,
                                onCheckedChange = { isChecked ->
                                    val idx = whitelist.indexOf(entry)
                                    if (idx != -1) {
                                        whitelist[idx] = entry.copy(isEnabled = isChecked)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF10B981)
                                )
                            )

                            IconButton(
                                onClick = { whitelist.remove(entry) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Rule",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
