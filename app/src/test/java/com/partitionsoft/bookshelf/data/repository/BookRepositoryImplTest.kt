package com.partitionsoft.bookshelf.data.repository

import com.partitionsoft.bookshelf.data.local.FavoriteBookDao
import com.partitionsoft.bookshelf.data.remote.api.BookService
import com.partitionsoft.bookshelf.data.remote.dto.BookShelfDto
import com.partitionsoft.bookshelf.data.remote.dto.ImageLinksDto
import com.partitionsoft.bookshelf.data.remote.dto.ItemDto
import com.partitionsoft.bookshelf.data.remote.dto.VolumeInfoDto
import com.partitionsoft.bookshelf.data.remote.dto.AccessInfoDto
import com.partitionsoft.bookshelf.domain.result.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class BookRepositoryImplTest {

    private val service: BookService = mockk()
    private val favoriteBookDao: FavoriteBookDao = mockk()
    private val repository = BookRepositoryImpl(service, favoriteBookDao)

    @Test
    fun `searchBooks emits loading then success`() = runTest {
        every { favoriteBookDao.observeFavoriteIds() } returns flowOf(emptyList())
        coEvery {
            service.searchBooks(
                query = any(),
                startIndex = any(),
                maxResults = any(),
                orderBy = any(),
                filter = any(),
                printType = any()
            )
        } returns sampleResponse()

        val emissions = repository.searchBooks("android").take(2).toList()

        assertTrue(emissions.first() is Result.Loading)
        val success = emissions[1] as Result.Success
        assertEquals(1, success.data.size)
        assertEquals("Sample Book", success.data.first().title)
    }

    @Test
    fun `searchBooks emits error when api fails`() = runTest {
        every { favoriteBookDao.observeFavoriteIds() } returns flowOf(emptyList())
        coEvery {
            service.searchBooks(
                query = any(),
                startIndex = any(),
                maxResults = any(),
                orderBy = any(),
                filter = any(),
                printType = any()
            )
        } throws IOException("boom")

        val emissions = repository.searchBooks("android").take(2).toList()

        assertTrue(emissions.first() is Result.Loading)
        assertTrue(emissions[1] is Result.Error)
    }

    @Test
    fun `getBookDetails emits loading then mapped book`() = runTest {
        every { favoriteBookDao.observeFavoriteIds() } returns flowOf(emptyList())
        coEvery { service.getBookById("book-1") } returns ItemDto(
            id = "book-1",
            volumeInfo = VolumeInfoDto(
                title = "Reader Book",
                authors = listOf("Author"),
                previewLink = "http://example.com/preview"
            ),
            accessInfo = AccessInfoDto(
                webReaderLink = "http://example.com/reader",
                embeddable = true
            )
        )

        val emissions = repository.getBookDetails("book-1").take(2).toList()

        assertTrue(emissions.first() is Result.Loading)
        val success = emissions[1] as Result.Success
        assertEquals("book-1", success.data.id)
        assertEquals("https://example.com/reader", success.data.webReaderLink)
        assertTrue(success.data.embeddable)
    }

    private fun sampleResponse(id: String = "1"): BookShelfDto = BookShelfDto(
        totalItems = 1,
        items = listOf(
            ItemDto(
                id = id,
                volumeInfo = VolumeInfoDto(
                    title = "Sample Book",
                    authors = listOf("Author"),
                    publishedDate = "2024",
                    categories = listOf("Category"),
                    averageRating = 4.0,
                    ratingsCount = 100,
                    imageLinksDto = ImageLinksDto(thumbnail = "http://example.com/cover.jpg"),
                    previewLink = "http://example.com/preview",
                    pageCount = 300,
                    language = "en"
                )
            )
        )
    )
}

