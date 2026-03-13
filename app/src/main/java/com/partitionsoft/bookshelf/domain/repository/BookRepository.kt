package com.partitionsoft.bookshelf.domain.repository

import com.partitionsoft.bookshelf.domain.model.Book

interface BookRepository {

    suspend fun searchBooks(query: String, maxResults: Int = 40): List<Book>

}