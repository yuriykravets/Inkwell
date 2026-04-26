package com.partitionsoft.bookshelf.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_sessions",
    indices = [
        Index(value = ["bookRef"]),
        Index(value = ["endedAtMillis"])
    ]
)
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookRef: String,
    val bookTitle: String?,
    val pagesReached: Int,
    val durationSeconds: Long,
    val startedAtMillis: Long,
    val endedAtMillis: Long
)

