package com.partitionsoft.bookshelf.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.partitionsoft.bookshelf.data.local.FavoriteBookDao
import com.partitionsoft.bookshelf.data.mapper.isOpenLibraryBookId
import com.partitionsoft.bookshelf.data.mapper.toOpenLibraryWorkId
import com.partitionsoft.bookshelf.data.mapper.toFavoriteEntity
import com.partitionsoft.bookshelf.data.mapper.toDomain
import com.partitionsoft.bookshelf.data.paging.BooksPagingSource
import com.partitionsoft.bookshelf.data.remote.api.BookService
import com.partitionsoft.bookshelf.data.remote.api.OpenLibraryService
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
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookService: BookService,
    private val openLibraryService: OpenLibraryService,
    private val favoriteBookDao: FavoriteBookDao
) : BookRepository {

    override fun observePagedBooks(
        query: String,
        orderBy: String?,
        filter: String?,
        pageSize: Int
    ): Flow<PagingData<Book>> = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            initialLoadSize = pageSize,
            prefetchDistance = pageSize / 2,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            BooksPagingSource(
                bookService = bookService,
                openLibraryService = openLibraryService,
                query = query,
                orderBy = orderBy,
                filter = filter,
                favoriteIdsProvider = { favoriteBookDao.getFavoriteIdsOnce().toSet() }
            )
        }
    ).flow

    override fun observePagedCategoryBooks(
        category: BookCategory,
        pageSize: Int
    ): Flow<PagingData<Book>> {
        val normalizedQuery = category.query.takeIf { it.startsWith(SUBJECT_QUERY_PREFIX) }
            ?: (SUBJECT_QUERY_PREFIX + category.query)
        return observePagedBooks(
            query = normalizedQuery,
            orderBy = ORDER_BY_POPULAR,
            pageSize = pageSize
        )
    }

    override fun searchBooks(
        query: String,
        maxResults: Int
    ): Flow<Result<List<Book>>> = flow {
        emit(Result.Loading)
        when (val result = safeApiCall { fetchBooks(query = query, maxResults = maxResults) }) {
            is Result.Success -> {
                emitAll(
                    observeFavoriteIds().map { favoriteIds ->
                        Result.Success(result.data.markFavorites(favoriteIds))
                    }
                )
            }
            is Result.Error -> emit(result)
            Result.Loading -> Unit
        }
    }.flowOn(Dispatchers.IO)

    override fun getBooksByCategory(
        categoryQuery: String,
        maxResults: Int
    ): Flow<Result<List<Book>>> = flow {
        emit(Result.Loading)
        when (
            val result = safeApiCall {
                fetchBooks(
                    query = normalizeCategoryQuery(categoryQuery),
                    maxResults = maxResults,
                    prioritizePopular = true
                )
            }
        ) {
            is Result.Success -> {
                emitAll(
                    observeFavoriteIds().map { favoriteIds ->
                        Result.Success(result.data.markFavorites(favoriteIds))
                    }
                )
            }
            is Result.Error -> emit(result)
            Result.Loading -> Unit
        }
    }.flowOn(Dispatchers.IO)

    override fun getBookDetails(bookId: String): Flow<Result<Book>> = flow {
        emit(Result.Loading)
        try {
            val remoteBook = if (bookId.isOpenLibraryBookId()) {
                val workId = bookId.toOpenLibraryWorkId()
                openLibraryService.getWorkById(workId).toDomain(workId)
            } else {
                bookService.getBookById(bookId).toDomain()
            }
            emitAll(
                observeFavoriteIds().map { favoriteIds ->
                    Result.Success(remoteBook.copy(isFavorite = favoriteIds.contains(remoteBook.id)))
                }
            )
        } catch (e: IOException) {
            val localFallback = favoriteBookDao.getFavoriteById(bookId)?.toDomain()
            if (localFallback != null) {
                emit(Result.Success(localFallback))
            } else {
                emit(Result.Error(e))
            }
        } catch (e: HttpException) {
            val localFallback = favoriteBookDao.getFavoriteById(bookId)?.toDomain()
            if (localFallback != null) {
                emit(Result.Success(localFallback))
            } else {
                emit(Result.Error(e))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun observeHomeFeed(): Flow<Result<HomeFeed>> = flow {
        emit(Result.Loading)
        when (val result = safeApiCall { buildHomeFeed() }) {
            is Result.Success -> {
                emitAll(
                    observeFavoriteIds().map { favoriteIds ->
                        Result.Success(result.data.markFavorites(favoriteIds))
                    }
                )
            }
            is Result.Error -> emit(result)
            Result.Loading -> Unit
        }
    }.flowOn(Dispatchers.IO)

    override fun observeFavoriteBooks(): Flow<List<Book>> =
        favoriteBookDao.observeFavorites().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeFavoriteIds(): Flow<Set<String>> =
        favoriteBookDao.observeFavoriteIds().map { it.toSet() }

    override suspend fun toggleFavorite(book: Book) {
        if (book.id.isBlank()) return
        if (favoriteBookDao.isFavorite(book.id)) {
            favoriteBookDao.deleteFavoriteById(book.id)
        } else {
            favoriteBookDao.upsertFavorite(book.toFavoriteEntity())
        }
    }

    override suspend fun isFavorite(bookId: String): Boolean = favoriteBookDao.isFavorite(bookId)

    private suspend fun buildHomeFeed(): HomeFeed = supervisorScope {
        val featuredDeferred = async {
            fetchFeaturedBooks()
        }

        val sections = SECTION_CONFIGS.map { config ->
            async {
                val books = safeFetchBooksForHome(
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

    private suspend fun fetchFeaturedBooks(): List<Book> {
        val collected = LinkedHashMap<String, Book>()

        for (candidate in FEATURED_QUERY_CANDIDATES) {
            val books = safeFetchBooksForHome(
                query = candidate.query,
                maxResults = candidate.maxResults,
                orderBy = candidate.orderBy,
                filter = candidate.filter
            )
                .asSequence()
                .filter { it.isRecognizableFeaturedBook() }
                .toList()

            books.forEach { book ->
                val key = book.id.ifBlank { "${book.title}_${book.authors.joinToString()}" }
                collected.putIfAbsent(key, book)
            }

            if (collected.size >= FEATURED_SECTION.maxResults) break
        }

        if (collected.isEmpty()) {
            return safeFetchBooksForHome(
                query = FEATURED_SECTION.query,
                maxResults = FEATURED_SECTION.maxResults,
                orderBy = FEATURED_SECTION.orderBy,
                filter = FEATURED_SECTION.filter
            )
        }

        return collected.values
            .toList()
            .sortedByPopularity()
            .take(FEATURED_SECTION.maxResults)
    }

    private fun Book.isRecognizableFeaturedBook(): Boolean {
        if (title.equals("Unknown Title", ignoreCase = true)) return false
        if (authors.isEmpty()) return false
        if (thumbnail.isNullOrBlank()) return false
        return ratingsCount >= MIN_FEATURED_RATINGS || (rating ?: 0.0) >= MIN_FEATURED_RATING
    }

    private suspend fun safeFetchBooksForHome(
        query: String,
        maxResults: Int,
        orderBy: String? = null,
        filter: String? = null
    ): List<Book> = runCatching {
        fetchBooks(
            query = query,
            maxResults = maxResults,
            orderBy = orderBy,
            filter = filter,
            prioritizePopular = true
        )
    }.getOrDefault(emptyList())

    private suspend fun fetchBooks(
        query: String,
        maxResults: Int,
        startIndex: Int = 0,
        orderBy: String? = null,
        filter: String? = null,
        prioritizePopular: Boolean = false
    ): List<Book> {
        val googleOrderBy = when (orderBy) {
            ORDER_BY_POPULAR -> null
            else -> orderBy
        }
        val googleResult = runCatching {
            bookService.searchBooks(
                query = query,
                startIndex = startIndex,
                maxResults = maxResults,
                orderBy = googleOrderBy,
                filter = filter
            )
        }

        val googleBooks = googleResult.getOrNull()
            ?.items
            .orEmpty()
            .map { it.toDomain() }
            .let { books ->
                if (prioritizePopular || orderBy == ORDER_BY_POPULAR) books.sortedByPopularity() else books
            }

        if (googleBooks.isNotEmpty()) return googleBooks

        return fetchOpenLibraryBooks(
            query = query,
            maxResults = maxResults,
            startIndex = startIndex,
            prioritizePopular = prioritizePopular || orderBy == ORDER_BY_POPULAR
        )
    }

    private suspend fun fetchOpenLibraryBooks(
        query: String,
        maxResults: Int,
        startIndex: Int,
        prioritizePopular: Boolean
    ): List<Book> {
        val limit = maxResults.coerceIn(1, 20)
        val page = (startIndex / limit) + 1
        val normalizedQuery = normalizeOpenLibraryQuery(query).ifBlank { DEFAULT_OPEN_LIBRARY_QUERY }

        val books = openLibraryService.searchBooks(
            query = normalizedQuery,
            page = page,
            limit = limit
        ).docs.orEmpty().map { it.toDomain() }

        return if (prioritizePopular) books.sortedByPopularity() else books
    }

    private fun normalizeOpenLibraryQuery(query: String): String =
        query.removePrefix(SUBJECT_QUERY_PREFIX)

    private fun normalizeCategoryQuery(categoryQuery: String): String =
        categoryQuery.takeIf { it.startsWith(SUBJECT_QUERY_PREFIX) }
            ?: (SUBJECT_QUERY_PREFIX + categoryQuery)

    private fun List<Book>.markFavorites(favoriteIds: Set<String>): List<Book> = map { book ->
        book.copy(isFavorite = favoriteIds.contains(book.id))
    }

    private fun HomeFeed.markFavorites(favoriteIds: Set<String>): HomeFeed = copy(
        featured = featured.markFavorites(favoriteIds),
        sections = sections.map { section ->
            section.copy(books = section.books.markFavorites(favoriteIds))
        }
    )

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
        private const val DEFAULT_OPEN_LIBRARY_QUERY = "book"
        private const val MIN_FEATURED_RATINGS = 25
        private const val MIN_FEATURED_RATING = 4.0

        private val FEATURED_SECTION = SectionConfig(
            id = "featured",
            title = "Featured",
            query = "subject:classic literature",
            maxResults = 10,
            layout = SectionLayout.Carousel,
            orderBy = ORDER_BY_POPULAR,
            filter = "ebooks"
        )

        private val FEATURED_QUERY_CANDIDATES = listOf(
            SectionConfig(
                id = "featured_classics",
                title = "Featured",
                query = "subject:classic literature",
                maxResults = 12,
                layout = SectionLayout.Carousel,
                orderBy = ORDER_BY_POPULAR,
                filter = "ebooks"
            ),
            SectionConfig(
                id = "featured_famous_fiction",
                title = "Featured",
                query = "subject:fiction",
                maxResults = 12,
                layout = SectionLayout.Carousel,
                orderBy = ORDER_BY_POPULAR,
                filter = "ebooks"
            ),
            SectionConfig(
                id = "featured_world_literature",
                title = "Featured",
                query = "subject:world literature",
                maxResults = 12,
                layout = SectionLayout.Carousel,
                orderBy = ORDER_BY_POPULAR,
                filter = "ebooks"
            )
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
                layout = SectionLayout.Horizontal
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