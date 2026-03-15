package com.partitionsoft.bookshelf.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.repository.BookRepository
import com.partitionsoft.bookshelf.domain.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

sealed interface BooksUiState {
    data class Success(val bookSearch: List<Book>) : BooksUiState
    object Error : BooksUiState
    object Loading : BooksUiState
}

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val booksRepository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BooksUiState>(BooksUiState.Loading)
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    private val _searchWidgetState = MutableStateFlow(SearchWidgetState.CLOSED)
    val searchWidgetState: StateFlow<SearchWidgetState> = _searchWidgetState.asStateFlow()

    private val _searchTextState = MutableStateFlow("")
    val searchTextState: StateFlow<String> = _searchTextState.asStateFlow()

    init {
        getBooks()
    }

    fun getBooks(query: String = "book", maxResults: Int = 40) {
        booksRepository
            .searchBooks(query, maxResults)
            .onEach { result ->
                _uiState.value = when (result) {
                    is Result.Loading -> BooksUiState.Loading
                    is Result.Success -> BooksUiState.Success(result.data)
                    is Result.Error -> BooksUiState.Error
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchTextState(newValue: String) {
        _searchTextState.value = newValue
    }

    fun updateSearchWidgetState(newValue: SearchWidgetState) {
        _searchWidgetState.value = newValue
    }

    enum class SearchWidgetState {
        OPENED,
        CLOSED
    }
}
