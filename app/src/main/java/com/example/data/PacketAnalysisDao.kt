package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PacketAnalysisDao {
    @Query("SELECT * FROM packet_analyses ORDER BY timestampMs DESC")
    fun getAllAnalyses(): Flow<List<PacketAnalysisEntity>>

    @Query("SELECT * FROM packet_analyses WHERE protocol = :protocol ORDER BY timestampMs DESC")
    fun getAnalysesByProtocol(protocol: String): Flow<List<PacketAnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: PacketAnalysisEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyses(analyses: List<PacketAnalysisEntity>)

    @Query("DELETE FROM packet_analyses")
    suspend fun clearAllAnalyses()
}
