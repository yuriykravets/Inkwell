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

sealed interface BookDetailsUiState {
    data object Loading : BookDetailsUiState
    data class Success(val book: Book) : BookDetailsUiState
    data object Error : BookDetailsUiState
}

@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val booksRepository: BookRepository
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle[BooksDestinations.BOOK_ID_ARG])

    private val _uiState = MutableStateFlow<BookDetailsUiState>(BookDetailsUiState.Loading)
    val uiState: StateFlow<BookDetailsUiState> = _uiState.asStateFlow()

    init {
        loadBookDetails()
    }

    fun loadBookDetails() {
        booksRepository
            .getBookDetails(bookId)
            .onEach { result ->
                _uiState.value = when (result) {
                    is Result.Loading -> BookDetailsUiState.Loading
                    is Result.Success -> BookDetailsUiState.Success(result.data)
                    is Result.Error -> BookDetailsUiState.Error
                }
            }
            .launchIn(viewModelScope)
    }
}

