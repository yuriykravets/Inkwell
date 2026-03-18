package com.partitionsoft.bookshelf.domain.model

enum class ReaderDocumentFormat {
    PDF,
    EPUB,
    FB2,
    UNKNOWN
}

data class ReaderDocument(
    val id: Long,
    val title: String,
    val uri: String,
    val format: ReaderDocumentFormat,
    val mimeType: String?,
    val addedAtMillis: Long,
    val lastLocation: String? = null
)

