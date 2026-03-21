package com.partitionsoft.bookshelf.data.repository

import com.partitionsoft.bookshelf.domain.model.Book
import org.junit.Assert.assertEquals
import org.junit.Test

class BookPopularityTest {

    @Test
    fun `sortedByPopularity sorts by ratings count then rating then year`() {
        val low = sampleBook(
            id = "low",
            title = "Low",
            ratingsCount = 20,
            rating = 4.8,
            publishedDate = "2024"
        )
        val high = sampleBook(
            id = "high",
            title = "High",
            ratingsCount = 500,
            rating = 3.2,
            publishedDate = "2016"
        )
        val sameCountHigherRating = sampleBook(
            id = "same-count-high-rating",
            title = "Same Count High Rating",
            ratingsCount = 100,
            rating = 4.9,
            publishedDate = "2020"
        )
        val sameCountLowerRatingNewer = sampleBook(
            id = "same-count-low-rating",
            title = "Same Count Low Rating",
            ratingsCount = 100,
            rating = 4.2,
            publishedDate = "2025-01"
        )

        val sorted = listOf(low, sameCountLowerRatingNewer, high, sameCountHigherRating).sortedByPopularity()

        assertEquals(
            listOf("high", "same-count-high-rating", "same-count-low-rating", "low"),
            sorted.map { it.id }
        )
    }

    @Test
    fun `sortedByPopularity uses title as stable fallback when popularity signals are missing`() {
        val zeta = sampleBook(id = "z", title = "Zeta", ratingsCount = 0, rating = null, publishedDate = null)
        val alpha = sampleBook(id = "a", title = "Alpha", ratingsCount = 0, rating = null, publishedDate = null)

        val sorted = listOf(zeta, alpha).sortedByPopularity()

        assertEquals(listOf("a", "z"), sorted.map { it.id })
    }

    private fun sampleBook(
        id: String,
        title: String,
        ratingsCount: Int,
        rating: Double?,
        publishedDate: String?
    ) = Book(
        id = id,
        title = title,
        authors = emptyList(),
        description = null,
        publishedDate = publishedDate,
        categories = emptyList(),
        rating = rating,
        ratingsCount = ratingsCount,
        thumbnail = null,
        previewLink = null,
        webReaderLink = null,
        embeddable = false,
        pageCount = null,
        language = null,
        isFavorite = false
    )
}

