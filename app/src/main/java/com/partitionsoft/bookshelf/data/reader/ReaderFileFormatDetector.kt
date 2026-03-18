package com.partitionsoft.bookshelf.data.reader

import android.net.Uri
import com.partitionsoft.bookshelf.domain.model.ReaderDocumentFormat
import java.util.Locale

object ReaderFileFormatDetector {

    fun detect(uri: Uri, mimeType: String?, displayName: String? = null): ReaderDocumentFormat {
        val normalizedMime = mimeType.orEmpty().lowercase(Locale.US)
        if (normalizedMime.contains("pdf")) return ReaderDocumentFormat.PDF
        if (normalizedMime.contains("epub")) return ReaderDocumentFormat.EPUB
        if (normalizedMime.contains("fictionbook") || normalizedMime.contains("fb2")) {
            return ReaderDocumentFormat.FB2
        }

        val candidateName = displayName
            ?.substringAfterLast('/')
            ?.lowercase(Locale.US)
            ?.takeIf { it.isNotBlank() }
            ?: uri.lastPathSegment.orEmpty().lowercase(Locale.US)

        return when {
            candidateName.endsWith(".pdf") -> ReaderDocumentFormat.PDF
            candidateName.endsWith(".epub") -> ReaderDocumentFormat.EPUB
            candidateName.endsWith(".fb2") -> ReaderDocumentFormat.FB2
            else -> ReaderDocumentFormat.UNKNOWN
        }
    }
}

