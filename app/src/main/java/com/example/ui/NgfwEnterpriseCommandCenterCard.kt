package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class NgfwTab(val title: String, val icon: ImageVector) {
    CORE_ID("Core ID™", Icons.Default.Fingerprint),
    SECURITY_SERVICES("Threat Prevention", Icons.Default.Security),
    SINGLE_PASS("Single-Pass & HA", Icons.Default.Layers),
    ACC_DASHBOARD("ACC & AIOps", Icons.Default.Analytics),
    EMERGING_TECH("PQC, ZeroTrust & 5G", Icons.Default.Psychology)
}

// Data models for NGFW framework
data class AppIdSignature(
    val name: String,
    val category: String,
    val riskLevel: Int, // 1 to 5
    val standardPort: String,
    val behavioralPattern: String,
    var isBlocked: Boolean = false,
    val bandwidthMb: Float,
    val sessionsCount: Int
)

data class UserIdMapping(
    val username: String,
    val directoryGroup: String,
    val sourceIp: String,
    val deviceType: String,
    val activeZone: String,
    val securityPosture: String, // Compliant, At-Risk, Quarantined
    val riskScore: Int
)

data class WildFireSample(
    val fileHash: String,
    val fileName: String,
    val fileType: String,
    val detonationStatus: String, // Analyzing, Benign, Malicious Zero-Day, Grayware
    val cveRef: String,
    val timestamp: String,
    val sandboxConfidence: Float
)

data class AccAppMetric(
    val appName: String,
    val category: String,
    val bandwidthGb: Float,
    val threatCount: Int,
    val riskWeight: Float,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NgfwEnterpriseCommandCenterCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCardExpanded by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(NgfwTab.CORE_ID) }

    // Global NGFW State
    var isSinglePassEngineActive by remember { mutableStateOf(true) }
    var isWildFireCloudSyncActive by remember { mutableStateOf(true) }
    var isDnsSecurityActive by remember { mutableStateOf(true) }
    var isSslDecryptionActive by remember { mutableStateOf(true) }
    var isZeroTrustEnforced by remember { mutableStateOf(true) }
    var isPqcVpnActive by remember { mutableStateOf(true) }
    var is5gSliceProtected by remember { mutableStateOf(true) }

    // Telemetry
    var totalPacketsInspected by remember { mutableLongStateOf(8492040L) }
    var zeroDayThreatsNeutralized by remember { mutableIntStateOf(142) }
    var activeGlobalProtectUsers by remember { mutableIntStateOf(328) }
    var singlePassLatencyUs by remember { mutableFloatStateOf(4.2f) }
    var aiPostureHealthScore by remember { mutableFloatStateOf(98.6f) }
    var highAvailabilityState by remember { mutableStateOf("ACTIVE-PRIMARY (HA1/HA2 Synced - 0.4ms)") }

    var deploymentOption by remember { mutableStateOf("Hardware Appliance (PA-5450 DPU)") }

    // App-ID™ Datastore
    val appSignatures = remember {
        mutableStateListOf(
            AppIdSignature("BitTorrent-P2P", "File-Sharing / High Risk", 5, "TCP/UDP Dynamic", "BitTorrent DHT / uTP Protocol Signature", true, 412.5f, 84),
            AppIdSignature("Tor-Encrypted-Relay", "Anonymizer / Evasion", 5, "TCP 9001/443", "TLS Multi-hop Onion Handshake", true, 94.2f, 18),
            AppIdSignature("GitHub-Enterprise", "Collaboration / Code", 1, "TCP 443", "HTTPS Git Smart Transfer + REST API", false, 1280.4f, 512),
            AppIdSignature("Zoom-Video-Meeting", "Unified Communications", 2, "UDP 8801-8810", "RTP Voice/Video Stream & SIP Control", false, 3420.0f, 890),
            AppIdSignature("SSH-Remote-Terminal", "Infrastructure Access", 3, "TCP 22", "SSHv2 Banner & Key Exchange", false, 180.2f, 45),
            AppIdSignature("Monero-CryptoMiner", "Malicious Activity", 5, "Stratum TCP 3333", "JSON-RPC PoW Hash Verification", true, 45.1f, 12)
        )
    }

    // User-ID™ & Device-ID Datastore
    val userMappings = remember {
        mutableStateListOf(
            UserIdMapping("alex.miller@corp.local", "DevOps-Admins", "10.240.12.84", "Corporate MacBook Pro", "Trust-Internal", "Compliant", 12),
            UserIdMapping("sarah.chen@corp.local", "SecOps-Leadership", "10.240.12.102", "Secured Linux Workstation", "DMZ-Admin", "Compliant", 4),
            UserIdMapping("guest_temp_982", "Contractor-Guests", "192.168.100.45", "Unknown BYOD Tablet", "Guest-Untrust", "At-Risk (No HIP)", 68),
            UserIdMapping("service_acct_cicd", "Automated-Services", "10.100.4.15", "Kubernetes Runner Pod", "Cloud-Workload", "Compliant", 8)
        )
    }

    // Advanced WildFire® Detonation Log
    val wildFireSamples = remember {
        mutableStateListOf(
            WildFireSample("7e29a...f881", "invoice_oct2026.pdf.exe", "PE32 Executable", "Malicious Zero-Day", "CVE-2026-9921", "2 min ago", 99.8f),
            WildFireSample("4b11c...a302", "firmware_update.bin", "Encrypted Blob", "Benign (Signed OEM)", "None", "14 min ago", 100.0f),
            WildFireSample("9f88d...110e", "powershell_stager.ps1", "PowerShell Script", "Malicious Ransomware", "CVE-2024-3400", "32 min ago", 98.4f),
            WildFireSample("1a2b3...c4d5", "dataset_analytics.parquet", "Data Container", "Benign Clean", "None", "1 hour ago", 99.9f)
        )
    }

    // Application Command Center (ACC) Data
    val accTopApps = remember {
        listOf(
            AccAppMetric("Zoom Meetings", "VoIP/Video", 42.8f, 0, 0.1f, Color(0xFF38BDF8)),
            AccAppMetric("GitHub API", "Development", 28.4f, 1, 0.2f, Color(0xFF10B981)),
            AccAppMetric("AWS S3 Gateway", "Cloud Storage", 19.6f, 0, 0.15f, Color(0xFFF59E0B)),
            AccAppMetric("BitTorrent DHT", "P2P (Blocked)", 8.2f, 48, 0.95f, Color(0xFFEF4444)),
            AccAppMetric("Custom C2 Domain", "Malicious C2 (Sinkholed)", 0.8f, 94, 1.0f, Color(0xFFEC4899))
        )
    }

    // Live packet engine ticker
    LaunchedEffect(isSinglePassEngineActive) {
        while (true) {
            delay(3000)
            if (isSinglePassEngineActive) {
                totalPacketsInspected += (12400L..48200L).random()
                singlePassLatencyUs = 3.8f + (0..12).random() * 0.1f
            }
        }
    }

    val primaryColor = Color(0xFF0284C7)
    val cardBackground = Color(0xFF0B132B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ngfw_enterprise_command_center_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.5f))
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
                    .clickable { isCardExpanded = !isCardExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0284C7), Color(0xFF0F172A))
                                )
                            )
                            .border(1.dp, primaryColor.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "NGFW Logo",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Next-Generation Firewall (NGFW)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF064E3B)
                            ) {
                                Text(
                                    text = "ENTERPRISE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF6EE7B7),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = "App-ID™ • User-ID™ • Content-ID • Single-Pass SP3 • WildFire • ZTNA",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { isCardExpanded = !isCardExpanded },
                    modifier = Modifier.testTag("toggle_ngfw_card_expansion")
                ) {
                    Icon(
                        imageVector = if (isCardExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isCardExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            if (isCardExpanded) {
                Spacer(modifier = Modifier.height(14.dp))

                // High-Level Telemetry Quick Stats Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NgfwMetricTile(
                        label = "Single-Pass Latency",
                        value = "${"%.1f".format(singlePassLatencyUs)} µs",
                        subtext = "Hardware Offload",
                        color = Color(0xFF38BDF8),
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f)
                    )
                    NgfwMetricTile(
                        label = "Zero-Day Blocks",
                        value = "$zeroDayThreatsNeutralized",
                        subtext = "WildFire Cloud",
                        color = Color(0xFFF59E0B),
                        icon = Icons.Default.VerifiedUser,
                        modifier = Modifier.weight(1f)
                    )
                    NgfwMetricTile(
                        label = "AIOps Health",
                        value = "${"%.1f".format(aiPostureHealthScore)}%",
                        subtext = "Optimal Posture",
                        color = Color(0xFF10B981),
                        icon = Icons.Default.Psychology,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color(0xFF38BDF8),
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = Color(0xFF38BDF8),
                            height = 3.dp
                        )
                    },
                    divider = { HorizontalDivider(color = Color(0xFF1E293B)) }
                ) {
                    NgfwTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (selectedTab == tab) Color(0xFF38BDF8) else Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = if (selectedTab == tab) Color(0xFFF8FAFC) else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    NgfwTab.CORE_ID -> {
                        CoreIdentificationView(
                            appSignatures = appSignatures,
                            userMappings = userMappings,
                            onToggleAppBlock = { index ->
                                appSignatures[index] = appSignatures[index].copy(
                                    isBlocked = !appSignatures[index].isBlocked
                                )
                                val isBlockedNow = appSignatures[index].isBlocked
                                Toast.makeText(
                                    context,
                                    "App-ID™ Rule: ${appSignatures[index].name} is now ${if (isBlockedNow) "BLOCKED" else "ALLOWED"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }

                    NgfwTab.SECURITY_SERVICES -> {
                        ThreatPreventionServicesView(
                            wildFireSamples = wildFireSamples,
                            isWildFireActive = isWildFireCloudSyncActive,
                            onToggleWildFire = { isWildFireCloudSyncActive = it },
                            isDnsSecurityActive = isDnsSecurityActive,
                            onToggleDnsSecurity = { isDnsSecurityActive = it },
                            isSslDecryptionActive = isSslDecryptionActive,
                            onToggleSslDecryption = { isSslDecryptionActive = it },
                            onTriggerDetonation = {
                                val newSample = WildFireSample(
                                    fileHash = "d41d8...e835",
                                    fileName = "zero_day_stager_${System.currentTimeMillis() % 1000}.bin",
                                    fileType = "Polymorphic ELF",
                                    detonationStatus = "Malicious Zero-Day",
                                    cveRef = "CVE-2026-${(1000..9999).random()}",
                                    timestamp = "Just now",
                                    sandboxConfidence = 99.7f
                                )
                                wildFireSamples.add(0, newSample)
                                zeroDayThreatsNeutralized++
                                Toast.makeText(context, "Advanced WildFire detonated & neutralized zero-day sample", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    NgfwTab.SINGLE_PASS -> {
                        SinglePassArchitectureView(
                            isSinglePassActive = isSinglePassEngineActive,
                            onToggleSinglePass = { isSinglePassEngineActive = it },
                            latencyUs = singlePassLatencyUs,
                            totalPackets = totalPacketsInspected,
                            deploymentOption = deploymentOption,
                            onSelectDeployment = { deploymentOption = it },
                            haState = highAvailabilityState,
                            onTriggerFailover = {
                                highAvailabilityState = if (highAvailabilityState.startsWith("ACTIVE-PRIMARY")) {
                                    "ACTIVE-SECONDARY (Peer Promoted in 0.2ms - Zero Session Loss)"
                                } else {
                                    "ACTIVE-PRIMARY (HA1/HA2 Synced - 0.4ms)"
                                }
                                Toast.makeText(context, "High-Availability cluster failover executed smoothly", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    NgfwTab.ACC_DASHBOARD -> {
                        AccAndAiOpsView(
                            topApps = accTopApps,
                            aiHealthScore = aiPostureHealthScore,
                            activeVpnUsers = activeGlobalProtectUsers,
                            onOptimizePosture = {
                                aiPostureHealthScore = 99.8f
                                Toast.makeText(context, "AIOps automated 14 policy simplifications & closed unused pinholes", Toast.LENGTH_LONG).show()
                            }
                        )
                    }

                    NgfwTab.EMERGING_TECH -> {
                        EmergingCapabilitiesView(
                            isZeroTrustEnforced = isZeroTrustEnforced,
                            onToggleZeroTrust = { isZeroTrustEnforced = it },
                            isPqcVpnActive = isPqcVpnActive,
                            onTogglePqcVpn = { isPqcVpnActive = it },
                            is5gSliceProtected = is5gSliceProtected,
                            onToggle5gSlice = { is5gSliceProtected = it },
                            onTriggerQuantumRekey = {
                                Toast.makeText(context, "GlobalProtect Post-Quantum VPN: Re-keyed with FIPS-203 ML-KEM-1024", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-View 1: Core Identification Technologies
// -------------------------------------------------------------
@Composable
fun CoreIdentificationView(
    appSignatures: List<AppIdSignature>,
    userMappings: List<UserIdMapping>,
    onToggleAppBlock: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeaderWithBadge(
            title = "App-ID™ Signature Engine",
            badge = "PATENTED BEHAVIORAL CLASSIFIER",
            badgeColor = Color(0xFF0284C7)
        )
        Text(
            text = "Classifies applications based on deep packet behavior regardless of evasion, standard port, or protocol.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        appSignatures.forEachIndexed { index, app ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, if (app.isBlocked) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (app.isBlocked) Color(0xFFFCA5A5) else Color(0xFFF8FAFC)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            RiskBadge(risk = app.riskLevel)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${app.category} • Standard: ${app.standardPort} • ${app.behavioralPattern}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = "Bandwidth: ${app.bandwidthMb} MB • Active Sessions: ${app.sessionsCount}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp
                            )
                        )
                    }

                    Button(
                        onClick = { onToggleAppBlock(index) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (app.isBlocked) Color(0xFFDC2626) else Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("toggle_app_${app.name}")
                    ) {
                        Text(
                            text = if (app.isBlocked) "BLOCKED" else "ALLOW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(16.dp))

        // User-ID & Device-ID Section
        SectionHeaderWithBadge(
            title = "User-ID™ & Device-ID Profiling",
            badge = "LDAP / AD / OKTA SYNC",
            badgeColor = Color(0xFF10B981)
        )
        Text(
            text = "Enforces least-privilege security policies bound directly to authenticated users and profiled IoT/endpoint devices.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        userMappings.forEach { user ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.username,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF1F5F9)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (user.securityPosture == "Compliant") Color(0xFF064E3B) else Color(0xFF78350F)
                            ) {
                                Text(
                                    text = user.securityPosture,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (user.securityPosture == "Compliant") Color(0xFF6EE7B7) else Color(0xFFFDE68A),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Group: ${user.directoryGroup} • IP: ${user.sourceIp} • Zone: ${user.activeZone}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontSize = 10.sp)
                        )
                        Text(
                            text = "Device-ID Profile: ${user.deviceType} (Risk Score: ${user.riskScore}/100)",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-View 2: Threat Prevention & Advanced Security Services
// -------------------------------------------------------------
@Composable
fun ThreatPreventionServicesView(
    wildFireSamples: List<WildFireSample>,
    isWildFireActive: Boolean,
    onToggleWildFire: (Boolean) -> Unit,
    isDnsSecurityActive: Boolean,
    onToggleDnsSecurity: (Boolean) -> Unit,
    isSslDecryptionActive: Boolean,
    onToggleSslDecryption: (Boolean) -> Unit,
    onTriggerDetonation: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeaderWithBadge(
            title = "Security Subscriptions & Engines",
            badge = "INLINE ML ACTIVE",
            badgeColor = Color(0xFF8B5CF6)
        )

        SecurityServiceToggleRow(
            title = "Advanced WildFire® Cloud Sandbox",
            subtitle = "Zero-day evasive malware cloud detonation in <5 seconds",
            isChecked = isWildFireActive,
            onCheckedChange = onToggleWildFire,
            icon = Icons.Default.CloudQueue,
            testTag = "toggle_wildfire_service"
        )

        SecurityServiceToggleRow(
            title = "Advanced DNS Security",
            subtitle = "Predictive DGA, C2 domain sinkholing & DNS tunneling defense",
            isChecked = isDnsSecurityActive,
            onCheckedChange = onToggleDnsSecurity,
            icon = Icons.Default.Lan,
            testTag = "toggle_dns_security_service"
        )

        SecurityServiceToggleRow(
            title = "SSL/TLS Forward Proxy Decryption",
            subtitle = "Deep packet inspection for encrypted HTTPS traffic with zero blindspots",
            isChecked = isSslDecryptionActive,
            onCheckedChange = onToggleSslDecryption,
            icon = Icons.Default.Lock,
            testTag = "toggle_ssl_decryption_service"
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Advanced WildFire Live Sandbox Log",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF1F5F9)
                    )
                )
                Text(
                    text = "Global cloud threat intelligence network telemetry",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                )
            }

            Button(
                onClick = onTriggerDetonation,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("trigger_wildfire_detonation_button")
            ) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Detonate Zero-Day", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        wildFireSamples.forEach { sample ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, if (sample.detonationStatus.contains("Malicious")) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (sample.detonationStatus.contains("Malicious")) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (sample.detonationStatus.contains("Malicious")) Color(0xFFEF4444) else Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = sample.fileName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF1F5F9)
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (sample.detonationStatus.contains("Malicious")) Color(0xFF450A0A) else Color(0xFF064E3B)
                        ) {
                            Text(
                                text = sample.detonationStatus,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (sample.detonationStatus.contains("Malicious")) Color(0xFFFCA5A5) else Color(0xFF6EE7B7),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SHA256: ${sample.fileHash} • Type: ${sample.fileType} • Reference: ${sample.cveRef}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "Confidence: ${sample.sandboxConfidence}% • Detonated: ${sample.timestamp}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-View 3: Single-Pass Parallel Processing (SP3) & HA
// -------------------------------------------------------------
@Composable
fun SinglePassArchitectureView(
    isSinglePassActive: Boolean,
    onToggleSinglePass: (Boolean) -> Unit,
    latencyUs: Float,
    totalPackets: Long,
    deploymentOption: String,
    onSelectDeployment: (String) -> Unit,
    haState: String,
    onTriggerFailover: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeaderWithBadge(
            title = "Single-Pass Architecture (SP3 Engine)",
            badge = "ULTRA-LOW LATENCY",
            badgeColor = Color(0xFF0284C7)
        )
        Text(
            text = "Performs operations (networking, User-ID, App-ID, Content-ID, and policy evaluation) simultaneously in a single memory pass, eliminating redundant buffer copies.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF030712),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Single-Pass Parallel Processing Pipeline",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SinglePassStageCard(title = "Ingress Packet", desc = "Zero-Copy RX", color = Color(0xFF64748B))
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                    SinglePassStageCard(title = "SP3 Core Engine", desc = "App-ID + Threat", color = Color(0xFF0284C7), isHighlighted = true)
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                    SinglePassStageCard(title = "Egress Forward", desc = "Line Rate", color = Color(0xFF10B981))
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Hardware Processing Latency: ${"%.1f".format(latencyUs)} µs",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6EE7B7), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                    Text(
                        text = "Packets Processed: $totalPackets",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 10.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(16.dp))

        // Deployment Options
        SectionHeaderWithBadge(
            title = "Deployment Form Factors",
            badge = "FLEXIBLE ARCHITECTURE",
            badgeColor = Color(0xFFF59E0B)
        )

        val deploymentList = listOf(
            "Hardware Appliance (PA-5450 DPU)",
            "Virtual Firewall (VM-Series AWS/Azure)",
            "Containerized Firewall (CN-Series K8s)"
        )

        deploymentList.forEach { option ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { onSelectDeployment(option) },
                shape = RoundedCornerShape(8.dp),
                color = if (deploymentOption == option) Color(0xFF0C2444) else Color(0xFF0F172A),
                border = BorderStroke(1.dp, if (deploymentOption == option) Color(0xFF38BDF8) else Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (deploymentOption == option) Icons.Default.CheckCircle else Icons.Default.Router,
                        contentDescription = null,
                        tint = if (deploymentOption == option) Color(0xFF38BDF8) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (deploymentOption == option) FontWeight.Bold else FontWeight.Normal,
                            color = if (deploymentOption == option) Color(0xFFF1F5F9) else Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // High Availability (HA)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "High Availability (HA) & Clustering",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF1F5F9)
                            )
                        )
                        Text(
                            text = haState,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6EE7B7), fontSize = 11.sp)
                        )
                    }

                    Button(
                        onClick = onTriggerFailover,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("trigger_ha_failover_button")
                    ) {
                        Text("Test Failover", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-View 4: Application Command Center (ACC) & AIOps Telemetry
// -------------------------------------------------------------
@Composable
fun AccAndAiOpsView(
    topApps: List<AccAppMetric>,
    aiHealthScore: Float,
    activeVpnUsers: Int,
    onOptimizePosture: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeaderWithBadge(
            title = "Application Command Center (ACC)",
            badge = "VISUAL RISK DASHBOARD",
            badgeColor = Color(0xFF38BDF8)
        )
        Text(
            text = "Real-time visibility into application traffic volume, risk profiles, and active threats across the network.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        topApps.forEach { metric ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${metric.appName} (${metric.category})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                    )
                    Text(
                        text = "${metric.bandwidthGb} GB • ${metric.threatCount} Threats",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = metric.color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (metric.bandwidthGb / 50f).coerceIn(0.05f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = metric.color,
                    trackColor = Color(0xFF1E293B)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(16.dp))

        // AIOps for NGFW & GlobalProtect
        SectionHeaderWithBadge(
            title = "AIOps for NGFW & Strata Cloud Manager",
            badge = "MACHINE LEARNING OPS",
            badgeColor = Color(0xFF10B981)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AIOps Health Posture Score: ${"%.1f".format(aiHealthScore)}%",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        )
                        Text(
                            text = "Active GlobalProtect ZTNA VPN Users: $activeVpnUsers",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.sp)
                        )
                    }

                    Button(
                        onClick = onOptimizePosture,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("optimize_aiops_posture_button")
                    ) {
                        Text("Auto-Remediate", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-View 5: Advanced & Emerging Capabilities
// -------------------------------------------------------------
@Composable
fun EmergingCapabilitiesView(
    isZeroTrustEnforced: Boolean,
    onToggleZeroTrust: (Boolean) -> Unit,
    isPqcVpnActive: Boolean,
    onTogglePqcVpn: (Boolean) -> Unit,
    is5gSliceProtected: Boolean,
    onToggle5gSlice: (Boolean) -> Unit,
    onTriggerQuantumRekey: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeaderWithBadge(
            title = "Zero Trust, PQC & 5G Security",
            badge = "POST-QUANTUM READY",
            badgeColor = Color(0xFF8B5CF6)
        )

        SecurityServiceToggleRow(
            title = "Zero Trust Policy Enforcement (ZTNA)",
            subtitle = "Strict default-deny micro-segmentation across internal VLANs and hybrid clouds",
            isChecked = isZeroTrustEnforced,
            onCheckedChange = onToggleZeroTrust,
            icon = Icons.Default.VerifiedUser,
            testTag = "toggle_zero_trust_service"
        )

        SecurityServiceToggleRow(
            title = "Post-Quantum Cryptography (PQC) VPN",
            subtitle = "Hybrid ML-KEM-1024 + ML-DSA lattice encryption protecting against quantum decryption",
            isChecked = isPqcVpnActive,
            onCheckedChange = onTogglePqcVpn,
            icon = Icons.Default.VpnLock,
            testTag = "toggle_pqc_vpn_service"
        )

        SecurityServiceToggleRow(
            title = "5G & 4G/LTE Infrastructure Security",
            subtitle = "Dedicated GTP-U tunnel inspection, subscriber IMSI protection, and slice isolation",
            isChecked = is5gSliceProtected,
            onCheckedChange = onToggle5gSlice,
            icon = Icons.Default.Devices,
            testTag = "toggle_5g_slice_service"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GlobalProtect PQC Key Exchange",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                    )
                    Text(
                        text = "Standard: NIST FIPS-203 Kyber-1024 / ML-KEM",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF38BDF8), fontSize = 10.sp)
                    )
                }

                OutlinedButton(
                    onClick = onTriggerQuantumRekey,
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("trigger_pqc_rekey_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFC084FC))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quantum Re-Key", color = Color(0xFFC084FC), fontSize = 10.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Helper UI Components
// -------------------------------------------------------------
@Composable
fun SectionHeaderWithBadge(
    title: String,
    badge: String,
    badgeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = badgeColor.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
        ) {
            Text(
                text = badge,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = badgeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
fun RiskBadge(risk: Int) {
    val (label, bg, fg) = when (risk) {
        5 -> Triple("CRITICAL", Color(0xFF450A0A), Color(0xFFFCA5A5))
        4 -> Triple("HIGH", Color(0xFF78350F), Color(0xFFFDE68A))
        3 -> Triple("MEDIUM", Color(0xFF422006), Color(0xFFFED7AA))
        2 -> Triple("LOW", Color(0xFF064E3B), Color(0xFF6EE7B7))
        else -> Triple("SAFE", Color(0xFF0F172A), Color(0xFF38BDF8))
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bg
    ) {
        Text(
            text = "RISK $risk: $label",
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = fg,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun NgfwMetricTile(
    label: String,
    value: String,
    subtext: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 15.sp
                )
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF64748B),
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
fun SecurityServiceToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isChecked) Color(0xFF38BDF8) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF64748B),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFF8FAFC),
                    checkedTrackColor = Color(0xFF0284C7),
                    uncheckedThumbColor = Color(0xFF64748B),
                    uncheckedTrackColor = Color(0xFF1E293B)
                )
            )
        }
    }
}

@Composable
fun SinglePassStageCard(
    title: String,
    desc: String,
    color: Color,
    isHighlighted: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isHighlighted) Color(0xFF0369A1).copy(alpha = 0.3f) else Color(0xFF0F172A),
        border = BorderStroke(1.dp, if (isHighlighted) Color(0xFF38BDF8) else Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlighted) Color(0xFF38BDF8) else Color(0xFFF1F5F9),
                    fontSize = 10.sp
                )
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp
                )
            )
        }
    }
}
