package com.partitionsoft.bookshelf.data.repository

import com.partitionsoft.bookshelf.data.mapper.toDomain
import com.partitionsoft.bookshelf.data.remote.api.BookService
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.repository.BookRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookService: BookService
) : BookRepository {
    override suspend fun searchBooks(
        query: String,
        maxResults: Int
    ): List<Book> =
        bookService
            .searchBooks(query, maxResults)
            .items
            ?.map { it.toDomain() }
            ?: emptyList()
}