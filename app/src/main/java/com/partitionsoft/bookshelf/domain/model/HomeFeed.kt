package com.partitionsoft.bookshelf.domain.model

/**
 * Represents the curated data required to render the home screen: featured titles, reusable
 * sections, and the selectable category chips. Keeping this in the domain layer keeps the UI
 * decoupled from how queries are composed.
 */
data class HomeFeed(
    val featured: List<Book>,
    val categories: List<BookCategory>,
    val sections: List<BookSection>
)

data class BookCategory(
    val id: String,
    val title: String,
    val query: String
)

data class BookSection(
    val id: String,
    val title: String,
    val layout: SectionLayout,
    val books: List<Book>
)

enum class SectionLayout {
    Carousel,
    Horizontal
}

