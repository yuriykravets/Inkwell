package com.partitionsoft.bookshelf.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reader_documents",
    indices = [Index(value = ["uri"], unique = true)]
)
data class ReaderDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val uri: String,
    val format: String,
    val mimeType: String?,
    val addedAtMillis: Long
)

