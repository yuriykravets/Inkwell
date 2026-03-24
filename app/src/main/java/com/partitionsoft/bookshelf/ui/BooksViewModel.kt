package com.partitionsoft.bookshelf.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.model.BookCategory
import com.partitionsoft.bookshelf.domain.model.BookSection
import com.partitionsoft.bookshelf.domain.model.ReaderDocument
import com.partitionsoft.bookshelf.domain.repository.BookRepository
import com.partitionsoft.bookshelf.domain.repository.ReaderRepository
import com.partitionsoft.bookshelf.domain.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BooksUiState {
    data class Success(val bookSearch: List<Book>) : BooksUiState
    object Error : BooksUiState
    object Loading : BooksUiState
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val featured: List<Book> = emptyList(),
    val sections: List<BookSection> = emptyList(),
    val categories: List<BookCategory> = emptyList(),
    val continueReading: ReaderDocument? = null,
    val error: Throwable? = null
)

data class CategoryShelfUiState(
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = false,
    val books: List<Book> = emptyList(),
    val error: Throwable? = null
)

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val booksRepository: BookRepository,
    private val readerRepository: ReaderRepository
) : ViewModel() {

    private val _searchUiState = MutableStateFlow<BooksUiState>(BooksUiState.Loading)
    val searchUiState: StateFlow<BooksUiState> = _searchUiState.asStateFlow()

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _categoryUiState = MutableStateFlow(CategoryShelfUiState())
    val categoryUiState: StateFlow<CategoryShelfUiState> = _categoryUiState.asStateFlow()

    private val _searchWidgetState = MutableStateFlow(SearchWidgetState.CLOSED)
    val searchWidgetState: StateFlow<SearchWidgetState> = _searchWidgetState.asStateFlow()

    private val _searchTextState = MutableStateFlow("")
    val searchTextState: StateFlow<String> = _searchTextState.asStateFlow()

    private var homeJob: Job? = null
    private var categoryJob: Job? = null
    private var searchJob: Job? = null

    init {
        observeContinueReading()
        observeHomeFeed()
        getBooks()
    }

    fun getBooks(query: String = "book", maxResults: Int = 40) {
        searchJob?.cancel()
        searchJob = booksRepository
            .searchBooks(query, maxResults)
            .onEach { result ->
                _searchUiState.value = when (result) {
                    is Result.Loading -> BooksUiState.Loading
                    is Result.Success -> BooksUiState.Success(result.data)
                    is Result.Error -> BooksUiState.Error
                }
            }
            .launchIn(viewModelScope)
    }

    fun closeSearch(clearQuery: Boolean = true) {
        _searchWidgetState.value = SearchWidgetState.CLOSED
        if (clearQuery) {
            _searchTextState.value = ""
        }
    }

    fun refreshHome() {
        observeHomeFeed()
    }

    fun loadCategory(category: BookCategory, maxResults: Int = CATEGORY_MAX_RESULTS) {
        categoryJob?.cancel()
        categoryJob = booksRepository
            .getBooksByCategory(category.query, maxResults)
            .onEach { result ->
                _categoryUiState.value = when (result) {
                    is Result.Loading -> CategoryShelfUiState(
                        selectedCategoryId = category.id,
                        isLoading = true
                    )
                    is Result.Success -> CategoryShelfUiState(
                        selectedCategoryId = category.id,
                        isLoading = false,
                        books = result.data
                    )
                    is Result.Error -> CategoryShelfUiState(
                        selectedCategoryId = category.id,
                        isLoading = false,
                        error = result.exception
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeHomeFeed() {
        homeJob?.cancel()
        homeJob = booksRepository
            .observeHomeFeed()
            .onEach { result ->
                when (result) {
                    is Result.Loading -> _homeUiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        val continueReading = _homeUiState.value.continueReading
                        _homeUiState.value = HomeUiState(
                            isLoading = false,
                            featured = result.data.featured,
                            sections = result.data.sections,
                            categories = result.data.categories,
                            continueReading = continueReading,
                            error = null
                        )
                        val defaultCategory = result.data.categories.firstOrNull()
                        if (defaultCategory != null && _categoryUiState.value.books.isEmpty()) {
                            loadCategory(defaultCategory)
                        }
                    }
                    is Result.Error -> _homeUiState.update {
                        it.copy(isLoading = false, error = result.exception)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeContinueReading() {
        readerRepository
            .observeContinueReading()
            .onEach { latest ->
                _homeUiState.update { current ->
                    current.copy(continueReading = latest)
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

    fun onFavoriteClicked(book: Book) {
        viewModelScope.launch {
            booksRepository.toggleFavorite(book)
        }
    }

    enum class SearchWidgetState {
        OPENED,
        CLOSED
    }

    companion object {
        private const val CATEGORY_MAX_RESULTS = 12
    }
}
