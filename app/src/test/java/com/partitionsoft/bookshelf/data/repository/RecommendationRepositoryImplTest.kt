package com.partitionsoft.bookshelf.data.repository

import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.repository.BookRepository
import com.partitionsoft.bookshelf.domain.result.Result
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationRepositoryImplTest {

    private val bookRepository: BookRepository = mockk()
    private val repository = RecommendationRepositoryImpl(bookRepository)

    @Test
    fun `recommend maps successful search into recommendation reasons`() = runTest {
        every {
            bookRepository.searchBooks(query = any(), maxResults = 24)
        } returns flowOf(
            Result.Loading,
            Result.Success(
                listOf(
                    sampleBook(id = "book-1", title = "Deep Work"),
                    sampleBook(id = "book-2", title = "Atomic Habits")
                )
            )
        )

        val result = repository.recommend(prompt = "Need focus and productivity", limit = 2)

        assertTrue(result is Result.Success)
        result as Result.Success
        assertEquals(2, result.data.size)
        assertEquals("Deep Work", result.data.first().book.title)
        assertTrue(result.data.first().reason.contains("Need focus and productivity"))
    }

    @Test
    fun `recommend returns error when search fails`() = runTest {
        every {
            bookRepository.searchBooks(query = any(), maxResults = 24)
        } returns flowOf(Result.Loading, Result.Error(IllegalStateException("boom")))

        val result = repository.recommend(prompt = "anything")

        assertTrue(result is Result.Error)
    }

    @Test
    fun `recommend localizes reason and keeps ukrainian preference`() = runTest {
        val capturedQueries = mutableListOf<String>()
        every {
            bookRepository.searchBooks(query = any(), maxResults = 24)
        } answers {
            capturedQueries += firstArg<String>()
            flowOf(Result.Loading, Result.Success(listOf(sampleBook(id = "ua-1", title = "Кобзар"))))
        }

        val result = repository.recommend(prompt = "Порадь українську класику")

        assertTrue(capturedQueries.any { query -> query.contains("українською") })
        assertTrue(result is Result.Success)
        result as Result.Success
        assertTrue(result.data.first().reason.contains("Підібрано за вашим запитом"))
    }

    @Test
    fun `recommend keeps author query clean when ukrainian language is requested`() = runTest {
        every {
            bookRepository.searchBooks(query = any(), maxResults = 24)
        } returns flowOf(Result.Loading, Result.Success(listOf(sampleBook(id = "book-1", title = "The Shining"))))

        repository.recommend(prompt = "Stephen King in Ukrainian")

        verify {
            bookRepository.searchBooks(query = "stephen king", maxResults = 24)
            bookRepository.searchBooks(query = "stephen king українською", maxResults = 24)
        }
    }

    private fun sampleBook(id: String, title: String): Book = Book(
        id = id,
        title = title,
        authors = listOf("Author"),
        description = "desc",
        publishedDate = "2024",
        categories = listOf("Productivity"),
        rating = 4.5,
        ratingsCount = 100,
        thumbnail = null,
        previewLink = null,
        pageCount = 200,
        language = "en"
    )
}

