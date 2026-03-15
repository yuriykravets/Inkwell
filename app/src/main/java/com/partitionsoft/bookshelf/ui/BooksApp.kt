package com.partitionsoft.bookshelf.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.ui.screens.HomeScreen
import com.partitionsoft.bookshelf.ui.screens.MainAppBar

@Composable
fun BooksApp(
    modifier: Modifier = Modifier,
    onBookClicked: (Book) -> Unit = {}
) {
    val booksViewModel: BooksViewModel = hiltViewModel()

    val uiState by booksViewModel.uiState.collectAsStateWithLifecycle()
    val searchWidgetState by booksViewModel.searchWidgetState.collectAsStateWithLifecycle()
    val searchTextState by booksViewModel.searchTextState.collectAsStateWithLifecycle()

    val scaffoldState = rememberScaffoldState()
    val errorMessage = stringResource(id = R.string.loading_failed)
    val retryLabel = stringResource(id = R.string.retry)

    LaunchedEffect(uiState, errorMessage, retryLabel) {
        if (uiState is BooksUiState.Error) {
            val result = scaffoldState.snackbarHostState.showSnackbar(
                message = errorMessage,
                actionLabel = retryLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                booksViewModel.getBooks()
            }
        }
    }

    BooksAppContent(
        modifier = modifier,
        scaffoldState = scaffoldState,
        searchWidgetState = searchWidgetState,
        searchTextState = searchTextState,
        onTextChange = booksViewModel::updateSearchTextState,
        onCloseClicked = {
            booksViewModel.updateSearchWidgetState(newValue = BooksViewModel.SearchWidgetState.CLOSED)
        },
        onSearchClicked = booksViewModel::getBooks,
        onSearchTriggered = {
            booksViewModel.updateSearchWidgetState(newValue = BooksViewModel.SearchWidgetState.OPENED)
        },
        booksUiState = uiState,
        retryAction = { booksViewModel.getBooks() },
        onBookClicked = onBookClicked
    )
}

@Composable
private fun BooksAppContent(
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState,
    searchWidgetState: BooksViewModel.SearchWidgetState,
    searchTextState: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    booksUiState: BooksUiState,
    retryAction: () -> Unit,
    onBookClicked: (Book) -> Unit
) {
    Scaffold(
        scaffoldState = scaffoldState,
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        snackbarHost = { SnackbarHost(hostState = scaffoldState.snackbarHostState) },
        topBar = {
            MainAppBar(
                searchWidgetState = searchWidgetState,
                searchTextState = searchTextState,
                onTextChange = onTextChange,
                onCloseClicked = onCloseClicked,
                onSearchClicked = onSearchClicked,
                onSearchTriggered = onSearchTriggered
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colors.background
        ) {
            HomeScreen(
                booksUiState = booksUiState,
                retryAction = retryAction,
                modifier = Modifier.fillMaxSize(),
                onBookClicked = onBookClicked
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BooksAppContentPreview(
    @PreviewParameter(BookPreviewParameterProvider::class) books: List<Book>
) {
    BooksAppContent(
        scaffoldState = rememberScaffoldState(),
        searchWidgetState = BooksViewModel.SearchWidgetState.CLOSED,
        searchTextState = "",
        onTextChange = {},
        onCloseClicked = {},
        onSearchClicked = {},
        onSearchTriggered = {},
        booksUiState = BooksUiState.Success(bookSearch = books),
        retryAction = {},
        onBookClicked = {}
    )
}

private class BookPreviewParameterProvider : PreviewParameterProvider<List<Book>> {
    override val values: Sequence<List<Book>> = sequenceOf(
        listOf(
            Book(
                id = "1",
                title = "Designing Compose",
                authors = listOf("Jane Doe"),
                description = null,
                publishedDate = "2023",
                categories = emptyList(),
                rating = 4.5,
                ratingsCount = 120,
                thumbnail = null,
                previewLink = null,
                pageCount = 320,
                language = "en"
            ),
            Book(
                id = "2",
                title = "Modern Android",
                authors = listOf("John Smith", "Chris Blue"),
                description = null,
                publishedDate = "2022",
                categories = emptyList(),
                rating = 4.8,
                ratingsCount = 220,
                thumbnail = null,
                previewLink = null,
                pageCount = 280,
                language = "en"
            )
        )
    )
}

