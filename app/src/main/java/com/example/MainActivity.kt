package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import com.example.network.NetworkStatus
import com.example.network.RetryLogEntry
import com.example.network.SimulationMode
import com.example.ui.NetworkViewModel
import com.example.ui.TestStats
import com.example.ui.ThreatSpiderChartDashboardCard
import com.example.ui.ThreatTimeline24hCard
import com.example.ui.QrGatewayScannerCard
import com.example.ui.ThreatNotificationSettingsCard
import com.example.ui.QuantumCryptoPanelCard
import com.example.ui.SecurityOverviewDashboardCard
import com.example.ui.ThreatLogCsvExporterCard
import com.example.ui.SafeHostWhitelistCard
import com.example.ui.LlmBatteryMonitorCard
import com.example.ui.SecurityDigestCard
import com.example.ui.GeoThreatHeatMapCard
import com.example.ui.QuantumTunnelHealthWidgetCard
import com.example.ui.ThemeManagerCard
import com.example.ui.QuickActionsBottomSheet
import com.example.ui.RecentThreatsRoomScreen
import com.example.ui.ThreatDensityHeatmapCard
import com.example.ui.DualLlmFirewallCard
import com.example.ui.HardwareAcceleratorCard
import com.example.ui.LlmAutoDebuggerCard
import com.example.ui.NetworkTrafficLogViewModel
import com.example.ui.NetworkTrafficLoggerCard
import com.example.ui.PerAppFirewallCard
import com.example.ui.PiHoleIpBlockerCard
import com.example.ui.CustomEncryptedDnsCard
import com.example.ui.DualLlmPersistentSettingsCard
import com.example.ui.WifiSecurityInspectorCard
import com.example.ui.DpiProtocolFilterCard
import com.example.ui.SecurityAutomationRulesCard
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExtendedFloatingActionButton
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NetworkViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NetShieldApp(viewModel = viewModel, isDarkMode = isDarkMode)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetShieldApp(
    viewModel: NetworkViewModel = viewModel(),
    isDarkMode: Boolean = true
) {
    val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()
    val retryLogs by viewModel.retryLogs.collectAsStateWithLifecycle()
    val simulationMode by viewModel.simulationMode.collectAsStateWithLifecycle()
    val failTargetCount by viewModel.failTargetCount.collectAsStateWithLifecycle()
    val maxRetries by viewModel.maxRetries.collectAsStateWithLifecycle()
    val initialDelayMs by viewModel.initialDelayMs.collectAsStateWithLifecycle()
    val backoffMultiplier by viewModel.backoffMultiplier.collectAsStateWithLifecycle()
    val useJitter by viewModel.useJitter.collectAsStateWithLifecycle()
    val isRequestInProgress by viewModel.isRequestInProgress.collectAsStateWithLifecycle()
    val lastTestResult by viewModel.lastTestResult.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    val isIdsEnabled by viewModel.isIdsEngineEnabled.collectAsStateWithLifecycle()
    val isTorEnabled by viewModel.isTorRoutingEnabled.collectAsStateWithLifecycle()
    val isQuantumEnabled by viewModel.isQuantumEncryptionEnabled.collectAsStateWithLifecycle()
    val isAutoBlockEnabled by viewModel.isAutoFirewallBlockEnabled.collectAsStateWithLifecycle()
    val blockedRules by viewModel.blockedFirewallRules.collectAsStateWithLifecycle()
    val dualLlmScanState by viewModel.dualLlmScanState.collectAsStateWithLifecycle()
    val isGpuCryptoEnabled by viewModel.isGpuCryptoAccelEnabled.collectAsStateWithLifecycle()
    val isNpuNeuralEnabled by viewModel.isNpuNeuralAccelEnabled.collectAsStateWithLifecycle()
    val isArmNeonEnabled by viewModel.isArmNeonVectorEnabled.collectAsStateWithLifecycle()
    val selectedCpuProfile by viewModel.selectedCpuProfile.collectAsStateWithLifecycle()
    val hardwareDeviceInfo by viewModel.hardwareDeviceInfo.collectAsStateWithLifecycle()
    val hardwareOffloadMetrics by viewModel.hardwareOffloadMetrics.collectAsStateWithLifecycle()
    val hwBenchmarkState by viewModel.hwBenchmarkState.collectAsStateWithLifecycle()
    val startupHwCheckState by viewModel.startupHwCheckState.collectAsStateWithLifecycle()
    val llmDebugState by viewModel.llmDebugState.collectAsStateWithLifecycle()
    val llmCustomDebug by viewModel.llmCustomDebug.collectAsStateWithLifecycle()
    val powerSavingState by viewModel.powerSavingState.collectAsStateWithLifecycle()
    val isDualLlmEngineEnabled by viewModel.isDualLlmEngineEnabled.collectAsStateWithLifecycle()
    val alertCacheCount by viewModel.alertCacheCount.collectAsStateWithLifecycle()
    val dbThreatLogs by viewModel.dbThreatLogs.collectAsStateWithLifecycle()
    val appRules by viewModel.appFirewallRules.collectAsStateWithLifecycle()
    val piHoleRules by viewModel.piHoleRules.collectAsStateWithLifecycle()
    val piHoleBlocklists by viewModel.piHoleBlocklists.collectAsStateWithLifecycle()
    val encryptedDnsState by viewModel.encryptedDnsState.collectAsStateWithLifecycle()
    val wifiSecurityState by viewModel.wifiSecurityState.collectAsStateWithLifecycle()
    val isWifiScanning by viewModel.isWifiScanning.collectAsStateWithLifecycle()
    val dpiRules by viewModel.dpiRules.collectAsStateWithLifecycle()
    val automationRules by viewModel.automationRules.collectAsStateWithLifecycle()

    val trafficLogViewModel: NetworkTrafficLogViewModel = viewModel()
    val trafficLogs by trafficLogViewModel.filteredTrafficLogs.collectAsStateWithLifecycle()
    val totalTrafficCount by trafficLogViewModel.totalLogCount.collectAsStateWithLifecycle()
    val highRiskTrafficCount by trafficLogViewModel.highRiskThreatCount.collectAsStateWithLifecycle()
    val selectedThreatFilter by trafficLogViewModel.selectedThreatFilter.collectAsStateWithLifecycle()
    val isSimulatingTraffic by trafficLogViewModel.isSimulatingCapture.collectAsStateWithLifecycle()

    var showQuickActionsSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "NetShield Logo",
                            tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NetShield Pro",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Quantum & Gateway Network Resilience",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showQuickActionsSheet = true },
                        modifier = Modifier.testTag("quick_actions_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Quick Actions",
                            tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.Bedtime,
                            contentDescription = "Toggle Theme Mode",
                            tint = if (isDarkMode) Color(0xFFF59E0B) else Color(0xFF0284C7)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearLogs() },
                        modifier = Modifier.testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear logs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showQuickActionsSheet = true },
                icon = { Icon(imageVector = Icons.Default.Bolt, contentDescription = null) },
                text = { Text("Quick Actions", fontWeight = FontWeight.Bold) },
                containerColor = if (isDarkMode) Color(0xFF0284C7) else Color(0xFF0284C7),
                contentColor = Color.White,
                modifier = Modifier.testTag("quick_actions_fab")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (showQuickActionsSheet) {
            QuickActionsBottomSheet(
                isDarkMode = isDarkMode,
                isIdsEnabled = isIdsEnabled,
                onToggleIds = { viewModel.toggleIdsEngine() },
                isTorEnabled = isTorEnabled,
                onToggleTor = { viewModel.toggleTorRouting() },
                isQuantumEnabled = isQuantumEnabled,
                onToggleQuantum = { viewModel.toggleQuantumEncryption() },
                isAutoBlockEnabled = isAutoBlockEnabled,
                onToggleAutoBlock = { viewModel.toggleAutoFirewallBlock() },
                isGpuCryptoEnabled = isGpuCryptoEnabled,
                onToggleGpuCrypto = { viewModel.toggleGpuCryptoAccel() },
                alertCacheCount = alertCacheCount,
                onClearAlertCache = { viewModel.clearAlertCache() },
                onDismiss = { showQuickActionsSheet = false }
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Real-Time Network Connectivity Indicator Banner
            item {
                ConnectivityStatusCard(networkStatus = networkStatus)
            }

            // System-Wide Theme Manager & Display Control Card
            item {
                ThemeManagerCard(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() }
                )
            }

            // Status Banner
            item {
                ResilienceHeaderCard(
                    maxRetries = maxRetries,
                    initialDelayMs = initialDelayMs,
                    multiplier = backoffMultiplier,
                    useJitter = useJitter,
                    stats = stats
                )
            }

            // Security Overview Dashboard: Quantum Risk Index & Real-Time Traffic Overlay
            item {
                SecurityOverviewDashboardCard()
            }

            // Recent Network Threats (Room Database Live Persistence Screen)
            item {
                RecentThreatsRoomScreen(
                    threatLogs = dbThreatLogs,
                    onSimulateThreat = { viewModel.simulateNewThreat() },
                    onClearLogs = { viewModel.clearAlertCache() },
                    onDeleteLog = { id -> viewModel.deleteThreatLogById(id) }
                )
            }

            // Visual Threat Density Temporal Heatmap Dashboard
            item {
                ThreatDensityHeatmapCard(threatLogs = dbThreatLogs)
            }

            // Real-Time IDS Anomaly Spider/Radar Chart Dashboard
            item {
                ThreatSpiderChartDashboardCard()
            }

            // 24-Hour Threat History Timeline Analytics Card
            item {
                ThreatTimeline24hCard()
            }

            // 24-Hour Security Threat Log CSV Exporter Card
            item {
                ThreatLogCsvExporterCard()
            }

            // Safe Host Whitelist Rule Management Card
            item {
                SafeHostWhitelistCard()
            }

            // Per-App Network Rules (Allow or Deny Any App) Card
            item {
                PerAppFirewallCard(
                    appRules = appRules,
                    onUpdateStatus = { pkg, st -> viewModel.updateAppRuleStatus(pkg, st) },
                    onAddCustomApp = { name, pkg, st -> viewModel.addCustomAppRule(name, pkg, st) },
                    onDeleteApp = { pkg -> viewModel.deleteAppRule(pkg) },
                    onBatchSet = { denySys -> viewModel.setBatchAppRules(denySys) }
                )
            }

            // Pi-hole Style IP & Domain Sinkhole Blocker Card
            item {
                PiHoleIpBlockerCard(
                    piHoleRules = piHoleRules,
                    blocklists = piHoleBlocklists,
                    onAddRule = { target, action, category -> viewModel.addPiHoleRule(target, action, category) },
                    onToggleRule = { id, enabled -> viewModel.togglePiHoleRule(id, enabled) },
                    onDeleteRule = { id -> viewModel.deletePiHoleRule(id) },
                    onToggleBlocklist = { id, enabled -> viewModel.togglePiHoleBlocklist(id, enabled) }
                )
            }

            // Custom & Encrypted DNS Engine Card (DoH, DoT, DoQ, DNSCrypt, TOR, ODoH)
            item {
                CustomEncryptedDnsCard(
                    dnsState = encryptedDnsState,
                    onSetProtocol = { proto -> viewModel.setDnsProtocol(proto) },
                    onSetPreset = { preset, prim, sec, doh, dot -> viewModel.setDnsPreset(preset, prim, sec, doh, dot) },
                    onUpdateCustomDns = { prim, sec, doh, dot, dnscrypt -> viewModel.updateCustomDnsServers(prim, sec, doh, dot, dnscrypt) },
                    onToggleLeakProtection = { enabled -> viewModel.toggleDnsLeakProtection(enabled) },
                    onToggleDnsSec = { enabled -> viewModel.toggleDnsSecValidation(enabled) },
                    onToggleFallback = { enabled -> viewModel.toggleAllowFallbackToPlaintext(enabled) },
                    onRunDiagnosticTest = { viewModel.runDnsDiagnosticTest() }
                )
            }

            // Wi-Fi & MitM Security Guard Card
            item {
                WifiSecurityInspectorCard(
                    wifiState = wifiSecurityState,
                    isScanning = isWifiScanning,
                    onRunAudit = { viewModel.runWifiSecurityAudit() }
                )
            }

            // DPI Payload & SNI Sanitizer Filter Card
            item {
                DpiProtocolFilterCard(
                    dpiRules = dpiRules,
                    onToggleRule = { id, enabled -> viewModel.toggleDpiRule(id, enabled) },
                    onAddRule = { name, proto, action, pattern -> viewModel.addDpiRule(name, proto, action, pattern) },
                    onDeleteRule = { id -> viewModel.deleteDpiRule(id) }
                )
            }

            // Automated Defense Trigger Rules Card
            item {
                SecurityAutomationRulesCard(
                    automationRules = automationRules,
                    onToggleRule = { id, enabled -> viewModel.toggleAutomationRule(id, enabled) },
                    onAddRule = { cond, act -> viewModel.addAutomationRule(cond, act) },
                    onDeleteRule = { id -> viewModel.deleteAutomationRule(id) }
                )
            }

            // Persistent Settings Toggle for Dual-LLM Analysis Engine
            item {
                DualLlmPersistentSettingsCard(
                    isEngineEnabled = isDualLlmEngineEnabled,
                    onToggleEngine = { viewModel.toggleDualLlmEngine(it) },
                    powerSavingState = powerSavingState,
                    onSetLlmIntensity = { viewModel.setDualLlmIntensity(it) }
                )
            }

            // Dual-LLM Power & Battery Impact Monitor Card
            item {
                LlmBatteryMonitorCard(
                    powerSavingState = powerSavingState,
                    onTogglePowerSavingMode = { viewModel.togglePowerSavingMode(it) },
                    onToggleAutoBatterySync = { viewModel.toggleAutoBatterySync(it) },
                    onToggleDualLlmEngine = { viewModel.toggleDualLlmEngine(it) },
                    onSetBatteryLevel = { viewModel.setSimulatedBatteryLevel(it) },
                    onSetThreshold = { viewModel.setAutoSavingsThreshold(it) },
                    onSetEncryptionMode = { viewModel.setEncryptionEngineMode(it) },
                    onSetLlmIntensity = { viewModel.setDualLlmIntensity(it) }
                )
            }

            // Room Database Network Traffic Logs Card for Intrusion Detection
            item {
                NetworkTrafficLoggerCard(
                    trafficLogs = trafficLogs,
                    totalLogCount = totalTrafficCount,
                    highRiskCount = highRiskTrafficCount,
                    selectedFilter = selectedThreatFilter,
                    isSimulating = isSimulatingTraffic,
                    onSetFilter = { trafficLogViewModel.setThreatFilter(it) },
                    onSimulateBurst = { trafficLogViewModel.simulateIntrusionCaptureBurst() },
                    onAddCustomLog = { srcIp, srcPort, dstIp, dstPort, proto, level, cat, act, payload ->
                        trafficLogViewModel.captureCustomPacket(srcIp, srcPort, dstIp, dstPort, proto, level, cat, act, payload)
                    },
                    onDeleteLog = { trafficLogViewModel.deleteLogById(it) },
                    onClearAll = { trafficLogViewModel.clearAllLogs() }
                )
            }

            // Dual-LLM Auto-Firewall Threat Blocking Engine Card
            item {
                DualLlmFirewallCard(
                    isAutoBlockEnabled = isAutoBlockEnabled,
                    onToggleAutoBlock = { viewModel.toggleAutoFirewallBlock() },
                    isDualLlmEngineEnabled = isDualLlmEngineEnabled,
                    onToggleDualLlmEngine = { viewModel.toggleDualLlmEngine(it) },
                    blockedRules = blockedRules,
                    scanState = dualLlmScanState,
                    onRunScan = { ip -> viewModel.runDualLlmScanAndBlock(ip) },
                    onResetScan = { viewModel.resetDualLlmScanState() },
                    onUnblockIp = { ip -> viewModel.unblockFirewallIp(ip) },
                    onAddManualBlock = { ip, reason -> viewModel.addManualFirewallRule(ip, reason) },
                    onClearAllRules = { viewModel.clearAllFirewallRules() }
                )
            }

            // Gemini System Self-Healing & LLM Auto-Debugger Card
            item {
                LlmAutoDebuggerCard(
                    diagnosticsState = llmDebugState,
                    customDebugResult = llmCustomDebug,
                    onRunDiagnostics = { viewModel.runLlmDiagnosticsAndHealing() },
                    onApplyAutoFix = { viewModel.applyLlmSystemAutoFix() },
                    onResetDiagnostics = { viewModel.resetLlmDebugDiagnostics() },
                    onSubmitCustomQuery = { query -> viewModel.submitCustomLlmDebugQuery(query) },
                    onClearCustomQuery = { viewModel.clearCustomLlmDebug() }
                )
            }

            // CPU & GPU Hardware Offload Engine Card (Snapdragon / Dimensity / Tensor TPU / Vulkan Compute)
            item {
                HardwareAcceleratorCard(
                    isGpuCryptoEnabled = isGpuCryptoEnabled,
                    onToggleGpuCrypto = { viewModel.toggleGpuCryptoAccel() },
                    isNpuNeuralEnabled = isNpuNeuralEnabled,
                    onToggleNpuNeural = { viewModel.toggleNpuNeuralAccel() },
                    isArmNeonEnabled = isArmNeonEnabled,
                    onToggleArmNeon = { viewModel.toggleArmNeonVector() },
                    selectedCpuProfile = selectedCpuProfile,
                    onSelectCpuProfile = { profile -> viewModel.selectCpuProfile(profile) },
                    deviceInfo = hardwareDeviceInfo,
                    metrics = hardwareOffloadMetrics,
                    benchmarkState = hwBenchmarkState,
                    onRunBenchmark = { viewModel.runHardwareBenchmark() },
                    onResetBenchmark = { viewModel.resetHwBenchmark() },
                    startupHwCheckState = startupHwCheckState,
                    onReRunStartupCheck = { viewModel.performStartupHardwareProbe() }
                )
            }

            // Gemini Weekly Security Digest & Hardening Recommendation Card
            item {
                SecurityDigestCard()
            }

            // Geographical Threat Origin Mercator Heat Map Card
            item {
                GeoThreatHeatMapCard()
            }

            // Real-Time Quantum-Resistant Tunnel Health & Latency Monitor Widget
            item {
                QuantumTunnelHealthWidgetCard()
            }

            // Gateway QR Code Scanner (ZXing + CameraX)
            item {
                QrGatewayScannerCard()
            }

            // High-Severity IDS Threat Local Push Notifications Settings Card
            item {
                ThreatNotificationSettingsCard()
            }

            // Post-Quantum Cryptographic (PQC) Protocol Selector Panel
            item {
                QuantumCryptoPanelCard(
                    isQuantumEncryptionEnabled = isQuantumEnabled,
                    onToggleQuantumEncryption = { enabled -> viewModel.setQuantumEncryption(enabled) }
                )
            }

            // Exponential Backoff Configurator
            item {
                BackoffConfigCard(
                    maxRetries = maxRetries,
                    initialDelayMs = initialDelayMs,
                    multiplier = backoffMultiplier,
                    useJitter = useJitter,
                    onMaxRetriesChange = { viewModel.setMaxRetries(it) },
                    onInitialDelayChange = { viewModel.setInitialDelayMs(it) },
                    onMultiplierChange = { viewModel.setBackoffMultiplier(it) },
                    onJitterChange = { viewModel.setUseJitter(it) }
                )
            }

            // Simulation Mode Controls
            item {
                SimulationControlsCard(
                    currentMode = simulationMode,
                    failTargetCount = failTargetCount,
                    onModeSelect = { viewModel.setSimulationMode(it) },
                    onFailCountSelect = { viewModel.setFailTargetCount(it) }
                )
            }

            // Test Execution Buttons
            item {
                ActionButtonsRow(
                    isRequestInProgress = isRequestInProgress,
                    onTestQuantum = { viewModel.testQuantumServerRequest() },
                    onTestGateway = { viewModel.testGatewayServerIngest() }
                )
            }

            // Active Progress Indicator
            if (isRequestInProgress) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7).copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFF38BDF8),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Executing request with Exponential Backoff retry protection...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFFE0F2FE),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF38BDF8),
                                trackColor = Color(0xFF0C4A6E)
                            )
                        }
                    }
                }
            }

            // Test Output Result Banner
            if (lastTestResult != null) {
                item {
                    TestResultCard(resultText = lastTestResult!!)
                }
            }

            // Section Header: Retry Stream Logs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Real-Time Retry Stream (${retryLogs.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                    )
                    if (retryLogs.isNotEmpty()) {
                        Text(
                            text = "Newest First",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF64748B)
                            )
                        )
                    }
                }
            }

            // Empty state if no retries yet
            if (retryLogs.isEmpty()) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No retry events recorded yet",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "Tap 'Test Quantum Server' or 'Test Gateway Server' to simulate requests and view live backoff retries.",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFF64748B)
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // List of Retry Logs
            items(retryLogs, key = { it.id }) { log ->
                RetryLogItem(log = log)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun ConnectivityStatusCard(networkStatus: NetworkStatus) {
    val isConnected = networkStatus is NetworkStatus.Connected
    val containerColor = if (isConnected) Color(0xFF0F291E) else Color(0xFF321419)
    val borderColor = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
    val iconTint = if (isConnected) Color(0xFF34D399) else Color(0xFFF87171)

    val (titleText, detailText, iconVector) = when (networkStatus) {
        is NetworkStatus.Connected -> {
            val typeStr = when {
                networkStatus.isWifi -> "Wi-Fi Network"
                networkStatus.isCellular -> "Cellular Network"
                else -> "Validated Internet Network"
            }
            val meteredStr = if (networkStatus.isMetered) " (Metered)" else " (Unmetered)"
            Triple("NETWORK ONLINE", typeStr + meteredStr, if (networkStatus.isWifi) Icons.Default.Wifi else Icons.Default.SignalCellularAlt)
        }
        is NetworkStatus.Disconnected -> {
            Triple("NETWORK DISCONNECTED", "Network-dependent background syncs & tasks paused automatically", Icons.Default.WifiOff)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(borderColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(borderColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = borderColor,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFE2E8F0)
                    )
                )
            }
        }
    }
}

@Composable
fun ResilienceHeaderCard(
    maxRetries: Int,
    initialDelayMs: Long,
    multiplier: Double,
    useJitter: Boolean,
    stats: TestStats
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXPONENTIAL BACKOFF ACTIVE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E),
                            letterSpacing = 1.sp
                        )
                    )
                }

                Text(
                    text = "${maxRetries} Retries • ${initialDelayMs}ms Base",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFF334155)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("Total Requests", "${stats.totalRequests}", Color(0xFFE2E8F0))
                StatItem("Retry Attempts", "${stats.totalRetries}", Color(0xFFF59E0B))
                StatItem("Recoveries", "${stats.successfulRecoveries}", Color(0xFF10B981))
                StatItem("Failed", "${stats.finalFailures}", Color(0xFFEF4444))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
        )
    }
}

@Composable
fun BackoffConfigCard(
    maxRetries: Int,
    initialDelayMs: Long,
    multiplier: Double,
    useJitter: Boolean,
    onMaxRetriesChange: (Int) -> Unit,
    onInitialDelayChange: (Long) -> Unit,
    onMultiplierChange: (Double) -> Unit,
    onJitterChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Retry Mechanism Configuration",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Max Retries Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Max Retry Attempts", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)))
                Text(text = "$maxRetries attempts", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold))
            }
            Slider(
                value = maxRetries.toFloat(),
                onValueChange = { onMaxRetriesChange(it.toInt()) },
                valueRange = 1f..8f,
                steps = 6,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF38BDF8),
                    activeTrackColor = Color(0xFF0284C7),
                    inactiveTrackColor = Color(0xFF334155)
                )
            )

            // Initial Delay Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Initial Base Delay", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)))
                Text(text = "${initialDelayMs}ms", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold))
            }
            Slider(
                value = initialDelayMs.toFloat(),
                onValueChange = { onInitialDelayChange(it.toLong()) },
                valueRange = 200f..4000f,
                steps = 18,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF38BDF8),
                    activeTrackColor = Color(0xFF0284C7),
                    inactiveTrackColor = Color(0xFF334155)
                )
            )

            // Multiplier & Jitter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Backoff Multiplier", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)))
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1.5, 2.0, 2.5, 3.0).forEach { m ->
                            FilterChip(
                                selected = multiplier == m,
                                onClick = { onMultiplierChange(m) },
                                label = { Text("${m}x", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0284C7),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF0F172A),
                                    labelColor = Color(0xFF94A3B8)
                                )
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Random Jitter", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)))
                        Text(text = if (useJitter) "Enabled" else "Disabled", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B)))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = useJitter,
                        onCheckedChange = onJitterChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimulationControlsCard(
    currentMode: SimulationMode,
    failTargetCount: Int,
    onModeSelect: (SimulationMode) -> Unit,
    onFailCountSelect: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Network Condition Simulator",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SimulationChip(
                    label = "Fail $failTargetCount Times Then Recover",
                    selected = currentMode == SimulationMode.FORCE_FAIL_THEN_SUCCEED,
                    onClick = { onModeSelect(SimulationMode.FORCE_FAIL_THEN_SUCCEED) }
                )
                SimulationChip(
                    label = "HTTP 503 Overload",
                    selected = currentMode == SimulationMode.FORCE_503_OVERLOAD,
                    onClick = { onModeSelect(SimulationMode.FORCE_503_OVERLOAD) }
                )
                SimulationChip(
                    label = "HTTP 429 Rate Limit",
                    selected = currentMode == SimulationMode.FORCE_429_RATE_LIMIT,
                    onClick = { onModeSelect(SimulationMode.FORCE_429_RATE_LIMIT) }
                )
                SimulationChip(
                    label = "Network Outage / Timeout",
                    selected = currentMode == SimulationMode.FORCE_NETWORK_OUTAGE,
                    onClick = { onModeSelect(SimulationMode.FORCE_NETWORK_OUTAGE) }
                )
                SimulationChip(
                    label = "Direct Mock 200 OK",
                    selected = currentMode == SimulationMode.MOCK_SUCCESS,
                    onClick = { onModeSelect(SimulationMode.MOCK_SUCCESS) }
                )
            }

            if (currentMode == SimulationMode.FORCE_FAIL_THEN_SUCCEED) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Simulated Initial Failures before recovery:",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    listOf(1, 2, 3, 4).forEach { count ->
                        FilterChip(
                            selected = failTargetCount == count,
                            onClick = { onFailCountSelect(count) },
                            label = { Text("$count", fontSize = 11.sp) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimulationChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFD97706),
            selectedLabelColor = Color.White,
            containerColor = Color(0xFF0F172A),
            labelColor = Color(0xFFCBD5E1)
        )
    )
}

@Composable
fun ActionButtonsRow(
    isRequestInProgress: Boolean,
    onTestQuantum: () -> Unit,
    onTestGateway: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onTestQuantum,
            enabled = !isRequestInProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("test_quantum_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0284C7),
                disabledContainerColor = Color(0xFF334155)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Test Quantum Server (Kyber / Cirq)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        OutlinedButton(
            onClick = onTestGateway,
            enabled = !isRequestInProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("test_gateway_button"),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF38BDF8)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Test Gateway Server (Morpheus Ingest)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun TestResultCard(resultText: String) {
    val isSuccess = resultText.contains("SUCCESS")
    val isException = resultText.contains("EXCEPTION") || resultText.contains("FAILED")

    val bannerColor = when {
        isSuccess -> Color(0xFF059669)
        isException -> Color(0xFFDC2626)
        else -> Color(0xFF0284C7)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bannerColor.copy(alpha = 0.15f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, bannerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = bannerColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
fun RetryLogItem(log: RetryLogEntry) {
    val borderColor = when {
        log.isSuccess -> Color(0xFF10B981)
        log.isFinalFailure -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }

    val icon = when {
        log.isSuccess -> Icons.Default.CheckCircle
        log.isFinalFailure -> Icons.Default.Clear
        else -> Icons.Default.Timer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Attempt ${log.attemptNumber} of ${log.maxRetries + 1}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(borderColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (log.delayMs > 0) "Backoff Wait: ${log.delayMs}ms" else "Resolved",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = borderColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${log.targetHost}${log.endpoint}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF38BDF8),
                    fontFamily = FontFamily.Monospace
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.reason,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFCBD5E1)
                )
            )
        }
    }
}
