package com.partitionsoft.bookshelf.data.remote.api

import com.partitionsoft.bookshelf.data.remote.dto.OpenLibrarySearchDto
import com.partitionsoft.bookshelf.data.remote.dto.OpenLibraryWorkDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryService {

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): OpenLibrarySearchDto

    @GET("works/{workId}.json")
    suspend fun getWorkById(
        @Path("workId") workId: String
    ): OpenLibraryWorkDto
}

