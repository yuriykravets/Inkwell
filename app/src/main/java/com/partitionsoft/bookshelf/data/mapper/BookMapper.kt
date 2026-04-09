package com.partitionsoft.bookshelf.data.mapper

import androidx.core.text.HtmlCompat
import com.partitionsoft.bookshelf.data.remote.dto.ItemDto
import com.partitionsoft.bookshelf.data.remote.dto.ImageLinksDto
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.model.optimizedBookCoverUrlOrNull

fun ItemDto.toDomain(): Book = Book(
    id = id.orEmpty(),
    title = volumeInfo?.title ?: "Unknown Title",
    authors = volumeInfo?.authors ?: emptyList(),
    description = volumeInfo?.description?.toPlainText(),
    publishedDate = volumeInfo?.publishedDate,
    categories = volumeInfo?.categories ?: emptyList(),
    rating = volumeInfo?.averageRating,
    ratingsCount = volumeInfo?.ratingsCount ?: 0,
    thumbnail = volumeInfo?.imageLinksDto?.bestAvailableCover().optimizedBookCoverUrlOrNull(),
    previewLink = volumeInfo?.previewLink,
    webReaderLink = accessInfo?.webReaderLink?.toHttps(),
    embeddable = accessInfo?.embeddable ?: false,
    pageCount = volumeInfo?.pageCount,
    language = volumeInfo?.language
)

private fun String.toHttps() = replace("http:", "https:")

private fun ImageLinksDto.bestAvailableCover(): String? =
    extraLarge ?: large ?: medium ?: small ?: thumbnail ?: smallThumbnail

private fun String.toPlainText(): String =
    HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
        .toString()
        .trim()
