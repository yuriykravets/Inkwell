package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.ui.BooksUiState

@Composable
fun HomeScreen(
    booksUiState: BooksUiState,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier,
    onBookClicked: (Book) -> Unit
) {
    Crossfade(
        targetState = booksUiState,
        modifier = modifier,
        label = "home_state"
    ) { state ->
        when (state) {
            is BooksUiState.Loading -> LoadingScreen(modifier = Modifier.fillMaxSize())
            is BooksUiState.Success -> {
                if (state.bookSearch.isEmpty()) {
                    EmptyScreen(onRefresh = retryAction)
                } else {
                    BooksGridScreen(
                        books = state.bookSearch,
                        modifier = Modifier.fillMaxSize(),
                        onBookClicked = onBookClicked
                    )
                }
            }
            is BooksUiState.Error -> ErrorScreen(
                retryAction = retryAction,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EmptyScreen(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_book_96),
            contentDescription = stringResource(id = R.string.empty_results_title)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.empty_results_title),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.empty_results_supporting),
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRefresh) {
            Text(text = stringResource(id = R.string.try_another_search))
        }
    }
}