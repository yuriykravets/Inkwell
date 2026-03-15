package com.partitionsoft.bookshelf.domain.repository

import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.result.Result
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    fun searchBooks(query: String, maxResults: Int = 40): Flow<Result<List<Book>>>

}