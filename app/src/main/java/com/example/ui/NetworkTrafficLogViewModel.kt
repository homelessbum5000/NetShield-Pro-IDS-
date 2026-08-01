package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NetShieldDatabase
import com.example.data.NetworkTrafficLogEntity
import com.example.data.NetworkTrafficLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class NetworkTrafficLogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NetworkTrafficLogRepository

    val allTrafficLogs: StateFlow<List<NetworkTrafficLogEntity>>
    val totalLogCount: StateFlow<Int>
    val highRiskThreatCount: StateFlow<Int>

    private val _selectedThreatFilter = MutableStateFlow("ALL")
    val selectedThreatFilter: StateFlow<String> = _selectedThreatFilter.asStateFlow()

    private val _isSimulatingCapture = MutableStateFlow(false)
    val isSimulatingCapture: StateFlow<Boolean> = _isSimulatingCapture.asStateFlow()

    val filteredTrafficLogs: StateFlow<List<NetworkTrafficLogEntity>>

    init {
        val dao = NetShieldDatabase.getInstance(application).networkTrafficLogDao()
        repository = NetworkTrafficLogRepository(dao)

        allTrafficLogs = repository.allTrafficLogs
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        totalLogCount = repository.totalLogCount
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

        highRiskThreatCount = repository.highRiskThreatCount
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

        filteredTrafficLogs = combine(allTrafficLogs, _selectedThreatFilter) { logs, filter ->
            if (filter == "ALL") {
                logs
            } else {
                logs.filter { it.threatLevel.equals(filter, ignoreCase = true) }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prepopulate with default intrusion detection traffic logs if database is empty
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.allTrafficLogs.first()
            if (existing.isEmpty()) {
                seedInitialTrafficLogs()
            }
        }
    }

    fun setThreatFilter(filter: String) {
        _selectedThreatFilter.value = filter
    }

    fun captureCustomPacket(
        sourceIp: String,
        sourcePort: Int,
        destinationIp: String,
        destinationPort: Int,
        protocol: String,
        threatLevel: String,
        threatCategory: String,
        actionTaken: String,
        payloadSnippet: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val log = NetworkTrafficLogEntity(
                timestampMs = System.currentTimeMillis(),
                timestampFormatted = sdf.format(Date()),
                sourceIp = sourceIp.ifBlank { "192.168.1.${Random.nextInt(2, 254)}" },
                sourcePort = sourcePort,
                destinationIp = destinationIp.ifBlank { "10.0.0.1" },
                destinationPort = destinationPort,
                protocol = protocol,
                packetSizeBytes = Random.nextLong(128, 4096),
                threatLevel = threatLevel,
                threatCategory = threatCategory,
                actionTaken = actionTaken,
                payloadSnippet = payloadSnippet
            )
            repository.insertLog(log)
        }
    }

    fun simulateIntrusionCaptureBurst() {
        if (_isSimulatingCapture.value) return
        _isSimulatingCapture.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val categories = listOf(
                Triple("CRITICAL", "SYN Flood DDoS", "BLOCKED"),
                Triple("HIGH", "SQL Injection Vector", "BLOCKED"),
                Triple("MEDIUM", "Unusual Port Probe", "FLAGGED"),
                Triple("LOW", "Malformed DNS Query", "LOGGED"),
                Triple("SAFE", "TLS 1.3 Handshake", "ALLOWED")
            )

            for (i in 1..4) {
                delay(300)
                val (threat, category, action) = categories[Random.nextInt(categories.size)]
                val srcIp = "185.220.101.${Random.nextInt(10, 220)}"
                val dstIp = "10.0.0.${Random.nextInt(1, 10)}"
                val proto = listOf("TCP", "UDP", "TLS 1.3", "QUIC", "HTTP/3").random()

                val log = NetworkTrafficLogEntity(
                    timestampMs = System.currentTimeMillis(),
                    timestampFormatted = sdf.format(Date()),
                    sourceIp = srcIp,
                    sourcePort = Random.nextInt(1024, 65535),
                    destinationIp = dstIp,
                    destinationPort = listOf(80, 443, 8080, 53, 22).random(),
                    protocol = proto,
                    packetSizeBytes = Random.nextLong(256, 2048),
                    threatLevel = threat,
                    threatCategory = category,
                    actionTaken = action,
                    payloadSnippet = "INT-IDS-CAPTURE #$i: Packet payload checked against neural signatures."
                )
                repository.insertLog(log)
            }
            _isSimulatingCapture.value = false
        }
    }

    fun deleteLogById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLogById(id)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllLogs()
        }
    }

    private suspend fun seedInitialTrafficLogs() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = System.currentTimeMillis()

        val sampleLogs = listOf(
            NetworkTrafficLogEntity(
                timestampMs = now - 120000,
                timestampFormatted = sdf.format(Date(now - 120000)),
                sourceIp = "198.51.100.42",
                sourcePort = 44321,
                destinationIp = "10.0.0.1",
                destinationPort = 8080,
                protocol = "TCP",
                packetSizeBytes = 1420,
                threatLevel = "CRITICAL",
                threatCategory = "SQL Injection Payload",
                actionTaken = "BLOCKED",
                payloadSnippet = "SELECT * FROM users WHERE '1'='1' -- UNION ALL SELECT password FROM admin"
            ),
            NetworkTrafficLogEntity(
                timestampMs = now - 90000,
                timestampFormatted = sdf.format(Date(now - 90000)),
                sourceIp = "203.0.113.19",
                sourcePort = 51200,
                destinationIp = "10.0.0.4",
                destinationPort = 22,
                protocol = "TCP",
                packetSizeBytes = 512,
                threatLevel = "HIGH",
                threatCategory = "SSH Brute-Force Probe",
                actionTaken = "QUARANTINED",
                payloadSnippet = "SSH-2.0-OpenSSH_8.2p1 authentication attempt overflow"
            ),
            NetworkTrafficLogEntity(
                timestampMs = now - 60000,
                timestampFormatted = sdf.format(Date(now - 60000)),
                sourceIp = "192.168.1.105",
                sourcePort = 58210,
                destinationIp = "1.1.1.1",
                destinationPort = 53,
                protocol = "UDP",
                packetSizeBytes = 128,
                threatLevel = "LOW",
                threatCategory = "DNS Query",
                actionTaken = "ALLOWED",
                payloadSnippet = "Standard A record query for security.google.com"
            ),
            NetworkTrafficLogEntity(
                timestampMs = now - 30000,
                timestampFormatted = sdf.format(Date(now - 30000)),
                sourceIp = "10.0.0.2",
                sourcePort = 59001,
                destinationIp = "142.250.190.46",
                destinationPort = 443,
                protocol = "TLS 1.3",
                packetSizeBytes = 3450,
                threatLevel = "SAFE",
                threatCategory = "Encrypted Stream",
                actionTaken = "ALLOWED",
                payloadSnippet = "Kyber1024 Quantum-Safe Session Key Established"
            )
        )

        repository.insertLogs(sampleLogs)
    }
}
