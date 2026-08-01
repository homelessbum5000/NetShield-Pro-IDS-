package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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

@Composable
fun DpiProtocolFilterCard(
    dpiRules: List<DpiRule>,
    onToggleRule: (String, Boolean) -> Unit,
    onAddRule: (String, String, String, String) -> Unit,
    onDeleteRule: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dpi_protocol_filter_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEC4899).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = "DPI Engine",
                            tint = Color(0xFFEC4899),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DPI Payload & SNI Sanitizer",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF831843),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${dpiRules.filter { it.isEnabled }.size} Active",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFFBCFE8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Heuristic payload inspection, TLS SNI masking, and protocol sanitization",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_dpi_rule_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Rule", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Rule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            // Rules List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                dpiRules.forEach { rule ->
                    DpiRuleItem(
                        rule = rule,
                        onToggle = { onToggleRule(rule.id, it) },
                        onDelete = { onDeleteRule(rule.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddDpiRuleDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, proto, action, pattern ->
                onAddRule(name, proto, action, pattern)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DpiRuleItem(
    rule: DpiRule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (rule.isEnabled) Color(0xFF475569) else Color(0xFF334155))
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
                    Surface(
                        color = when (rule.action) {
                            "BLOCK" -> Color(0xFF7F1D1D)
                            "MASK_SNI" -> Color(0xFF065F46)
                            "SANITIZE" -> Color(0xFF075985)
                            else -> Color(0xFF581C87)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = rule.action,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Protocol: ${rule.protocol} • Pattern: ${rule.pattern}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                )
                Text(
                    text = "Filtered Hits: ${rule.hitsCount} packets",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFEC4899), fontSize = 10.sp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFDB2777),
                        uncheckedThumbColor = Color(0xFF94A3B8),
                        uncheckedTrackColor = Color(0xFF334155)
                    )
                )
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Rule", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddDpiRuleDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("TLS/SNI") }
    var action by remember { mutableStateOf("MASK_SNI") }
    var pattern by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom DPI Payload Filter", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Filter Rule Name") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFEC4899), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFFEC4899)),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("Payload / SNI Pattern") },
                    placeholder = { Text("e.g. *.adserver.com, User-Agent:*") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFEC4899), unfocusedBorderColor = Color(0xFF334155), focusedLabelColor = Color(0xFFEC4899)),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Action:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1)))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("MASK_SNI", "SANITIZE", "BLOCK", "INSPECT_PAYLOAD").forEach { act ->
                        FilterChip(
                            selected = action == act,
                            onClick = { action = act },
                            label = { Text(act, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFDB2777), selectedLabelColor = Color.White)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank() && pattern.isNotBlank()) onAdd(name, protocol, action, pattern) },
                enabled = name.isNotBlank() && pattern.isNotBlank()
            ) {
                Text("Add Rule", color = Color(0xFFEC4899))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF0F172A)
    )
}
