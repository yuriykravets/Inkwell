package com.partitionsoft.bookshelf.data.mapper

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.partitionsoft.bookshelf.data.remote.dto.OpenLibraryDocDto
import com.partitionsoft.bookshelf.data.remote.dto.OpenLibraryWorkDto
import com.partitionsoft.bookshelf.domain.model.Book

private const val OPEN_LIBRARY_ID_PREFIX = "ol:"
private const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org"
private const val OPEN_LIBRARY_COVERS_BASE_URL = "https://covers.openlibrary.org/b/id/"

fun OpenLibraryDocDto.toDomain(): Book {
    val workKey = key.orEmpty()
    val workId = workKey.substringAfterLast('/')

    return Book(
        id = OPEN_LIBRARY_ID_PREFIX + workId,
        title = title ?: "Unknown Title",
        authors = authorName ?: emptyList(),
        description = null,
        publishedDate = firstPublishYear?.toString(),
        categories = subject ?: emptyList(),
        rating = null,
        ratingsCount = 0,
        thumbnail = coverId?.let { "$OPEN_LIBRARY_COVERS_BASE_URL$it-L.jpg" },
        previewLink = workKey.takeIf { it.isNotBlank() }?.let { "$OPEN_LIBRARY_BASE_URL$it" },
        webReaderLink = null,
        embeddable = false,
        pageCount = numberOfPagesMedian,
        language = language?.firstOrNull(),
        isFavorite = false
    )
}

fun OpenLibraryWorkDto.toDomain(workId: String): Book = Book(
    id = OPEN_LIBRARY_ID_PREFIX + workId,
    title = title ?: "Unknown Title",
    authors = emptyList(),
    description = description.toReadableText(),
    publishedDate = firstPublishDate,
    categories = subjects ?: emptyList(),
    rating = null,
    ratingsCount = 0,
    thumbnail = covers?.firstOrNull()?.let { "$OPEN_LIBRARY_COVERS_BASE_URL$it-L.jpg" },
    previewLink = "$OPEN_LIBRARY_BASE_URL/works/$workId",
    webReaderLink = null,
    embeddable = false,
    pageCount = null,
    language = languages?.firstOrNull()?.key?.substringAfterLast('/'),
    isFavorite = false
)

fun String.isOpenLibraryBookId(): Boolean = startsWith(OPEN_LIBRARY_ID_PREFIX)

fun String.toOpenLibraryWorkId(): String = removePrefix(OPEN_LIBRARY_ID_PREFIX)

private fun JsonElement?.toReadableText(): String? {
    if (this == null || isJsonNull) return null

    return when {
        isJsonPrimitive -> {
            val primitive = this as JsonPrimitive
            if (primitive.isString) primitive.asString else null
        }
        isJsonObject -> asJsonObject.get("value")?.takeIf { it.isJsonPrimitive }?.asString
        else -> null
    }?.trim()?.takeIf { it.isNotBlank() }
}

