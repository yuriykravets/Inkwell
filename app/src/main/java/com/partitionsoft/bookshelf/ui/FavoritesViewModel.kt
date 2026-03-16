package com.partitionsoft.bookshelf.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FavoritesIntent {
    data object ObserveFavorites : FavoritesIntent
    data class ToggleFavorite(val book: Book) : FavoritesIntent
}

data class FavoritesViewState(
    val isLoading: Boolean = true,
    val books: List<Book> = emptyList(),
    val error: Throwable? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: BookRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesViewState())
    val state: StateFlow<FavoritesViewState> = _state.asStateFlow()

    private val _intents = MutableSharedFlow<FavoritesIntent>(extraBufferCapacity = 8)
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        handleIntent(FavoritesIntent.ObserveFavorites)
    }

    fun handleIntent(intent: FavoritesIntent) {
        _intents.tryEmit(intent)
        when (intent) {
            FavoritesIntent.ObserveFavorites -> observeFavorites()
            is FavoritesIntent.ToggleFavorite -> toggleFavorite(intent.book)
        }
    }

    private fun observeFavorites() {
        repository.observeFavoriteBooks()
            .onEach { books ->
                _state.value = FavoritesViewState(
                    isLoading = false,
                    books = books,
                    error = null
                )
            }
            .launchIn(viewModelScope)
    }

    private fun toggleFavorite(book: Book) {
        viewModelScope.launch {
            runCatching { repository.toggleFavorite(book) }
                .onFailure {
                    _events.tryEmit("Unable to update favorites")
                }
        }
    }
}

