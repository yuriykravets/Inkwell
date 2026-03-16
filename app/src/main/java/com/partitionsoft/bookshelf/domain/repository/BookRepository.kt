package com.partitionsoft.bookshelf.domain.repository

import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.model.BookCategory
import com.partitionsoft.bookshelf.domain.model.HomeFeed
import com.partitionsoft.bookshelf.domain.result.Result
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    fun searchBooks(query: String, maxResults: Int = 40): Flow<Result<List<Book>>>

    fun getBooksByCategory(categoryQuery: String, maxResults: Int = 10): Flow<Result<List<Book>>>

    fun getBookDetails(bookId: String): Flow<Result<Book>>

    fun observeHomeFeed(): Flow<Result<HomeFeed>>

    fun observePagedBooks(
        query: String,
        orderBy: String? = null,
        filter: String? = null,
        pageSize: Int = 15
    ): Flow<PagingData<Book>>

    fun observePagedCategoryBooks(
        category: BookCategory,
        pageSize: Int = 15
    ): Flow<PagingData<Book>>
}