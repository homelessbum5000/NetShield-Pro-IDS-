package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_traffic_logs")
data class NetworkTrafficLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long = System.currentTimeMillis(),
    val timestampFormatted: String,
    val sourceIp: String,
    val sourcePort: Int,
    val destinationIp: String,
    val destinationPort: Int,
    val protocol: String, // e.g., TCP, UDP, TLS 1.3, QUIC, DNS, HTTP/3
    val packetSizeBytes: Long,
    val threatLevel: String, // CRITICAL, HIGH, MEDIUM, LOW, SAFE
    val threatCategory: String, // e.g., Port Scan, DDoS Flood, SQLi, Exfiltration, Normal Traffic
    val actionTaken: String, // BLOCKED, QUARANTINED, ALLOWED, FLAGGED
    val payloadSnippet: String = ""
)
