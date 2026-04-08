package com.partitionsoft.bookshelf.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BookShelfDto(
    @SerializedName("totalItems") val totalItems: Int? = null,
    @SerializedName("items") val items: List<ItemDto>? = null
)

data class ItemDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("volumeInfo") val volumeInfo: VolumeInfoDto? = null,
    @SerializedName("accessInfo") val accessInfo: AccessInfoDto? = null
)

data class VolumeInfoDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("authors") val authors: List<String>? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("publishedDate") val publishedDate: String? = null,
    @SerializedName("categories") val categories: List<String>? = null,
    @SerializedName("averageRating") val averageRating: Double? = null,
    @SerializedName("ratingsCount") val ratingsCount: Int? = null,
    @SerializedName("imageLinks") val imageLinksDto: ImageLinksDto? = null,
    @SerializedName("previewLink") val previewLink: String? = null,
    @SerializedName("pageCount") val pageCount: Int? = null,
    @SerializedName("language") val language: String? = null
)

data class ImageLinksDto(
    @SerializedName("smallThumbnail") val smallThumbnail: String? = null,
    @SerializedName("thumbnail") val thumbnail: String? = null,
    @SerializedName("small") val small: String? = null,
    @SerializedName("medium") val medium: String? = null,
    @SerializedName("large") val large: String? = null,
    @SerializedName("extraLarge") val extraLarge: String? = null
)

data class AccessInfoDto(
    @SerializedName("webReaderLink") val webReaderLink: String? = null,
    @SerializedName("embeddable") val embeddable: Boolean? = null
)
