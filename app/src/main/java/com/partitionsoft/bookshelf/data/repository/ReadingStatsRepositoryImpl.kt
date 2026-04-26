package com.partitionsoft.bookshelf.data.repository

import com.partitionsoft.bookshelf.data.local.ReadingSessionDao
import com.partitionsoft.bookshelf.data.local.ReadingSessionEntity
import com.partitionsoft.bookshelf.domain.model.ReadingSessionRecord
import com.partitionsoft.bookshelf.domain.model.ReadingStatsSnapshot
import com.partitionsoft.bookshelf.domain.repository.ReadingStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingStatsRepositoryImpl @Inject constructor(
    private val readingSessionDao: ReadingSessionDao
) : ReadingStatsRepository {

    override fun observeReadingStats(): Flow<ReadingStatsSnapshot> =
        readingSessionDao.observeSessions().map { sessions ->
            val now = System.currentTimeMillis()
            ReadingStatsSnapshot(
                streakDays = ReadingStatsCalculator.calculateStreakDays(sessions = sessions, nowMillis = now),
                todayMinutes = ReadingStatsCalculator.calculateTodayMinutes(sessions = sessions, nowMillis = now),
                booksRead = ReadingStatsCalculator.calculateBooksRead(sessions),
                pagesRead = ReadingStatsCalculator.calculatePagesRead(sessions),
                totalHours = ReadingStatsCalculator.calculateTotalHours(sessions),
                weeklyProgress = ReadingStatsCalculator.buildWeeklyProgress(sessions = sessions, nowMillis = now)
            )
        }

    override suspend fun recordReadingSession(session: ReadingSessionRecord) {
        if (session.durationSeconds <= 0L) return
        readingSessionDao.insertSession(
            ReadingSessionEntity(
                bookRef = session.bookRef,
                bookTitle = session.bookTitle,
                pagesReached = session.pagesReached.coerceAtLeast(0),
                durationSeconds = session.durationSeconds,
                startedAtMillis = session.startedAtMillis,
                endedAtMillis = session.endedAtMillis
            )
        )
    }
}


