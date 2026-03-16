package com.partitionsoft.bookshelf.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.partitionsoft.bookshelf.data.mapper.toDomain
import com.partitionsoft.bookshelf.data.remote.api.BookService
import com.partitionsoft.bookshelf.domain.model.Book
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.max

class BooksPagingSource(
    private val bookService: BookService,
    private val query: String,
    private val orderBy: String? = null,
    private val filter: String? = null
) : PagingSource<Int, Book>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val startIndex = params.key ?: 0
        val requestedSize = params.loadSize.coerceAtMost(MAX_RESULTS_PER_REQUEST)

        return try {
            val response = bookService.searchBooks(
                query = query,
                startIndex = startIndex,
                maxResults = requestedSize,
                orderBy = orderBy,
                filter = filter
            )
            val books = response.items.orEmpty().map { it.toDomain() }
            val totalItems = response.totalItems ?: 0
            val nextIndex = startIndex + books.size

            LoadResult.Page(
                data = books,
                prevKey = if (startIndex == 0) null else max(startIndex - requestedSize, 0),
                nextKey = if (books.isEmpty() || nextIndex >= totalItems) null else nextIndex
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
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
    }
}

