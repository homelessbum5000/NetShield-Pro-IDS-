package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NetShieldDatabase
import com.example.data.PacketAnalysisEntity
import com.example.data.ThreatLogEntity
import com.example.data.ThreatRepository
import com.example.network.EncryptedIDSRequest
import com.example.network.NetworkConnectivityManager
import com.example.network.NetworkStatus
import com.example.network.QuantumRandomRequest
import com.example.network.RetrofitClient
import com.example.network.RetryLogEntry
import com.example.network.SimulationMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TestStats(
    val totalRequests: Int = 0,
    val totalRetries: Int = 0,
    val successfulRecoveries: Int = 0,
    val finalFailures: Int = 0
)

class NetworkViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NetShieldDatabase.getInstance(application)
    val threatRepository = ThreatRepository(db.threatLogDao(), db.packetAnalysisDao())

    val dbThreatLogs: StateFlow<List<ThreatLogEntity>> = threatRepository.allThreatLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dbPacketAnalyses: StateFlow<List<PacketAnalysisEntity>> = threatRepository.allPacketAnalyses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val connectivityManager = NetworkConnectivityManager(application)
    val networkStatus: StateFlow<NetworkStatus> = connectivityManager.networkStatus

    val retryLogs: StateFlow<List<RetryLogEntry>> = RetrofitClient.retryLogs

    private val _simulationMode = MutableStateFlow(SimulationMode.FORCE_FAIL_THEN_SUCCEED)
    val simulationMode: StateFlow<SimulationMode> = _simulationMode.asStateFlow()

    private val _failTargetCount = MutableStateFlow(2)
    val failTargetCount: StateFlow<Int> = _failTargetCount.asStateFlow()

    private val _maxRetries = MutableStateFlow(3)
    val maxRetries: StateFlow<Int> = _maxRetries.asStateFlow()

    private val _initialDelayMs = MutableStateFlow(1000L)
    val initialDelayMs: StateFlow<Long> = _initialDelayMs.asStateFlow()

    private val _backoffMultiplier = MutableStateFlow(2.0)
    val backoffMultiplier: StateFlow<Double> = _backoffMultiplier.asStateFlow()

    private val _useJitter = MutableStateFlow(true)
    val useJitter: StateFlow<Boolean> = _useJitter.asStateFlow()

    private val _isRequestInProgress = MutableStateFlow(false)
    val isRequestInProgress: StateFlow<Boolean> = _isRequestInProgress.asStateFlow()

    private val _lastTestResult = MutableStateFlow<String?>(null)
    val lastTestResult: StateFlow<String?> = _lastTestResult.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isIdsEngineEnabled = MutableStateFlow(true)
    val isIdsEngineEnabled: StateFlow<Boolean> = _isIdsEngineEnabled.asStateFlow()

    private val _isTorRoutingEnabled = MutableStateFlow(false)
    val isTorRoutingEnabled: StateFlow<Boolean> = _isTorRoutingEnabled.asStateFlow()

    private val _alertCacheCount = MutableStateFlow(42)
    val alertCacheCount: StateFlow<Int> = _alertCacheCount.asStateFlow()

    private val _stats = MutableStateFlow(TestStats())
    val stats: StateFlow<TestStats> = _stats.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun toggleIdsEngine() {
        _isIdsEngineEnabled.value = !_isIdsEngineEnabled.value
    }

    fun toggleTorRouting() {
        _isTorRoutingEnabled.value = !_isTorRoutingEnabled.value
    }

    fun clearAlertCache() {
        _alertCacheCount.value = 0
        viewModelScope.launch(Dispatchers.IO) {
            threatRepository.clearAllLogs()
            threatRepository.clearAllAnalyses()
        }
    }

    init {
        connectivityManager.startMonitoring()
        applyConfig()
        seedDatabaseIfEmpty()
    }

    private fun seedDatabaseIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            val existingLogs = threatRepository.allThreatLogs.first()
            if (existingLogs.isEmpty()) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val now = System.currentTimeMillis()

                val sampleLogs = listOf(
                    ThreatLogEntity(
                        timestampMs = now - 300000,
                        timestampIso = dateFormat.format(Date(now - 300000)),
                        severity = "CRITICAL",
                        attackVector = "SYN-Flood Volumetric Burst",
                        sourceIp = "185.220.101.5",
                        targetPort = 443,
                        actionTaken = "PACKET_DROPPED",
                        quantumKeyId = "KYBER1024_0x00A1",
                        details = "High density SYN flood detected on post-quantum TLS portal."
                    ),
                    ThreatLogEntity(
                        timestampMs = now - 900000,
                        timestampIso = dateFormat.format(Date(now - 900000)),
                        severity = "HIGH",
                        attackVector = "Quantum Harvest-Now Decrypt-Later",
                        sourceIp = "198.51.100.42",
                        targetPort = 8443,
                        actionTaken = "KYBER_REKEYED",
                        quantumKeyId = "KYBER1024_0x00B2",
                        details = "Forced automatic post-quantum key rotation due to entropy anomaly."
                    ),
                    ThreatLogEntity(
                        timestampMs = now - 1800000,
                        timestampIso = dateFormat.format(Date(now - 1800000)),
                        severity = "MEDIUM",
                        attackVector = "HTTP/2 Rapid Reset Flood",
                        sourceIp = "45.154.255.88",
                        targetPort = 443,
                        actionTaken = "RATE_LIMITED",
                        quantumKeyId = "KYBER1024_0x00C3",
                        details = "Stream multiplexing rate limiting imposed on offending client."
                    )
                )
                threatRepository.insertLogs(sampleLogs)

                val sampleAnalyses = listOf(
                    PacketAnalysisEntity(
                        timestampMs = now - 600000,
                        totalPackets = 1420500,
                        protocol = "TLS",
                        entropyScore = 0.9842,
                        bytesAnalyzed = 104857600,
                        flaggedCount = 184,
                        summary = "High entropy quantum encrypted stream verification pass."
                    ),
                    PacketAnalysisEntity(
                        timestampMs = now - 1200000,
                        totalPackets = 890400,
                        protocol = "HTTP/2",
                        entropyScore = 0.7410,
                        bytesAnalyzed = 52428800,
                        flaggedCount = 42,
                        summary = "Standard multiplexed stream with minor header anomalies."
                    )
                )
                threatRepository.insertAnalyses(sampleAnalyses)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        connectivityManager.stopMonitoring()
    }

    fun setSimulationMode(mode: SimulationMode) {
        _simulationMode.value = mode
        RetrofitClient.mockSimulator.mode = mode
        RetrofitClient.mockSimulator.resetCounts()
    }

    fun setFailTargetCount(count: Int) {
        _failTargetCount.value = count
        RetrofitClient.mockSimulator.failCountTarget = count
        RetrofitClient.mockSimulator.resetCounts()
    }

    fun setMaxRetries(retries: Int) {
        _maxRetries.value = retries
        applyConfig()
    }

    fun setInitialDelayMs(delay: Long) {
        _initialDelayMs.value = delay
        applyConfig()
    }

    fun setBackoffMultiplier(multiplier: Double) {
        _backoffMultiplier.value = multiplier
        applyConfig()
    }

    fun setUseJitter(enabled: Boolean) {
        _useJitter.value = enabled
        applyConfig()
    }

    private fun applyConfig() {
        RetrofitClient.updateConfig(
            maxRetries = _maxRetries.value,
            initialDelayMs = _initialDelayMs.value,
            maxDelayMs = 16000L,
            backoffMultiplier = _backoffMultiplier.value,
            useJitter = _useJitter.value
        )
    }

    fun clearLogs() {
        RetrofitClient.clearLogs()
        _lastTestResult.value = null
        _stats.value = TestStats()
    }

    fun testQuantumServerRequest() {
        if (_isRequestInProgress.value) return
        _isRequestInProgress.value = true
        _lastTestResult.value = "Executing Quantum Random request with exponential backoff protection..."

        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                val response = RetrofitClient.quantumApiService.getRandomBytes(
                    request = QuantumRandomRequest(nBytes = 64)
                )

                val elapsed = System.currentTimeMillis() - startTime
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        _lastTestResult.value = "✅ QUANTUM REQUEST SUCCESS (${elapsed}ms)\nStatus: ${body?.status}, Source: ${body?.entropySource}"
                        updateStats(success = true)
                    } else {
                        _lastTestResult.value = "❌ QUANTUM REQUEST FAILED (${elapsed}ms)\nHTTP Code: ${response.code()} - ${response.message()}"
                        updateStats(success = false)
                    }
                }
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis() - startTime
                withContext(Dispatchers.Main) {
                    _lastTestResult.value = "💥 QUANTUM EXCEPTION (${elapsed}ms)\n${e.localizedMessage ?: e.javaClass.simpleName}"
                    updateStats(success = false)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isRequestInProgress.value = false
                }
            }
        }
    }

    fun testGatewayServerIngest() {
        if (_isRequestInProgress.value) return
        _isRequestInProgress.value = true
        _lastTestResult.value = "Executing Gateway Ingest request with exponential backoff protection..."

        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                val samplePayload = EncryptedIDSRequest(
                    kyberCiphertext = "KYBER1024_CT_EXample123456789==",
                    wrappedAesKey = "WRAPPED_AES_KEY_8877665544==",
                    aesIv = "IV_123456789012",
                    payloadData = "SURE_TELEMETRY_BLOCK_NETSHIELD"
                )

                val response = RetrofitClient.gatewayApiService.ingestBatch(
                    request = samplePayload
                )

                val elapsed = System.currentTimeMillis() - startTime
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        _lastTestResult.value = "✅ GATEWAY INGEST SUCCESS (${elapsed}ms)\nStatus: ${body?.status}, ID: ${body?.ingestId}"
                        updateStats(success = true)
                    } else {
                        _lastTestResult.value = "❌ GATEWAY INGEST FAILED (${elapsed}ms)\nHTTP Code: ${response.code()} - ${response.message()}"
                        updateStats(success = false)
                    }
                }
            } catch (e: Exception) {
                val elapsed = System.currentTimeMillis() - startTime
                withContext(Dispatchers.Main) {
                    _lastTestResult.value = "💥 GATEWAY EXCEPTION (${elapsed}ms)\n${e.localizedMessage ?: e.javaClass.simpleName}"
                    updateStats(success = false)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isRequestInProgress.value = false
                }
            }
        }
    }

    private fun updateStats(success: Boolean) {
        val logs = retryLogs.value
        val retryCountForRun = logs.count { !it.isSuccess && !it.isFinalFailure }
        
        val currentStats = _stats.value
        _stats.value = currentStats.copy(
            totalRequests = currentStats.totalRequests + 1,
            totalRetries = currentStats.totalRetries + retryCountForRun,
            successfulRecoveries = if (success && retryCountForRun > 0) currentStats.successfulRecoveries + 1 else currentStats.successfulRecoveries,
            finalFailures = if (!success) currentStats.finalFailures + 1 else currentStats.finalFailures
        )
    }
}
