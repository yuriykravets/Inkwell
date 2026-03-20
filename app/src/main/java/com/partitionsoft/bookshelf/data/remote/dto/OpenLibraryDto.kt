package com.partitionsoft.bookshelf.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class OpenLibrarySearchDto(
    @SerializedName("numFound") val numFound: Int? = null,
    @SerializedName("docs") val docs: List<OpenLibraryDocDto>? = null
)

data class OpenLibraryDocDto(
    @SerializedName("key") val key: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("author_name") val authorName: List<String>? = null,
    @SerializedName("first_publish_year") val firstPublishYear: Int? = null,
    @SerializedName("subject") val subject: List<String>? = null,
    @SerializedName("cover_i") val coverId: Int? = null,
    @SerializedName("language") val language: List<String>? = null,
    @SerializedName("number_of_pages_median") val numberOfPagesMedian: Int? = null
)

data class OpenLibraryWorkDto(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: JsonElement? = null,
    @SerializedName("subjects") val subjects: List<String>? = null,
    @SerializedName("covers") val covers: List<Int>? = null,
    @SerializedName("first_publish_date") val firstPublishDate: String? = null,
    @SerializedName("languages") val languages: List<OpenLibraryLanguageDto>? = null
)

data class OpenLibraryLanguageDto(
    @SerializedName("key") val key: String? = null
)

