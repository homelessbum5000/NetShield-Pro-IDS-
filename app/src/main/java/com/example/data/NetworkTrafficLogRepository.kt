package com.example.data

import kotlinx.coroutines.flow.Flow

class NetworkTrafficLogRepository(
    private val trafficLogDao: NetworkTrafficLogDao
) {
    val allTrafficLogs: Flow<List<NetworkTrafficLogEntity>> = trafficLogDao.getAllTrafficLogs()
    val totalLogCount: Flow<Int> = trafficLogDao.getTrafficLogCount()
    val highRiskThreatCount: Flow<Int> = trafficLogDao.getHighRiskThreatCount()
    val highRiskTrafficLogs: Flow<List<NetworkTrafficLogEntity>> = trafficLogDao.getHighRiskTrafficLogs()

    fun getLogsByThreatLevel(threatLevel: String): Flow<List<NetworkTrafficLogEntity>> {
        return trafficLogDao.getTrafficLogsByThreatLevel(threatLevel)
    }

    suspend fun insertLog(log: NetworkTrafficLogEntity): Long {
        return trafficLogDao.insertTrafficLog(log)
    }

    suspend fun insertLogs(logs: List<NetworkTrafficLogEntity>) {
        trafficLogDao.insertTrafficLogs(logs)
    }

    suspend fun deleteLogById(id: Long) {
        trafficLogDao.deleteTrafficLogById(id)
    }

    suspend fun clearAllLogs() {
        trafficLogDao.clearAllTrafficLogs()
    }
}
