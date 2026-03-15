package com.partitionsoft.bookshelf.data.repository

import com.partitionsoft.bookshelf.data.mapper.toDomain
import com.partitionsoft.bookshelf.data.remote.api.BookService
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.repository.BookRepository
import com.partitionsoft.bookshelf.domain.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookService: BookService
) : BookRepository {
    override fun searchBooks(
        query: String,
        maxResults: Int
    ): Flow<Result<List<Book>>> = flow {
        emit(Result.Loading)

        try {
            val books = bookService
                .searchBooks(query, maxResults)
                .items
                ?.map { it.toDomain() }
                ?: emptyList()
            emit(Result.Success(books))
        } catch (e: IOException) {
            emit(Result.Error(e))
        } catch (e: HttpException) {
            emit(Result.Error(e))
        }
    }.flowOn(Dispatchers.IO)

}