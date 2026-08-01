package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkTrafficLogDao {

    @Query("SELECT * FROM network_traffic_logs ORDER BY timestampMs DESC")
    fun getAllTrafficLogs(): Flow<List<NetworkTrafficLogEntity>>

    @Query("SELECT * FROM network_traffic_logs WHERE threatLevel = :threatLevel ORDER BY timestampMs DESC")
    fun getTrafficLogsByThreatLevel(threatLevel: String): Flow<List<NetworkTrafficLogEntity>>

    @Query("SELECT * FROM network_traffic_logs WHERE threatLevel IN ('CRITICAL', 'HIGH') ORDER BY timestampMs DESC")
    fun getHighRiskTrafficLogs(): Flow<List<NetworkTrafficLogEntity>>

    @Query("SELECT COUNT(*) FROM network_traffic_logs")
    fun getTrafficLogCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM network_traffic_logs WHERE threatLevel IN ('CRITICAL', 'HIGH')")
    fun getHighRiskThreatCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrafficLog(log: NetworkTrafficLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrafficLogs(logs: List<NetworkTrafficLogEntity>)

    @Query("DELETE FROM network_traffic_logs WHERE id = :id")
    suspend fun deleteTrafficLogById(id: Long)

    @Query("DELETE FROM network_traffic_logs")
    suspend fun clearAllTrafficLogs()
}
