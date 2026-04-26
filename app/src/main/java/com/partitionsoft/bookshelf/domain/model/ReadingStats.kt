package com.partitionsoft.bookshelf.domain.model

data class ReadingSessionRecord(
    val bookRef: String,
    val bookTitle: String?,
    val pagesReached: Int,
    val durationSeconds: Long,
    val startedAtMillis: Long,
    val endedAtMillis: Long
)

data class WeeklyReadingDay(
    val label: String,
    val minutesRead: Int,
    val isToday: Boolean
)

data class ReadingStatsSnapshot(
    val streakDays: Int,
    val todayMinutes: Int,
    val booksRead: Int,
    val pagesRead: Int,
    val totalHours: Double,
    val weeklyProgress: List<WeeklyReadingDay>
)

