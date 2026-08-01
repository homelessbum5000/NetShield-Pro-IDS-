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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwitchAccessShortcut
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
fun PiHoleIpBlockerCard(
    piHoleRules: List<PiHoleRule> = emptyList(),
    blocklists: List<PiHoleBlocklistSubscription> = emptyList(),
    onAddRule: (target: String, action: PiHoleRuleAction, category: PiHoleRuleCategory) -> Unit = { _, _, _ -> },
    onToggleRule: (id: String, enabled: Boolean) -> Unit = { _, _ -> },
    onDeleteRule: (id: String) -> Unit = {},
    onToggleBlocklist: (id: String, enabled: Boolean) -> Unit = { _, _ -> }
) {
    var selectedActionFilter by remember { mutableStateOf("ALL") }
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var showBlocklistsSection by remember { mutableStateOf(false) }

    val filteredRules = piHoleRules.filter { rule ->
        when (selectedActionFilter) {
            "DENY" -> rule.action == PiHoleRuleAction.DENY
            "ALLOW" -> rule.action == PiHoleRuleAction.ALLOW
            else -> true
        }
    }

    val totalBlockedHits = piHoleRules.filter { it.action == PiHoleRuleAction.DENY }.sumOf { it.hitsCount }
    val activeBlocklistEntries = blocklists.filter { it.isEnabled }.sumOf { it.entryCount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pi_hole_ip_blocker_card"),
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
                            .background(Color(0xFFEC4899).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Pi-hole IP & Domain Sinkhole",
                            tint = Color(0xFFF472B6),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pi-hole IP & Domain Blocker",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "DNS Sinkhole & Blacklist/Whitelist Filter",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showAddRuleDialog = true },
                    modifier = Modifier.testTag("add_pihole_ip_rule_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF472B6)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEC4899))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add IP/Domain", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E293B))

            // Pi-hole Dashboard Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Blocked Queries", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$totalBlockedHits", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFEC4899), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                    }
                }

                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Blocklist Size", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${activeBlocklistEntries / 1000}k Hosts", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                    }
                }

                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Block Percentage", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("23.8%", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Blocklists Toggle Header Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .clickable { showBlocklistsSection = !showBlocklistsSection }
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ListAlt, contentDescription = null, tint = Color(0xFFF472B6), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pi-hole Blocklist Subscriptions (${blocklists.count { it.isEnabled }}/${blocklists.size} Active)", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }
                Text(if (showBlocklistsSection) "Hide" else "Show", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }

            AnimatedVisibility(visible = showBlocklistsSection) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    blocklists.forEach { list ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(list.name, style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                Text("${list.entryCount} entries • ${list.url.take(35)}...", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                            }
                            Switch(
                                checked = list.isEnabled,
                                onCheckedChange = { onToggleBlocklist(list.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFEC4899),
                                    uncheckedThumbColor = Color(0xFF94A3B8),
                                    uncheckedTrackColor = Color(0xFF1E293B)
                                ),
                                modifier = Modifier.testTag("toggle_blocklist_${list.id}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rule Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("IP / Domain Sinkhole Rules:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ALL", "DENY", "ALLOW").forEach { filter ->
                        val isSelected = selectedActionFilter == filter
                        val chipColor = when (filter) {
                            "DENY" -> Color(0xFFEF4444)
                            "ALLOW" -> Color(0xFF10B981)
                            else -> Color(0xFF64748B)
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedActionFilter = filter },
                            label = { Text(filter, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFF94A3B8)
                            ),
                            modifier = Modifier.testTag("pihole_filter_$filter")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rules List
            if (filteredRules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No IP/Domain rules recorded.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredRules.forEach { rule ->
                        PiHoleRuleRowItem(
                            rule = rule,
                            onToggle = { enabled -> onToggleRule(rule.id, enabled) },
                            onDelete = { onDeleteRule(rule.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddRuleDialog) {
        AddPiHoleRuleDialog(
            onDismiss = { showAddRuleDialog = false },
            onAdd = { target, action, category ->
                onAddRule(target, action, category)
                showAddRuleDialog = false
            }
        )
    }
}

@Composable
fun PiHoleRuleRowItem(
    rule: PiHoleRule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val isDeny = rule.action == PiHoleRuleAction.DENY
    val badgeColor = if (isDeny) Color(0xFFEF4444) else Color(0xFF10B981)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(1.dp, badgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isDeny) "DENY (BLOCK)" else "ALLOW (PASS)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = rule.target,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Category: ${rule.category.name.replace("_", " ")} • Hits: ${rule.hitsCount} • Added: ${rule.addedDate}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = badgeColor,
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier.testTag("toggle_rule_${rule.id}")
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddPiHoleRuleDialog(
    onDismiss: () -> Unit,
    onAdd: (target: String, action: PiHoleRuleAction, category: PiHoleRuleCategory) -> Unit
) {
    var target by remember { mutableStateOf("") }
    var action by remember { mutableStateOf(PiHoleRuleAction.DENY) }
    var category by remember { mutableStateOf(PiHoleRuleCategory.MALWARE_C2) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFFF472B6),
        unfocusedBorderColor = Color(0xFF334155),
        focusedLabelColor = Color(0xFFF472B6),
        unfocusedLabelColor = Color(0xFF94A3B8)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Dns, contentDescription = null, tint = Color(0xFFF472B6))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Pi-hole IP / Domain Rule", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("IP Address or Domain (e.g. 45.33.32.156 or ads.google.com)") },
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Rule Action:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = action == PiHoleRuleAction.DENY,
                        onClick = { action = PiHoleRuleAction.DENY },
                        label = { Text("DENY (BLOCK)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFEF4444), selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = action == PiHoleRuleAction.ALLOW,
                        onClick = { action = PiHoleRuleAction.ALLOW },
                        label = { Text("ALLOW (BYPASS)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF10B981), selectedLabelColor = Color.White)
                    )
                }

                Text("Threat Category:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PiHoleRuleCategory.values().forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.name.replace("_", " "), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEC4899),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (target.isNotBlank()) {
                        onAdd(target, action, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
            ) {
                Text("Add Pi-hole Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
