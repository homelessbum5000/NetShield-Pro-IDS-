package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SecurityLogEntry(
    val logId: String,
    val timestampIso: String,
    val severity: String, // CRITICAL, HIGH, MEDIUM, LOW
    val attackVector: String,
    val sourceIp: String,
    val targetPort: Int,
    val actionTaken: String,
    val quantumKeyId: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThreatLogCsvExporterCard() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedSeverityFilter by remember { mutableStateOf("ALL") } // ALL, CRITICAL, HIGH, MEDIUM, LOW

    // Generate 24-hour threat logs dataset
    val full24hLogs = remember { generate24hSecurityLogs() }

    val filteredLogs = remember(selectedSeverityFilter, full24hLogs) {
        if (selectedSeverityFilter == "ALL") {
            full24hLogs
        } else {
            full24hLogs.filter { it.severity.equals(selectedSeverityFilter, ignoreCase = true) }
        }
    }

    var exportStatusMessage by remember { mutableStateOf<String?>(null) }

    fun buildCsvString(logs: List<SecurityLogEntry>): String {
        val sb = StringBuilder()
        // CSV Header
        sb.append("Log_ID,Timestamp_ISO,Severity,Attack_Vector,Source_IP,Target_Port,Action_Taken,Quantum_Key_ID\n")
        logs.forEach { log ->
            sb.append("${log.logId},")
                .append("${log.timestampIso},")
                .append("${log.severity},")
                .append("\"${log.attackVector}\",")
                .append("${log.sourceIp},")
                .append("${log.targetPort},")
                .append("\"${log.actionTaken}\",")
                .append("${log.quantumKeyId}\n")
        }
        return sb.toString()
    }

    // StorageAccessFramework CreateDocument launcher for saving CSV to Device Storage
    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val csvData = buildCsvString(filteredLogs)
                context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                    outputStream.write(csvData.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }
                exportStatusMessage = "Successfully exported ${filteredLogs.size} log entries to device storage!"
                Toast.makeText(context, "CSV Export Saved Successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("CSVExport", "Error writing CSV to URI: ${e.message}")
                exportStatusMessage = "Export failed: ${e.localizedMessage}"
            }
        } else {
            exportStatusMessage = "CSV export cancelled by user."
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("threat_log_csv_exporter_card"),
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
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export CSV",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "24-Hour Threat Log CSV Exporter",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Export structured audit logs to device storage",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                // Log Count Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${filteredLogs.size} Logs",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            // Filter Chips
            Text(
                text = "Filter Logs for Export:",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW").forEach { filter ->
                    FilterChip(
                        selected = selectedSeverityFilter == filter,
                        onClick = { selectedSeverityFilter = filter },
                        label = { Text(filter, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (filter) {
                                "CRITICAL" -> Color(0xFFEF4444)
                                "HIGH" -> Color(0xFFF59E0B)
                                "MEDIUM" -> Color(0xFF38BDF8)
                                "LOW" -> Color(0xFF10B981)
                                else -> Color(0xFF0284C7)
                            },
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF0F172A),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Log Preview Table Window (Top 4 Preview Rows)
            Text(
                text = "Log Preview (Showing top 4 of ${filteredLogs.size} entries):",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Timestamp", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 10.sp), modifier = Modifier.weight(1.2f))
                        Text("Vector", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 10.sp), modifier = Modifier.weight(1.5f))
                        Text("Source IP", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 10.sp), modifier = Modifier.weight(1.2f))
                        Text("Action", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 10.sp), modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(color = Color(0xFF1E293B))

                    // Rows
                    filteredLogs.take(4).forEach { log ->
                        val sevColor = when (log.severity) {
                            "CRITICAL" -> Color(0xFFEF4444)
                            "HIGH" -> Color(0xFFF59E0B)
                            "MEDIUM" -> Color(0xFF38BDF8)
                            else -> Color(0xFF10B981)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = log.timestampIso.takeLast(8),
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = log.attackVector,
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                text = log.sourceIp,
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1), fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = log.actionTaken,
                                style = MaterialTheme.typography.labelSmall.copy(color = sevColor, fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Save to Storage SAF vs Copy / Share CSV
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val defaultFilename = "NetShield_Threat_Logs_24h_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
                        createCsvLauncher.launch(defaultFilename)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val csvText = buildCsvString(filteredLogs)
                        clipboardManager.setText(AnnotatedString(csvText))
                        exportStatusMessage = "Copied ${filteredLogs.size} CSV log records to clipboard!"
                        Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy CSV", fontSize = 12.sp)
                }
            }

            if (exportStatusMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = exportStatusMessage!!,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF34D399),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

private fun generate24hSecurityLogs(): List<SecurityLogEntry> {
    val attackVectors = listOf(
        "SYN-Flood Volumetric Burst",
        "Quantum Harvest-Now Decrypt-Later",
        "UDP Amplification Attack",
        "HTTP/2 Rapid Reset Flood",
        "TLS Handshake Protocol Anomaly",
        "Kyber Key Exchange Mismatch",
        "DNS Tunneling Exfiltration",
        "Port Scan Sweep (TCP SYN)"
    )

    val actions = listOf(
        "PACKET_DROPPED",
        "IP_BLACKHOLED",
        "KYBER_REKEYED",
        "RATE_LIMITED",
        "FLAGGED_INSPECT"
    )

    val ips = listOf(
        "185.220.101.5",
        "198.51.100.42",
        "203.0.113.195",
        "45.154.255.88",
        "192.0.2.71",
        "103.251.167.12"
    )

    val severities = listOf("CRITICAL", "HIGH", "MEDIUM", "LOW")

    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val nowMs = System.currentTimeMillis()

    return (0..48).map { index ->
        val timeOffsetMs = (24L * 3600L * 1000L / 48L) * index
        val timestampIso = dateFormat.format(Date(nowMs - timeOffsetMs))

        val sev = when {
            index % 7 == 0 -> "CRITICAL"
            index % 3 == 0 -> "HIGH"
            index % 2 == 0 -> "MEDIUM"
            else -> "LOW"
        }

        SecurityLogEntry(
            logId = "LOG-2026-${1000 + index}",
            timestampIso = timestampIso,
            severity = sev,
            attackVector = attackVectors[index % attackVectors.size],
            sourceIp = ips[index % ips.size],
            targetPort = if (index % 2 == 0) 443 else 8443,
            actionTaken = actions[index % actions.size],
            quantumKeyId = "KYBER1024_0x" + (3000 + index).toString(16).uppercase()
        )
    }
}
