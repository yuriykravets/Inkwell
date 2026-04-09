package com.partitionsoft.bookshelf.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookCoverUrlTest {

    @Test
    fun `optimizedBookCoverUrlOrNull returns null for blank input`() {
        assertNull("   ".optimizedBookCoverUrlOrNull())
    }

    @Test
    fun `toOptimizedBookCoverUrl enforces https and strips curl edge`() {
        val url = "http://books.google.com/books/content?id=abc&printsec=frontcover&img=1&edge=curl"

        val optimized = url.toOptimizedBookCoverUrl()

        assertEquals(
            "https://books.google.com/books/content?id=abc&printsec=frontcover&img=1&zoom=3",
            optimized
        )
    }

    @Test
    fun `toOptimizedBookCoverUrl upgrades existing low zoom param`() {
        val url = "https://books.google.com/books/content?id=abc&zoom=1&img=1"

        val optimized = url.toOptimizedBookCoverUrl()

        assertEquals("https://books.google.com/books/content?id=abc&zoom=3&img=1", optimized)
    }

    @Test
    fun `toOptimizedBookCoverUrl keeps non Google URLs unchanged except https`() {
        val url = "http://covers.openlibrary.org/b/id/123-L.jpg"

        val optimized = url.toOptimizedBookCoverUrl()

        assertEquals("https://covers.openlibrary.org/b/id/123-L.jpg", optimized)
    }
}

