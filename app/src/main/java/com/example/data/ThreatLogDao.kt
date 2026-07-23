package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreatLogDao {
    @Query("SELECT * FROM threat_logs ORDER BY timestampMs DESC")
    fun getAllLogs(): Flow<List<ThreatLogEntity>>

    @Query("SELECT * FROM threat_logs WHERE severity = :severity ORDER BY timestampMs DESC")
    fun getLogsBySeverity(severity: String): Flow<List<ThreatLogEntity>>

    @Query("SELECT COUNT(*) FROM threat_logs")
    fun getLogCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ThreatLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<ThreatLogEntity>)

    @Query("DELETE FROM threat_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM threat_logs")
    suspend fun clearAllLogs()
}
