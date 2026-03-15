package com.partitionsoft.bookshelf.ui.navigation

object BooksDestinations {
    const val HOME_ROUTE = "home"
    const val DETAILS_ROUTE = "details/{bookId}"
    const val READER_ROUTE = "reader/{bookId}"

    const val BOOK_ID_ARG = "bookId"

    fun detailsRoute(bookId: String): String = "details/$bookId"

    fun readerRoute(bookId: String): String = "reader/$bookId"
}

