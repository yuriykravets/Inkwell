package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
    onFavoritesClicked: () -> Unit
) {
    AnimatedContent(
        targetState = searchWidgetState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "main_app_bar_state"
    ) { state ->
        when (state) {
            BooksViewModel.SearchWidgetState.CLOSED -> {
                ClosedAppBar(
                    onSearchClicked = onSearchTriggered,
                    onFavoritesClicked = onFavoritesClicked
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
}