package com.partitionsoft.bookshelf.data.repository

import com.partitionsoft.bookshelf.domain.model.Book

internal const val ORDER_BY_POPULAR = "popular"

internal fun List<Book>.sortedByPopularity(): List<Book> {
    if (size < 2) return this
    return sortedWith(POPULARITY_COMPARATOR)
}

private val POPULARITY_COMPARATOR = compareByDescending<Book> { it.ratingsCount }
    .thenByDescending { it.rating ?: 0.0 }
    .thenByDescending { it.publishedDate.toPublishedYear() }
    .thenBy { it.title.lowercase() }

private fun String?.toPublishedYear(): Int {
    if (this.isNullOrBlank()) return 0
    return YEAR_REGEX.find(this)?.value?.toIntOrNull() ?: 0
}

private val YEAR_REGEX = Regex("\\d{4}")

