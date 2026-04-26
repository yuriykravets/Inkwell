package com.partitionsoft.bookshelf.data.repository

import com.partitionsoft.bookshelf.data.local.ReadingSessionEntity
import com.partitionsoft.bookshelf.domain.model.WeeklyReadingDay
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.DateFormatSymbols
import kotlin.math.roundToInt

internal object ReadingStatsCalculator {

    fun calculateStreakDays(
        sessions: List<ReadingSessionEntity>,
        nowMillis: Long
    ): Int {
        val distinctDays = sessions
            .asSequence()
            .map { dayKey(it.endedAtMillis) }
            .distinct()
            .sortedDescending()
            .toList()

        val latestReadDay = distinctDays.firstOrNull() ?: return 0
        val yesterday = dayKey(nowMillis - MILLIS_PER_DAY)
        if (latestReadDay < yesterday) return 0

        var streak = 1
        var cursor = latestReadDay
        for (index in 1 until distinctDays.size) {
            val day = distinctDays[index]
            if (day == previousDayKey(cursor)) {
                streak += 1
                cursor = day
            } else {
                break
            }
        }
        return streak
    }

    fun calculateTodayMinutes(
        sessions: List<ReadingSessionEntity>,
        nowMillis: Long
    ): Int {
        val today = dayKey(nowMillis)
        val seconds = sessions
            .asSequence()
            .filter { dayKey(it.endedAtMillis) == today }
            .sumOf { it.durationSeconds }
        return (seconds / 60.0).roundToInt()
    }

    fun buildWeeklyProgress(
        sessions: List<ReadingSessionEntity>,
        nowMillis: Long,
        locale: Locale = Locale.getDefault()
    ): List<WeeklyReadingDay> {
        val today = dayKey(nowMillis)
        val minutesByDay = sessions
            .groupBy { dayKey(it.endedAtMillis) }
            .mapValues { (_, daySessions) ->
                (daySessions.sumOf { it.durationSeconds } / 60.0).roundToInt()
            }

        return (6 downTo 0).map { offset ->
            val day = dayKey(nowMillis - (offset * MILLIS_PER_DAY))
            WeeklyReadingDay(
                label = dayLabel(day, locale),
                minutesRead = minutesByDay[day] ?: 0,
                isToday = day == today
            )
        }
    }

    fun calculateBooksRead(sessions: List<ReadingSessionEntity>): Int =
        sessions
            .asSequence()
            .filter { it.pagesReached > 0 }
            .map { it.bookRef }
            .distinct()
            .count()

    fun calculatePagesRead(sessions: List<ReadingSessionEntity>): Int =
        sessions
            .groupBy { it.bookRef }
            .values
            .sumOf { bookSessions -> bookSessions.maxOfOrNull { it.pagesReached } ?: 0 }

    fun calculateTotalHours(sessions: List<ReadingSessionEntity>): Double {
        val totalSeconds = sessions.sumOf { it.durationSeconds }
        return ((totalSeconds / 3600.0) * 10).roundToInt() / 10.0
    }

    private fun dayKey(millis: Long): Int {
        val calendar = Calendar.getInstance().apply { timeInMillis = millis }
        return calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun previousDayKey(dayKey: Int): Int {
        val year = dayKey / 1000
        val dayOfYear = dayKey % 1000
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.DAY_OF_YEAR, dayOfYear)
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun dayLabel(dayKey: Int, locale: Locale): String {
        val year = dayKey / 1000
        val dayOfYear = dayKey % 1000
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.DAY_OF_YEAR, dayOfYear)
        }
        val dayIndex = calendar.get(Calendar.DAY_OF_WEEK)
        return DateFormatSymbols(locale).shortWeekdays[dayIndex].ifBlank {
            Date(dayKey.toLong()).toString()
        }
    }

    private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
}



