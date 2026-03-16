package com.partitionsoft.bookshelf.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.repository.BookRepository
import com.partitionsoft.bookshelf.ui.navigation.BooksDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseBooksViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BookRepository
) : ViewModel() {

    val title: String = savedStateHandle.get<String>(BooksDestinations.TITLE_ARG)
        ?.let(Uri::decode)
        ?.takeIf { it.isNotBlank() }
        ?: DEFAULT_TITLE

    private val query: String = savedStateHandle.get<String>(BooksDestinations.QUERY_ARG)
        ?.let(Uri::decode)
        ?.takeIf { it.isNotBlank() }
        ?: DEFAULT_QUERY

    private val orderBy: String? = savedStateHandle.get<String>(BooksDestinations.ORDER_BY_ARG)
        ?.let(Uri::decode)
        ?.takeIf { it.isNotBlank() }

    private val filter: String? = savedStateHandle.get<String>(BooksDestinations.FILTER_ARG)
        ?.let(Uri::decode)
        ?.takeIf { it.isNotBlank() }

    val booksPagingFlow: Flow<PagingData<Book>> = repository
        .observePagedBooks(
            query = query,
            orderBy = orderBy,
            filter = filter,
            pageSize = PAGE_SIZE
        )
        .cachedIn(viewModelScope)

    val favoriteIds: StateFlow<Set<String>> = repository
        .observeFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun onFavoriteClicked(book: Book) {
        viewModelScope.launch {
            repository.toggleFavorite(book)
        }
    }

    private companion object {
        private const val PAGE_SIZE = 15
        private const val DEFAULT_QUERY = "book"
        private const val DEFAULT_TITLE = "Books"
    }
}

