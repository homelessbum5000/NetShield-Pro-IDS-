package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
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
fun PerAppFirewallCard(
    appRules: List<AppFirewallRule> = emptyList(),
    onUpdateStatus: (packageName: String, status: AppRuleStatus) -> Unit = { _, _ -> },
    onAddCustomApp: (appName: String, packageName: String, status: AppRuleStatus) -> Unit = { _, _, _ -> },
    onDeleteApp: (packageName: String) -> Unit = {},
    onBatchSet: (denyAllSystem: Boolean) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showAddAppDialog by remember { mutableStateOf(false) }

    val filteredRules = appRules.filter { rule ->
        val matchesSearch = rule.appName.contains(searchQuery, ignoreCase = true) ||
                rule.packageName.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedCategoryFilter) {
            "ALL" -> true
            "SYSTEM" -> rule.isSystemApp
            "USER" -> !rule.isSystemApp
            "DENIED" -> rule.status == AppRuleStatus.DENIED
            "ALLOWED" -> rule.status == AppRuleStatus.ALLOWED
            else -> true
        }
        matchesSearch && matchesCategory
    }

    val totalAllowed = appRules.count { it.status == AppRuleStatus.ALLOWED }
    val totalDenied = appRules.count { it.status == AppRuleStatus.DENIED }
    val totalBlockedAttempts = appRules.sumOf { it.blockedAttemptsToday }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("per_app_firewall_card"),
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
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AppBlocking,
                            contentDescription = "Per-App Firewall",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Per-App Network Rules",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Granular App Internet Permission Controller",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showAddAppDialog = true },
                    modifier = Modifier.testTag("add_app_rule_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA78BFA)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6))
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF1E293B))

            // Metrics Summary
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
                        Text("Allowed Apps", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$totalAllowed", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF10B981), fontWeight = FontWeight.Bold))
                    }
                }

                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Blocked Apps", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$totalDenied", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFEF4444), fontWeight = FontWeight.Bold))
                    }
                }

                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Blocked Packets", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$totalBlockedAttempts", style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar & Quick Filters
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search application name or package...", color = Color(0xFF64748B), fontSize = 12.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", tint = Color(0xFF94A3B8))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFA78BFA),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_app_rules_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips + Batch Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "USER", "SYSTEM", "DENIED", "ALLOWED").forEach { filter ->
                    val isSelected = selectedCategoryFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = filter },
                        label = { Text(filter, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF8B5CF6),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("app_filter_chip_$filter")
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = { onBatchSet(true) },
                    modifier = Modifier.testTag("block_system_telemetry_batch_button")
                ) {
                    Text("Block System Telemetry", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Rules List
            if (filteredRules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching apps found.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredRules.forEach { rule ->
                        AppRuleRowItem(
                            rule = rule,
                            onUpdateStatus = { newStatus -> onUpdateStatus(rule.packageName, newStatus) },
                            onDelete = { onDeleteApp(rule.packageName) }
                        )
                    }
                }
            }
        }
    }

    if (showAddAppDialog) {
        AddAppRuleDialog(
            onDismiss = { showAddAppDialog = false },
            onAdd = { name, pkg, status ->
                onAddCustomApp(name, pkg, status)
                showAddAppDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppRuleRowItem(
    rule: AppFirewallRule,
    onUpdateStatus: (AppRuleStatus) -> Unit,
    onDelete: () -> Unit
) {
    val iconVector = when (rule.iconCategory) {
        "BROWSER" -> Icons.Default.Language
        "BANKING" -> Icons.Default.Lock
        "SOCIAL" -> Icons.Default.Apps
        "GAME" -> Icons.Default.Games
        "VPN" -> Icons.Default.VpnKey
        else -> Icons.Default.Android
    }

    val statusColor = when (rule.status) {
        AppRuleStatus.ALLOWED -> Color(0xFF10B981)
        AppRuleStatus.DENIED -> Color(0xFFEF4444)
        AppRuleStatus.WIFI_ONLY -> Color(0xFF38BDF8)
        AppRuleStatus.CELLULAR_ONLY -> Color(0xFFF59E0B)
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = iconVector, contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(rule.appName, style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            if (rule.isSystemApp) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF334155))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("SYSTEM", fontSize = 9.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(rule.packageName, style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace, fontSize = 10.sp))
                    }
                }

                if (rule.blockedAttemptsToday > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF7F1D1D))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("${rule.blockedAttemptsToday} Blocked", fontSize = 10.sp, color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rule Selector Segmented Control
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AppRuleStatus.values().forEach { status ->
                    val isSelected = rule.status == status
                    val btnColor = when (status) {
                        AppRuleStatus.ALLOWED -> Color(0xFF059669)
                        AppRuleStatus.DENIED -> Color(0xFFDC2626)
                        AppRuleStatus.WIFI_ONLY -> Color(0xFF0284C7)
                        AppRuleStatus.CELLULAR_ONLY -> Color(0xFFD97706)
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdateStatus(status) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (status) {
                                    AppRuleStatus.ALLOWED -> Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp))
                                    AppRuleStatus.DENIED -> Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(12.dp))
                                    AppRuleStatus.WIFI_ONLY -> Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(12.dp))
                                    AppRuleStatus.CELLULAR_ONLY -> Icon(Icons.Default.SignalCellular4Bar, contentDescription = null, modifier = Modifier.size(12.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(status.name.replace("_", " "), fontSize = 10.sp)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = btnColor,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("rule_${rule.packageName}_${status.name}")
                    )
                }
            }
        }
    }
}

@Composable
fun AddAppRuleDialog(
    onDismiss: () -> Unit,
    onAdd: (appName: String, packageName: String, status: AppRuleStatus) -> Unit
) {
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(AppRuleStatus.DENIED) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFFA78BFA),
        unfocusedBorderColor = Color(0xFF334155),
        focusedLabelColor = Color(0xFFA78BFA),
        unfocusedLabelColor = Color(0xFF94A3B8)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AppBlocking, contentDescription = null, tint = Color(0xFFA78BFA))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Custom App Rule", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name (e.g. Signal)") },
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name (e.g. org.thoughtcrime.securesms)") },
                    colors = tfColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Initial Rule Action:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppRuleStatus.values().forEach { st ->
                        FilterChip(
                            selected = selectedStatus == st,
                            onClick = { selectedStatus = st },
                            label = { Text(st.name.replace("_", " "), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF8B5CF6),
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
                    if (packageName.isNotBlank()) {
                        onAdd(appName, packageName, selectedStatus)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
            ) {
                Text("Add Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
