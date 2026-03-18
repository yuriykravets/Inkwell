package com.partitionsoft.bookshelf.data.local

import androidx.room.ColumnInfo

data class ReaderDocumentWithProgress(
    val id: Long,
    val title: String,
    val uri: String,
    val format: String,
    val mimeType: String?,
    val addedAtMillis: Long,
    @ColumnInfo(name = "lastLocation") val lastLocation: String?
)

