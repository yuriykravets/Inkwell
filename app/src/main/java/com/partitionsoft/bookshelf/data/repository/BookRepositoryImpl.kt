package com.partitionsoft.bookshelf.data.repository

import com.partitionsoft.bookshelf.data.mapper.toDomain
import com.partitionsoft.bookshelf.data.remote.api.BookService
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.model.BookCategory
import com.partitionsoft.bookshelf.domain.model.BookSection
import com.partitionsoft.bookshelf.domain.model.HomeFeed
import com.partitionsoft.bookshelf.domain.model.SectionLayout
import com.partitionsoft.bookshelf.domain.repository.BookRepository
import com.partitionsoft.bookshelf.domain.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
        emit(safeApiCall { fetchBooks(query = query, maxResults = maxResults) })
    }.flowOn(Dispatchers.IO)

    override fun getBooksByCategory(
        categoryQuery: String,
        maxResults: Int
    ): Flow<Result<List<Book>>> = flow {
        emit(Result.Loading)
        emit(
            safeApiCall {
                val normalizedQuery = categoryQuery.takeIf { it.startsWith(SUBJECT_QUERY_PREFIX) }
                    ?: (SUBJECT_QUERY_PREFIX + categoryQuery)
                fetchBooks(query = normalizedQuery, maxResults = maxResults)
            }
        )
    }.flowOn(Dispatchers.IO)

    override fun getBookDetails(bookId: String): Flow<Result<Book>> = flow {
        emit(Result.Loading)
        emit(safeApiCall { bookService.getBookById(bookId).toDomain() })
    }.flowOn(Dispatchers.IO)

    override fun observeHomeFeed(): Flow<Result<HomeFeed>> = flow {
        emit(Result.Loading)
        emit(safeApiCall { buildHomeFeed() })
    }.flowOn(Dispatchers.IO)

    private suspend fun buildHomeFeed(): HomeFeed = coroutineScope {
        val featuredDeferred = async {
            fetchBooks(
                query = FEATURED_SECTION.query,
                maxResults = FEATURED_SECTION.maxResults,
                orderBy = FEATURED_SECTION.orderBy,
                filter = FEATURED_SECTION.filter
            )
        }

        val sections = SECTION_CONFIGS.map { config ->
            async {
                val books = fetchBooks(
                    query = config.query,
                    maxResults = config.maxResults,
                    orderBy = config.orderBy,
                    filter = config.filter
                )
                BookSection(
                    id = config.id,
                    title = config.title,
                    layout = config.layout,
                    books = books
                )
            }
        }.awaitAll()

        HomeFeed(
            featured = featuredDeferred.await(),
            categories = CATEGORIES,
            sections = sections.filter { it.books.isNotEmpty() }
        )
    }

    private suspend fun fetchBooks(
        query: String,
        maxResults: Int,
        orderBy: String? = null,
        filter: String? = null
    ): List<Book> = bookService.searchBooks(
        query = query,
        maxResults = maxResults,
        orderBy = orderBy,
        filter = filter
    ).items?.map { it.toDomain() } ?: emptyList()

    private suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> =
        try {
            Result.Success(call())
        } catch (e: IOException) {
            Result.Error(e)
        } catch (e: HttpException) {
            Result.Error(e)
        }

    private data class SectionConfig(
        val id: String,
        val title: String,
        val query: String,
        val maxResults: Int,
        val layout: SectionLayout,
        val orderBy: String? = null,
        val filter: String? = null
    )

    private companion object {
        private const val SUBJECT_QUERY_PREFIX = "subject:"

        private val FEATURED_SECTION = SectionConfig(
            id = "featured",
            title = "Featured",
            query = "bestseller",
            maxResults = 10,
            layout = SectionLayout.Carousel,
            orderBy = "newest",
            filter = "ebooks"
        )

        private val SECTION_CONFIGS = listOf(
            SectionConfig(
                id = "android_trending",
                title = "Trending on Android",
                query = "android development",
                maxResults = 10,
                layout = SectionLayout.Horizontal
            ),
            SectionConfig(
                id = "design_spotlight",
                title = "Design Spotlight",
                query = "design thinking",
                maxResults = 10,
                layout = SectionLayout.Horizontal
            ),
            SectionConfig(
                id = "business_moves",
                title = "Business & Finance",
                query = "subject:business",
                maxResults = 10,
                layout = SectionLayout.Horizontal
            ),
            SectionConfig(
                id = "science_breakthroughs",
                title = "Science Breakthroughs",
                query = "subject:science",
                maxResults = 10,
                layout = SectionLayout.Horizontal,
                orderBy = "newest"
            )
        )

        private val CATEGORIES = listOf(
            BookCategory(id = "fiction", title = "Fiction", query = "subject:fiction"),
            BookCategory(id = "mystery", title = "Mystery", query = "subject:mystery"),
            BookCategory(id = "romance", title = "Romance", query = "subject:romance"),
            BookCategory(id = "history", title = "History", query = "subject:history"),
            BookCategory(id = "science", title = "Science", query = "subject:science")
        )
    }
}