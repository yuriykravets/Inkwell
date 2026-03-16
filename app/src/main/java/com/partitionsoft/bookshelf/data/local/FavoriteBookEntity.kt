package com.partitionsoft.bookshelf.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_books")
data class FavoriteBookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authors: String,
    val description: String?,
    val publishedDate: String?,
    val categories: String,
    val rating: Double?,
    val ratingsCount: Int,
    val thumbnail: String?,
    val previewLink: String?,
    val webReaderLink: String?,
    val embeddable: Boolean,
    val pageCount: Int?,
    val language: String?,
    val savedAtMillis: Long
)

