package com.partitionsoft.bookshelf.domain.model

private val ZOOM_QUERY_REGEX = Regex("([?&])zoom=\\d+")

fun String?.optimizedBookCoverUrlOrNull(): String? =
    this
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.toOptimizedBookCoverUrl()

fun String.toOptimizedBookCoverUrl(): String {
    var url = this
        .replace("http://", "https://")
        .replace("http:", "https:")
        .replace("&edge=curl", "")
        .replace("?edge=curl&", "?")
        .replace("?edge=curl", "")
        .replace("&&", "&")
        .replace("?&", "?")

    if (url.contains("books.google", ignoreCase = true)) {
        url = if (ZOOM_QUERY_REGEX.containsMatchIn(url)) {
            url.replace(ZOOM_QUERY_REGEX, "$1zoom=3")
        } else {
            val join = if (url.contains("?")) "&" else "?"
            "$url${join}zoom=3"
        }
    }

    return url
}

