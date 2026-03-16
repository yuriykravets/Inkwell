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
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.model.BookCategory
import com.partitionsoft.bookshelf.ui.navigation.BooksDestinations
import com.partitionsoft.bookshelf.ui.screens.BookDetailsRoute
import com.partitionsoft.bookshelf.ui.screens.BrowseBooksRoute
import com.partitionsoft.bookshelf.ui.screens.FavoritesRoute
import com.partitionsoft.bookshelf.ui.screens.HomeScreen
import com.partitionsoft.bookshelf.ui.screens.MainAppBar
import com.partitionsoft.bookshelf.ui.screens.ReaderRoute

@Composable
fun BooksApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BooksDestinations.HOME_ROUTE,
        modifier = modifier.fillMaxSize()
    ) {
        composable(route = BooksDestinations.HOME_ROUTE) {
            HomeRoute(
                onBookClicked = {
                    navController.navigate(BooksDestinations.detailsRoute(it.id))
                },
                onBrowseRequested = { title, query, orderBy, filter ->
                    navController.navigate(
                        BooksDestinations.browseRoute(
                            title = title,
                            query = query,
                            orderBy = orderBy,
                            filter = filter
                        )
                    )
                },
                onFavoritesClicked = {
                    navController.navigate(BooksDestinations.FAVORITES_ROUTE)
                }
            )
        }

        composable(route = BooksDestinations.FAVORITES_ROUTE) {
            FavoritesRoute(
                onBackClicked = navController::navigateUp,
                onBookClicked = { book ->
                    navController.navigate(BooksDestinations.detailsRoute(book.id))
                }
            )
        }

        composable(
            route = BooksDestinations.BROWSE_ROUTE,
            arguments = listOf(
                navArgument(BooksDestinations.TITLE_ARG) { type = NavType.StringType },
                navArgument(BooksDestinations.QUERY_ARG) { type = NavType.StringType },
                navArgument(BooksDestinations.ORDER_BY_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(BooksDestinations.FILTER_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            BrowseBooksRoute(
                onBackClicked = navController::navigateUp,
                onBookClicked = { book ->
                    navController.navigate(BooksDestinations.detailsRoute(book.id))
                }
            )
        }

        composable(
            route = BooksDestinations.DETAILS_ROUTE,
            arguments = listOf(navArgument(BooksDestinations.BOOK_ID_ARG) { type = NavType.StringType })
        ) {
            BookDetailsRoute(
                onBackClicked = navController::navigateUp,
                onReadClicked = { bookId ->
                    navController.navigate(BooksDestinations.readerRoute(bookId))
                },
                onPreviewClicked = { previewLink ->
                    navController.context.startActivity(
                        android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            previewLink.toUri()
                        )
                    )
                }
            )
        }

        composable(
            route = BooksDestinations.READER_ROUTE,
            arguments = listOf(navArgument(BooksDestinations.BOOK_ID_ARG) { type = NavType.StringType })
        ) {
            ReaderRoute(onBackClicked = navController::navigateUp)
        }
    }
}

@Composable
private fun HomeRoute(
    onBookClicked: (Book) -> Unit,
    onBrowseRequested: (title: String, query: String, orderBy: String?, filter: String?) -> Unit,
    onFavoritesClicked: () -> Unit
) {
    val booksViewModel: BooksViewModel = hiltViewModel()

    val searchUiState by booksViewModel.searchUiState.collectAsStateWithLifecycle()
    val homeUiState by booksViewModel.homeUiState.collectAsStateWithLifecycle()
    val categoryUiState by booksViewModel.categoryUiState.collectAsStateWithLifecycle()
    val searchWidgetState by booksViewModel.searchWidgetState.collectAsStateWithLifecycle()
    val searchTextState by booksViewModel.searchTextState.collectAsStateWithLifecycle()
    val isSearchActive =
        searchWidgetState == BooksViewModel.SearchWidgetState.OPENED || searchTextState.isNotBlank()

    val scaffoldState = rememberScaffoldState()
    val errorMessage = stringResource(id = R.string.loading_failed)
    val retryLabel = stringResource(id = R.string.retry)

    LaunchedEffect(searchUiState, errorMessage, retryLabel) {
        if (searchUiState is BooksUiState.Error) {
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
        onFavoritesClicked = onFavoritesClicked,
        booksUiState = searchUiState,
        homeUiState = homeUiState,
        categoryUiState = categoryUiState,
        isSearchActive = isSearchActive,
        onCategorySelected = booksViewModel::loadCategory,
        onHomeRetry = booksViewModel::refreshHome,
        searchRetryAction = { booksViewModel.getBooks(searchTextState) },
        onBookClicked = onBookClicked,
        onFavoriteClicked = booksViewModel::onFavoriteClicked,
        onBrowseRequested = onBrowseRequested
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
    onFavoritesClicked: () -> Unit,
    booksUiState: BooksUiState,
    homeUiState: HomeUiState,
    categoryUiState: CategoryShelfUiState,
    isSearchActive: Boolean,
    onCategorySelected: (BookCategory) -> Unit,
    onHomeRetry: () -> Unit,
    searchRetryAction: () -> Unit,
    onBookClicked: (Book) -> Unit,
    onFavoriteClicked: (Book) -> Unit,
    onBrowseRequested: (title: String, query: String, orderBy: String?, filter: String?) -> Unit
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
                onSearchTriggered = onSearchTriggered,
                onFavoritesClicked = onFavoritesClicked
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
                homeUiState = homeUiState,
                categoryShelfUiState = categoryUiState,
                booksUiState = booksUiState,
                isSearchActive = isSearchActive,
                retryAction = onHomeRetry,
                onCategorySelected = onCategorySelected,
                modifier = Modifier.fillMaxSize(),
                onBookClicked = onBookClicked,
                onFavoriteClicked = onFavoriteClicked,
                onSearchRetry = searchRetryAction,
                onBrowseRequested = onBrowseRequested
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BooksAppContentPreview() {
    BooksAppContent(
        scaffoldState = rememberScaffoldState(),
        searchWidgetState = BooksViewModel.SearchWidgetState.CLOSED,
        searchTextState = "",
        onTextChange = {},
        onCloseClicked = {},
        onSearchClicked = {},
        onSearchTriggered = {},
        onFavoritesClicked = {},
        booksUiState = BooksUiState.Success(bookSearch = emptyList()),
        homeUiState = HomeUiState(
            isLoading = false,
            featured = emptyList(),
            sections = emptyList(),
            categories = emptyList()
        ),
        categoryUiState = CategoryShelfUiState(),
        isSearchActive = false,
        onCategorySelected = {},
        onHomeRetry = {},
        searchRetryAction = {},
        onBookClicked = {},
        onFavoriteClicked = {},
        onBrowseRequested = { _, _, _, _ -> }
    )
}


