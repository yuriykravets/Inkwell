package com.partitionsoft.bookshelf.data.mapper

import com.partitionsoft.bookshelf.data.local.ReaderDocumentEntity
import com.partitionsoft.bookshelf.data.local.ReaderDocumentWithProgress
import com.partitionsoft.bookshelf.domain.model.ReaderDocument
import com.partitionsoft.bookshelf.domain.model.ReaderDocumentFormat

fun ReaderDocumentWithProgress.toDomain(): ReaderDocument = ReaderDocument(
    id = id,
    title = title,
    uri = uri,
    format = ReaderDocumentFormat.entries.firstOrNull { it.name == format }
        ?: ReaderDocumentFormat.UNKNOWN,
    mimeType = mimeType,
    addedAtMillis = addedAtMillis,
    lastLocation = lastLocation
)

fun ReaderDocumentEntity.toDomain(lastLocation: String?): ReaderDocument = ReaderDocument(
    id = id,
    title = title,
    uri = uri,
    format = ReaderDocumentFormat.entries.firstOrNull { it.name == format }
        ?: ReaderDocumentFormat.UNKNOWN,
    mimeType = mimeType,
    addedAtMillis = addedAtMillis,
    lastLocation = lastLocation
)

