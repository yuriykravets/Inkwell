package com.partitionsoft.bookshelf.data.reader

import android.content.ContentResolver
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream

object EpubParser {

    data class Chapter(
        val title: String,
        val html: String
    )

    data class Publication(
        val title: String,
        val chapters: List<Chapter>
    )

    fun parse(contentResolver: ContentResolver, uri: Uri): Result<Publication> = runCatching {
        val entries = readZipEntries(contentResolver, uri)
        val containerXml = entries[CONTAINER_XML_PATH]?.decodeToString()
            ?: error("Invalid EPUB container")

        val opfPath = extractRootFilePath(containerXml) ?: error("OPF path missing")
        val opfXml = entries[opfPath]?.decodeToString() ?: error("OPF file not found")

        val manifest = extractManifest(opfXml)
        val spine = extractSpineOrder(opfXml)
        val bookTitle = extractTitle(opfXml) ?: DEFAULT_BOOK_TITLE
        val basePath = opfPath.substringBeforeLast('/', missingDelimiterValue = "")

        val chapters = spine.mapNotNull { idRef ->
            val href = manifest[idRef]?.takeIf { isReadableHtml(it) } ?: return@mapNotNull null
            val normalizedPath = if (basePath.isBlank()) href.href else "$basePath/${href.href}"
            val chapterHtml = entries[normalizedPath]?.decodeToString() ?: return@mapNotNull null
            Chapter(
                title = href.title ?: "Chapter",
                html = chapterHtml
            )
        }

        Publication(
            title = bookTitle,
            chapters = chapters
        )
    }

    private data class ManifestItem(
        val href: String,
        val mediaType: String,
        val title: String?
    )

    private fun readZipEntries(contentResolver: ContentResolver, uri: Uri): Map<String, ByteArray> {
        val input = contentResolver.openInputStream(uri) ?: error("Unable to open EPUB")
        return input.use { stream ->
            ZipInputStream(stream).use { zip ->
                val result = linkedMapOf<String, ByteArray>()
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val bytes = ByteArrayOutputStream()
                        val buffer = ByteArray(BUFFER_SIZE)
                        var read = zip.read(buffer)
                        while (read > 0) {
                            bytes.write(buffer, 0, read)
                            read = zip.read(buffer)
                        }
                        result[entry.name] = bytes.toByteArray()
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
                result
            }
        }
    }

    private fun extractRootFilePath(containerXml: String): String? =
        Regex("full-path\\s*=\\s*\"([^\"]+)\"")
            .find(containerXml)
            ?.groupValues
            ?.getOrNull(1)

    private fun extractManifest(opfXml: String): Map<String, ManifestItem> {
        val itemRegex = Regex(
            "<item[^>]*id=\"([^\"]+)\"[^>]*href=\"([^\"]+)\"[^>]*media-type=\"([^\"]+)\"[^>]*>"
        )
        return itemRegex.findAll(opfXml).associate { match ->
            val id = match.groupValues[1]
            val href = match.groupValues[2]
            val mediaType = match.groupValues[3]
            id to ManifestItem(href = href, mediaType = mediaType, title = href.substringAfterLast('/'))
        }
    }

    private fun extractSpineOrder(opfXml: String): List<String> {
        val spineRegex = Regex("<itemref[^>]*idref=\"([^\"]+)\"[^>]*/?>")
        return spineRegex.findAll(opfXml).map { it.groupValues[1] }.toList()
    }

    private fun extractTitle(opfXml: String): String? =
        Regex("<dc:title[^>]*>(.*?)</dc:title>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .find(opfXml)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?.takeIf { it.isNotBlank() }

    private fun isReadableHtml(item: ManifestItem): Boolean =
        item.mediaType.contains("xhtml") || item.mediaType.contains("html")

    private fun ByteArray.decodeToString(): String = String(this, StandardCharsets.UTF_8)

    private const val BUFFER_SIZE = 8 * 1024
    private const val CONTAINER_XML_PATH = "META-INF/container.xml"
    private const val DEFAULT_BOOK_TITLE = "EPUB"
}

