package com.partitionsoft.bookshelf.domain.repository

import com.partitionsoft.bookshelf.domain.model.ReadingSessionRecord
import com.partitionsoft.bookshelf.domain.model.ReadingStatsSnapshot
import kotlinx.coroutines.flow.Flow

interface ReadingStatsRepository {
    fun observeReadingStats(): Flow<ReadingStatsSnapshot>

    suspend fun recordReadingSession(session: ReadingSessionRecord)
}

