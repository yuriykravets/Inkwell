package com.partitionsoft.bookshelf.domain.model

data class Book(
    val id: String,
    val title: String,
    val authors: List<String>,
    val description: String?,
    val publishedDate: String?,
    val categories: List<String>,
    val rating: Double?,
    val ratingsCount: Int,
    val thumbnail: String?,
    val previewLink: String?,
    val webReaderLink: String? = null,
    val embeddable: Boolean = false,
    val pageCount: Int?,
    val language: String?,
    val isFavorite: Boolean = false
)