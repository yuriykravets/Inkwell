package com.partitionsoft.bookshelf.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reader_progress")
data class ReaderProgressEntity(
    @PrimaryKey val documentId: Long,
    val location: String,
    val updatedAtMillis: Long
)

