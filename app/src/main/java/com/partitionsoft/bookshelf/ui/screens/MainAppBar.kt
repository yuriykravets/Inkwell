package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.runtime.Composable
import com.partitionsoft.bookshelf.ui.BooksViewModel

@Composable
fun MainAppBar(
    searchWidgetState: BooksViewModel.SearchWidgetState,
    searchTextState: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit,
    onSearchClicked: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    onFavoritesClicked: () -> Unit,
    onLibraryClicked: () -> Unit
) {
    when (searchWidgetState) {
        BooksViewModel.SearchWidgetState.CLOSED -> {
            ClosedAppBar (
                onSearchTriggered,
                onFavoritesClicked,
                onLibraryClicked
            )
        }
        BooksViewModel.SearchWidgetState.OPENED -> {
            OpenedAppBar(
                text = searchTextState,
                onTextChange = onTextChange,
                onCloseClicked = onCloseClicked,
                onSearchClicked = onSearchClicked
            )
        }
    }
}