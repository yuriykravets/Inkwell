package com.partitionsoft.bookshelf.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReadingSessionEntity)

    @Query("SELECT * FROM reading_sessions ORDER BY endedAtMillis DESC")
    fun observeSessions(): Flow<List<ReadingSessionEntity>>

    @Query("SELECT * FROM reading_sessions WHERE endedAtMillis >= :fromMillis ORDER BY endedAtMillis DESC")
    fun observeSessionsFrom(fromMillis: Long): Flow<List<ReadingSessionEntity>>
}

