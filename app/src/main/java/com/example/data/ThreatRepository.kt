package com.example.data

import kotlinx.coroutines.flow.Flow

class ThreatRepository(
    private val threatLogDao: ThreatLogDao,
    private val packetAnalysisDao: PacketAnalysisDao
) {
    val allThreatLogs: Flow<List<ThreatLogEntity>> = threatLogDao.getAllLogs()
    val threatLogCount: Flow<Int> = threatLogDao.getLogCount()
    val allPacketAnalyses: Flow<List<PacketAnalysisEntity>> = packetAnalysisDao.getAllAnalyses()

    fun getLogsBySeverity(severity: String): Flow<List<ThreatLogEntity>> {
        return threatLogDao.getLogsBySeverity(severity)
    }

    suspend fun insertLog(log: ThreatLogEntity): Long {
        return threatLogDao.insertLog(log)
    }

    suspend fun insertLogs(logs: List<ThreatLogEntity>) {
        threatLogDao.insertLogs(logs)
    }

    suspend fun deleteLogById(id: Long) {
        threatLogDao.deleteLogById(id)
    }

    suspend fun clearAllLogs() {
        threatLogDao.clearAllLogs()
    }

    suspend fun insertAnalysis(analysis: PacketAnalysisEntity): Long {
        return packetAnalysisDao.insertAnalysis(analysis)
    }

    suspend fun insertAnalyses(analyses: List<PacketAnalysisEntity>) {
        packetAnalysisDao.insertAnalyses(analyses)
    }

    suspend fun clearAllAnalyses() {
        packetAnalysisDao.clearAllAnalyses()
    }
}
