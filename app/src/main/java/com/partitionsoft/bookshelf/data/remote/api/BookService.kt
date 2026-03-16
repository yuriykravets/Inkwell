package com.partitionsoft.bookshelf.data.remote.api

import com.partitionsoft.bookshelf.data.remote.dto.BookShelfDto
import com.partitionsoft.bookshelf.data.remote.dto.ItemDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BookService {

    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("startIndex") startIndex: Int = 0,
        @Query("maxResults") maxResults: Int = 40,
        @Query("orderBy") orderBy: String? = null,
        @Query("filter") filter: String? = null,
        @Query("printType") printType: String = "books"
    ): BookShelfDto

    @GET("volumes/{id}")
    suspend fun getBookById(
        @Path("id") id: String
    ): ItemDto
}