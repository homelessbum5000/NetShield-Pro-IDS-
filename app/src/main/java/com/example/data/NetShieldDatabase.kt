package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ThreatLogEntity::class, PacketAnalysisEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NetShieldDatabase : RoomDatabase() {

    abstract fun threatLogDao(): ThreatLogDao
    abstract fun packetAnalysisDao(): PacketAnalysisDao

    companion object {
        @Volatile
        private var INSTANCE: NetShieldDatabase? = null

        fun getInstance(context: Context): NetShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NetShieldDatabase::class.java,
                    "netshield_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
