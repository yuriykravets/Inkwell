package com.partitionsoft.bookshelf.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.repository.BookRepository
import com.partitionsoft.bookshelf.domain.result.Result
import com.partitionsoft.bookshelf.ui.navigation.BooksDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Ready(val book: Book, val readerUrl: String) : ReaderUiState
    data object Unavailable : ReaderUiState
    data object Error : ReaderUiState
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val booksRepository: BookRepository
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle[BooksDestinations.BOOK_ID_ARG])

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        loadReader()
    }

    fun loadReader() {
        booksRepository
            .getBookDetails(bookId)
            .onEach { result ->
                _uiState.value = when (result) {
                    is Result.Loading -> ReaderUiState.Loading
                    is Result.Success -> result.data.toReaderState()
                    is Result.Error -> ReaderUiState.Error
                }
            }
            .launchIn(viewModelScope)
    }

    private fun Book.toReaderState(): ReaderUiState {
        val embeddedReaderUrl = "https://books.google.com/books?id=$id&output=embed"
        return if (!embeddable) {
            ReaderUiState.Unavailable
        } else {
            ReaderUiState.Ready(book = this, readerUrl = embeddedReaderUrl)
        }
    }
}

