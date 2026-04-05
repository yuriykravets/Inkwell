package com.partitionsoft.bookshelf.data.repository

import com.partitionsoft.bookshelf.domain.model.AiRecommendation
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.repository.BookRepository
import com.partitionsoft.bookshelf.domain.repository.RecommendationRepository
import com.partitionsoft.bookshelf.domain.result.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationRepositoryImpl @Inject constructor(
    private val bookRepository: BookRepository
) : RecommendationRepository {

    private data class SearchIntent(
        val query: String,
        val preferUkrainian: Boolean
    )

    override suspend fun recommend(prompt: String, limit: Int): Result<List<AiRecommendation>> {
        val intent = prompt.toSearchIntent()
        val searchResult = runRecommendationSearch(
            query = intent.query,
            preferUkrainian = intent.preferUkrainian
        )

        return when (searchResult) {
            is Result.Success -> {
                val items = searchResult.data
                    .filter { it.id.isNotBlank() && it.title.isNotBlank() }
                    .let { books ->
                        if (intent.preferUkrainian) books.sortedByUkrainianPriority() else books
                    }
                    .distinctBy { it.id }
                    .take(limit)
                    .map { book ->
                        AiRecommendation(
                            book = book,
                            reason = buildReason(prompt = prompt, book = book)
                        )
                    }
                Result.Success(items)
            }
            is Result.Error -> Result.Error(searchResult.exception)
            Result.Loading -> Result.Error(IllegalStateException("Recommendation flow did not resolve"))
        }
    }

    private suspend fun runRecommendationSearch(
        query: String,
        preferUkrainian: Boolean
    ): Result<List<Book>> {
        if (!preferUkrainian) {
            return bookRepository.searchBooks(query = query, maxResults = 24)
                .first { result -> result !is Result.Loading }
        }

        val ukrainianSeed = bookRepository.searchBooks(
            query = "${query.withoutLanguageIntentTokens()} українською".trim(),
            maxResults = 24
        ).first { result -> result !is Result.Loading }

        val fallbackSeed = bookRepository.searchBooks(
            query = query,
            maxResults = 24
        ).first { result -> result !is Result.Loading }

        val primary = (ukrainianSeed as? Result.Success)?.data.orEmpty()
        val secondary = (fallbackSeed as? Result.Success)?.data.orEmpty()

        return when {
            primary.isNotEmpty() || secondary.isNotEmpty() -> {
                Result.Success((primary + secondary).distinctBy { it.id })
            }
            ukrainianSeed is Result.Error -> ukrainianSeed
            fallbackSeed is Result.Error -> fallbackSeed
            else -> Result.Success(emptyList())
        }
    }

    private fun buildReason(prompt: String, book: Book): String {
        return if (prompt.prefersUkrainian()) {
            val authorPart = book.authors.firstOrNull()?.let { " автора $it" }.orEmpty()
            val categoryPart = book.categories.firstOrNull()?.let { " у жанрі $it" }.orEmpty()
            "Підібрано за вашим запитом '${prompt.trim()}': ${book.title}$authorPart$categoryPart."
        } else {
            val authorPart = book.authors.firstOrNull()?.let { " by $it" }.orEmpty()
            val categoryPart = book.categories.firstOrNull()?.let { " in $it" }.orEmpty()
            "Matched your request '${prompt.trim()}' with ${book.title}$authorPart$categoryPart."
        }
    }

    private fun String.toSearchQuery(): String {
        val normalized = lowercase()
            .replace(Regex("[^\\p{L}\\p{N} ]"), " ")
            .split(Regex("\\s+"))
            .filter { token -> token.length >= 3 }
            .take(5)
            .joinToString(" ")

        return normalized.ifBlank { "bestseller" }
    }

    private fun String.toSearchIntent(): SearchIntent {
        val preferUkrainian = prefersUkrainian()
        val query = toSearchQuery().withoutLanguageIntentTokens().ifBlank { "bestseller" }
        return SearchIntent(query = query, preferUkrainian = preferUkrainian)
    }

    private fun String.withoutLanguageIntentTokens(): String {
        return split(Regex("\\s+"))
            .filter { token -> token.isNotBlank() && !token.isUkrainianIntentToken() }
            .take(5)
            .joinToString(" ")
    }

    private fun String.isUkrainianIntentToken(): Boolean {
        val token = lowercase()
        return token.startsWith("укра") || token.startsWith("ukrain")
    }

    private fun String.prefersUkrainian(): Boolean {
        val text = lowercase()
        return text.contains(Regex("[а-щьюяєіїґ]")) ||
            text.contains("укра") ||
            text.contains("ukrain")
    }

    private fun List<Book>.sortedByUkrainianPriority(): List<Book> = sortedByDescending { book ->
        book.language == "uk" ||
            book.title.contains(Regex("[А-ЩЬЮЯЄІЇҐа-щьюяєіїґ]")) ||
            book.authors.any { author -> author.contains(Regex("[А-ЩЬЮЯЄІЇҐа-щьюяєіїґ]")) }
    }
}

