package com.partitionsoft.bookshelf.data.mapper

import com.partitionsoft.bookshelf.data.local.FavoriteBookEntity
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.model.optimizedBookCoverUrlOrNull

private const val LIST_DELIMITER = "\u001F"

fun Book.toFavoriteEntity(savedAtMillis: Long = System.currentTimeMillis()): FavoriteBookEntity =
    FavoriteBookEntity(
        id = id,
        title = title,
        authors = authors.joinToString(separator = LIST_DELIMITER),
        description = description,
        publishedDate = publishedDate,
        categories = categories.joinToString(separator = LIST_DELIMITER),
        rating = rating,
        ratingsCount = ratingsCount,
        thumbnail = thumbnail.optimizedBookCoverUrlOrNull(),
        previewLink = previewLink,
        webReaderLink = webReaderLink,
        embeddable = embeddable,
        pageCount = pageCount,
        language = language,
        savedAtMillis = savedAtMillis
    )

fun FavoriteBookEntity.toDomain(): Book = Book(
    id = id,
    title = title,
    authors = authors.splitList(),
    description = description,
    publishedDate = publishedDate,
    categories = categories.splitList(),
    rating = rating,
    ratingsCount = ratingsCount,
    thumbnail = thumbnail.optimizedBookCoverUrlOrNull(),
    previewLink = previewLink,
    webReaderLink = webReaderLink,
    embeddable = embeddable,
    pageCount = pageCount,
    language = language,
    isFavorite = true
)

private fun String.splitList(): List<String> =
    takeIf { it.isNotBlank() }
        ?.split(LIST_DELIMITER)
        ?.filter { it.isNotBlank() }
        ?: emptyList()

