package com.partitionsoft.bookshelf.data.reader

import android.content.ContentResolver
import android.net.Uri
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

object Fb2Parser {

    data class Chapter(
        val title: String,
        val html: String
    )

    data class Publication(
        val title: String,
        val chapters: List<Chapter>
    )

    fun parse(contentResolver: ContentResolver, uri: Uri): Result<Publication> = runCatching {
        val document = contentResolver.openInputStream(uri)?.use { input ->
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
            }
            val builder = factory.newDocumentBuilder()
            builder.parse(input)
        } ?: error("Unable to open FB2")

        val root = document.documentElement ?: error("Invalid FB2 document")
        val rawTitle = root.firstTextByTag("book-title")
        val title = rawTitle?.takeIf { it.isNotBlank() } ?: DEFAULT_BOOK_TITLE

        val allBodies = root.elementsByTag("body")
        val contentBodies = allBodies.filterNot { body ->
            body.getAttribute("name").equals("notes", ignoreCase = true)
        }.ifEmpty { allBodies }

        val chapters = mutableListOf<Chapter>()
        contentBodies.forEach { body ->
            val bodySections = body.directChildren("section")
            if (bodySections.isEmpty()) {
                // Some providers wrap full text under nested sections without direct body parsing.
                val html = buildSectionHtml(body, includeNestedSections = true)
                if (html.isNotBlank()) {
                    chapters += Chapter(title = FALLBACK_CHAPTER_TITLE, html = html)
                }
            } else {
                bodySections.forEach { section ->
                    collectSections(section, chapters)
                }
            }
        }

        val normalizedChapters = chapters
            .mapIndexed { index, chapter ->
                chapter.copy(title = chapter.title.takeIf { it.isNotBlank() } ?: "Section ${index + 1}")
            }
            .ifEmpty {
                listOf(
                    Chapter(
                        title = FALLBACK_CHAPTER_TITLE,
                        html = "<p>$EMPTY_CONTENT_FALLBACK</p>"
                    )
                )
            }

        Publication(
            title = title,
            chapters = normalizedChapters
        )
    }

    private fun collectSections(section: Element, chapters: MutableList<Chapter>) {
        val title = section.firstTextByTag("title") ?: FALLBACK_CHAPTER_TITLE
        val html = buildSectionHtml(section)
        if (html.isNotBlank()) {
            chapters += Chapter(title = title.trim(), html = html)
        }
        section.directChildren("section").forEach { child ->
            collectSections(child, chapters)
        }
    }

    private fun buildSectionHtml(container: Element, includeNestedSections: Boolean = false): String {
        val htmlParts = mutableListOf<String>()
        appendSectionNodes(container, htmlParts, includeNestedSections)
        return htmlParts.joinToString(separator = "\n")
    }

    private fun appendSectionNodes(
        node: Node,
        htmlParts: MutableList<String>,
        includeNestedSections: Boolean
    ) {
        val children = node.childNodes ?: return
        for (index in 0 until children.length) {
            val child = children.item(index)
            if (child.nodeType != Node.ELEMENT_NODE) continue

            val element = child as Element
            when (element.localTagName()) {
                "section" -> if (includeNestedSections) {
                    appendSectionNodes(element, htmlParts, includeNestedSections = true)
                }
                "title" -> {
                    val heading = element.textContent.orEmpty().trim()
                    if (heading.isNotBlank()) {
                        htmlParts += "<h2>${heading.escapeHtml()}</h2>"
                    }
                }
                "subtitle" -> {
                    val subtitle = element.textContent.orEmpty().trim()
                    if (subtitle.isNotBlank()) {
                        htmlParts += "<h3>${subtitle.escapeHtml()}</h3>"
                    }
                }
                "p", "v", "text-author" -> {
                    val paragraph = element.textContent.orEmpty().trim()
                    if (paragraph.isNotBlank()) {
                        htmlParts += "<p>${paragraph.escapeHtml()}</p>"
                    }
                }
                "empty-line" -> htmlParts += "<br/>"
                else -> appendSectionNodes(element, htmlParts, includeNestedSections)
            }
        }
    }

    private fun Element.firstTextByTag(tagName: String): String? =
        elementsByTag(tagName)
            .firstOrNull()
            ?.textContent
            ?.trim()
            ?.takeIf { it.isNotBlank() }

    private fun Element.elementsByTag(tagName: String): List<Element> {
        val result = mutableListOf<Element>()
        val nodes = getElementsByTagNameNS("*", tagName)
        for (index in 0 until nodes.length) {
            val item = nodes.item(index)
            if (item is Element) result += item
        }
        if (result.isNotEmpty()) return result

        val fallback = getElementsByTagName(tagName)
        for (index in 0 until fallback.length) {
            val item = fallback.item(index)
            if (item is Element) result += item
        }
        return result
    }

    private fun Element.directChildren(tagName: String): List<Element> {
        val result = mutableListOf<Element>()
        val children = childNodes
        for (index in 0 until children.length) {
            val child = children.item(index)
            if (child is Element && child.localTagName() == tagName) {
                result += child
            }
        }
        return result
    }

    private fun Element.localTagName(): String =
        localName?.lowercase().orEmpty().ifBlank { tagName.substringAfter(':').lowercase() }

    private fun String.escapeHtml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")

    private const val DEFAULT_BOOK_TITLE = "FB2"
    private const val FALLBACK_CHAPTER_TITLE = "Section"
    private const val EMPTY_CONTENT_FALLBACK = "This chapter has no readable text."
}

