package com.partitionsoft.bookshelf.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.partitionsoft.bookshelf.data.mapper.toDomain
import com.partitionsoft.bookshelf.data.repository.ORDER_BY_POPULAR
import com.partitionsoft.bookshelf.data.repository.sortedByPopularity
import com.partitionsoft.bookshelf.data.remote.api.BookService
import com.partitionsoft.bookshelf.data.remote.api.OpenLibraryService
import com.partitionsoft.bookshelf.domain.model.Book
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.max

class BooksPagingSource(
    private val bookService: BookService,
    private val openLibraryService: OpenLibraryService,
    private val query: String,
    private val orderBy: String? = null,
    private val filter: String? = null,
    private val favoriteIdsProvider: suspend () -> Set<String>
) : PagingSource<Int, Book>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val startIndex = params.key ?: 0
        val requestedSize = params.loadSize.coerceAtMost(MAX_RESULTS_PER_REQUEST)
        val googleOrderBy = if (orderBy == ORDER_BY_POPULAR) null else orderBy

        return try {
            val response = bookService.searchBooks(
                query = query,
                startIndex = startIndex,
                maxResults = requestedSize,
                orderBy = googleOrderBy,
                filter = filter
            )
            val favoriteIds = favoriteIdsProvider()
            val googleBooks = response.items.orEmpty().map { item ->
                val book = item.toDomain()
                book.copy(isFavorite = favoriteIds.contains(book.id))
            }.let { books ->
                if (orderBy == ORDER_BY_POPULAR) books.sortedByPopularity() else books
            }
            if (googleBooks.isNotEmpty()) {
                val totalItems = response.totalItems ?: 0
                val nextIndex = startIndex + googleBooks.size

                return LoadResult.Page(
                    data = googleBooks,
                    prevKey = if (startIndex == 0) null else max(startIndex - requestedSize, 0),
                    nextKey = if (nextIndex >= totalItems) null else nextIndex
                )
            }

            loadFromOpenLibrary(startIndex = startIndex, requestedSize = requestedSize)
        } catch (_: IOException) {
            loadFromOpenLibrary(startIndex = startIndex, requestedSize = requestedSize)
        } catch (e: HttpException) {
            if (e.code() == 429) {
                loadFromOpenLibrary(startIndex = startIndex, requestedSize = requestedSize)
            } else {
                LoadResult.Error(e)
            }
        }
    }

    private suspend fun loadFromOpenLibrary(
        startIndex: Int,
        requestedSize: Int
    ): LoadResult<Int, Book> {
        return try {
            val openLibraryPageSize = requestedSize.coerceAtMost(MAX_OPEN_LIBRARY_RESULTS_PER_REQUEST)
            val page = (startIndex / openLibraryPageSize) + 1
            val response = openLibraryService.searchBooks(
                query = query.removePrefix(SUBJECT_QUERY_PREFIX),
                page = page,
                limit = openLibraryPageSize
            )
            val favoriteIds = favoriteIdsProvider()
            val books = response.docs.orEmpty().map { doc ->
                val book = doc.toDomain()
                book.copy(isFavorite = favoriteIds.contains(book.id))
            }.let { mappedBooks ->
                if (orderBy == ORDER_BY_POPULAR) mappedBooks.sortedByPopularity() else mappedBooks
            }
            val totalItems = response.numFound ?: 0
            val nextIndex = startIndex + books.size

            LoadResult.Page(
                data = books,
                prevKey = if (startIndex == 0) null else max(startIndex - requestedSize, 0),
                nextKey = if (books.isEmpty() || nextIndex >= totalItems) null else nextIndex
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val closestPage = state.closestPageToPosition(anchorPosition) ?: return null
        return closestPage.prevKey?.plus(state.config.pageSize)
            ?: closestPage.nextKey?.minus(state.config.pageSize)
    }

    private companion object {
        private const val MAX_RESULTS_PER_REQUEST = 40
        private const val MAX_OPEN_LIBRARY_RESULTS_PER_REQUEST = 20
        private const val SUBJECT_QUERY_PREFIX = "subject:"
    }
}

