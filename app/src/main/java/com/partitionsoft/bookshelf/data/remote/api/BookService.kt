package com.partitionsoft.bookshelf.data.remote.api

import com.partitionsoft.bookshelf.data.remote.dto.BookShelfDto
import retrofit2.http.GET
import retrofit2.http.Query

interface BookService {

    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 40,
        @Query("orderBy") orderBy: String? = null,
        @Query("filter") filter: String? = null,
        @Query("printType") printType: String = "books"
    ): BookShelfDto
}