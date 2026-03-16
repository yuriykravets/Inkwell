package com.partitionsoft.bookshelf.ui.navigation

import android.net.Uri

object BooksDestinations {
    const val HOME_ROUTE = "home"
    const val FAVORITES_ROUTE = "favorites"
    const val DETAILS_ROUTE = "details/{bookId}"
    const val READER_ROUTE = "reader/{bookId}"
    const val BROWSE_ROUTE = "browse?title={title}&query={query}&orderBy={orderBy}&filter={filter}"

    const val BOOK_ID_ARG = "bookId"
    const val TITLE_ARG = "title"
    const val QUERY_ARG = "query"
    const val ORDER_BY_ARG = "orderBy"
    const val FILTER_ARG = "filter"

    fun detailsRoute(bookId: String): String = "details/$bookId"

    fun readerRoute(bookId: String): String = "reader/$bookId"

    fun browseRoute(
        title: String,
        query: String,
        orderBy: String? = null,
        filter: String? = null
    ): String = buildString {
        append("browse")
        append("?title=")
        append(Uri.encode(title))
        append("&query=")
        append(Uri.encode(query))
        append("&orderBy=")
        append(Uri.encode(orderBy.orEmpty()))
        append("&filter=")
        append(Uri.encode(filter.orEmpty()))
    }
}

