package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packet_analyses")
data class PacketAnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long = System.currentTimeMillis(),
    val totalPackets: Long,
    val protocol: String, // TCP, UDP, HTTP/2, QUIC, TLS
    val entropyScore: Double,
    val bytesAnalyzed: Long,
    val flaggedCount: Int,
    val summary: String
)
