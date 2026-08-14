package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThreatRepository(
    private val threatLogDao: ThreatLogDao,
    private val packetAnalysisDao: PacketAnalysisDao,
    private val cryptoManager: RoomAesGcmCryptoManager? = null
) {
    // Flow of decrypted logs for standard UI consumption
    val allThreatLogs: Flow<List<ThreatLogEntity>> = threatLogDao.getAllLogs().map { list ->
        list.map { cryptoManager?.decryptThreatLog(it) ?: it }
    }

    // Flow of raw encrypted logs exactly as persisted in SQLite at rest
    val allRawEncryptedLogs: Flow<List<ThreatLogEntity>> = threatLogDao.getAllLogs()

    val threatLogCount: Flow<Int> = threatLogDao.getLogCount()

    val allPacketAnalyses: Flow<List<PacketAnalysisEntity>> = packetAnalysisDao.getAllAnalyses().map { list ->
        list.map { cryptoManager?.decryptPacketAnalysis(it) ?: it }
    }

    fun getLogsBySeverity(severity: String): Flow<List<ThreatLogEntity>> {
        return threatLogDao.getAllLogs().map { list ->
            list.map { cryptoManager?.decryptThreatLog(it) ?: it }
                .filter { it.severity.equals(severity, ignoreCase = true) }
        }
    }

    suspend fun insertLog(log: ThreatLogEntity): Long {
        val encryptedLog = cryptoManager?.encryptThreatLog(log) ?: log
        return threatLogDao.insertLog(encryptedLog)
    }

    suspend fun insertLogs(logs: List<ThreatLogEntity>) {
        val encryptedLogs = logs.map { cryptoManager?.encryptThreatLog(it) ?: it }
        threatLogDao.insertLogs(encryptedLogs)
    }

    suspend fun deleteLogById(id: Long) {
        threatLogDao.deleteLogById(id)
    }

    suspend fun clearAllLogs() {
        threatLogDao.clearAllLogs()
    }

    suspend fun insertAnalysis(analysis: PacketAnalysisEntity): Long {
        val encrypted = cryptoManager?.encryptPacketAnalysis(analysis) ?: analysis
        return packetAnalysisDao.insertAnalysis(encrypted)
    }

    suspend fun insertAnalyses(analyses: List<PacketAnalysisEntity>) {
        val encrypted = analyses.map { cryptoManager?.encryptPacketAnalysis(it) ?: it }
        packetAnalysisDao.insertAnalyses(encrypted)
    }

    suspend fun clearAllAnalyses() {
        packetAnalysisDao.clearAllAnalyses()
    }
}

