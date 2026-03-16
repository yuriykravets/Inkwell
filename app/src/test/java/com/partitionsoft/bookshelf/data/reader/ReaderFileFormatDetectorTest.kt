package com.partitionsoft.bookshelf.data.reader

import android.net.Uri
import com.partitionsoft.bookshelf.domain.model.ReaderDocumentFormat
import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderFileFormatDetectorTest {

    @Test
    fun `detect returns pdf from mime type`() {
        val format = ReaderFileFormatDetector.detect(
            uri = Uri.parse("content://books/book.epub"),
            mimeType = "application/pdf"
        )

        assertEquals(ReaderDocumentFormat.PDF, format)
    }

    @Test
    fun `detect returns epub from extension`() {
        val format = ReaderFileFormatDetector.detect(
            uri = Uri.parse("content://books/my_novel.epub"),
            mimeType = null
        )

        assertEquals(ReaderDocumentFormat.EPUB, format)
    }

    @Test
    fun `detect returns fb2 from extension`() {
        val format = ReaderFileFormatDetector.detect(
            uri = Uri.parse("content://books/archive.fb2"),
            mimeType = "application/octet-stream"
        )

        assertEquals(ReaderDocumentFormat.FB2, format)
    }

    @Test
    fun `detect returns fb2 from mime type`() {
        val format = ReaderFileFormatDetector.detect(
            uri = Uri.parse("content://books/archive.xml"),
            mimeType = "application/x-fictionbook+xml"
        )

        assertEquals(ReaderDocumentFormat.FB2, format)
    }

    @Test
    fun `detect returns fb2 from display name when uri has no extension`() {
        val format = ReaderFileFormatDetector.detect(
            uri = Uri.parse("content://com.android.providers.downloads.documents/document/7421"),
            mimeType = "application/octet-stream",
            displayName = "war-and-peace.fb2"
        )

        assertEquals(ReaderDocumentFormat.FB2, format)
    }
}

