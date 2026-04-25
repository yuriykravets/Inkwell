package com.partitionsoft.bookshelf.data.repository

import com.partitionsoft.bookshelf.data.local.ReadingSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ReadingStatsCalculatorTest {

    @Test
    fun `streak is alive when user read yesterday but not yet today`() {
        withUtcTimeZone {
            val now = utcMillis(2026, Calendar.APRIL, 18, 10, 0)
            val sessions = listOf(
                session("book-1", endedAt = utcMillis(2026, Calendar.APRIL, 17, 20, 0)),
                session("book-2", endedAt = utcMillis(2026, Calendar.APRIL, 16, 20, 0))
            )

            val streak = ReadingStatsCalculator.calculateStreakDays(sessions = sessions, nowMillis = now)

            assertEquals(2, streak)
        }
    }

    @Test
    fun `streak resets when there is a gap larger than one day`() {
        withUtcTimeZone {
            val now = utcMillis(2026, Calendar.APRIL, 18, 10, 0)
            val sessions = listOf(
                session("book-1", endedAt = utcMillis(2026, Calendar.APRIL, 15, 20, 0))
            )

            val streak = ReadingStatsCalculator.calculateStreakDays(sessions = sessions, nowMillis = now)

            assertEquals(0, streak)
        }
    }

    @Test
    fun `weekly progress includes summed minutes per day`() {
        withUtcTimeZone {
            val now = utcMillis(2026, Calendar.APRIL, 18, 10, 0)
            val sessions = listOf(
                session("book-1", durationSeconds = 1200, endedAt = utcMillis(2026, Calendar.APRIL, 18, 9, 0)),
                session("book-1", durationSeconds = 600, endedAt = utcMillis(2026, Calendar.APRIL, 18, 11, 0)),
                session("book-2", durationSeconds = 900, endedAt = utcMillis(2026, Calendar.APRIL, 17, 13, 0))
            )

            val weekly = ReadingStatsCalculator.buildWeeklyProgress(sessions = sessions, nowMillis = now)

            assertEquals(7, weekly.size)
            assertEquals(30, weekly.last().minutesRead)
            assertEquals(15, weekly[5].minutesRead)
        }
    }

    private fun session(
        bookRef: String,
        durationSeconds: Long = 600,
        endedAt: Long
    ) = ReadingSessionEntity(
        bookRef = bookRef,
        bookTitle = null,
        pagesReached = 10,
        durationSeconds = durationSeconds,
        startedAtMillis = endedAt - durationSeconds * 1000,
        endedAtMillis = endedAt
    )

    private fun utcMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun withUtcTimeZone(block: () -> Unit) {
        val previous = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            block()
        } finally {
            TimeZone.setDefault(previous)
        }
    }
}

