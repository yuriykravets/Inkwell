package com.partitionsoft.bookshelf.data.mapper

import com.partitionsoft.bookshelf.data.remote.dto.ItemDto
import com.partitionsoft.bookshelf.domain.model.Book

fun ItemDto.toDomain(): Book = Book(
    id = id.orEmpty(),
    title = volumeInfo?.title ?: "Unknown Title",
    authors = volumeInfo?.authors ?: emptyList(),
    description = volumeInfo?.description,
    publishedDate = volumeInfo?.publishedDate,
    categories = volumeInfo?.categories ?: emptyList(),
    rating = volumeInfo?.averageRating,
    ratingsCount = volumeInfo?.ratingsCount ?: 0,
    thumbnail = volumeInfo?.imageLinksDto?.thumbnail?.toHttps(),
    previewLink = volumeInfo?.previewLink,
    pageCount = volumeInfo?.pageCount,
    language = volumeInfo?.language
)

private fun String.toHttps() = replace("http:", "https:")