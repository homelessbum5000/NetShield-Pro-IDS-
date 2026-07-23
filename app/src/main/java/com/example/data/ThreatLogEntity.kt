package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "threat_logs")
data class ThreatLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long = System.currentTimeMillis(),
    val timestampIso: String,
    val severity: String, // CRITICAL, HIGH, MEDIUM, LOW
    val attackVector: String,
    val sourceIp: String,
    val targetPort: Int,
    val actionTaken: String,
    val quantumKeyId: String,
    val details: String = ""
)
