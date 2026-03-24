package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.ui.FavoritesIntent
import com.partitionsoft.bookshelf.ui.FavoritesViewModel
import com.partitionsoft.bookshelf.ui.components.InkwellSupportText
import com.partitionsoft.bookshelf.ui.components.InkwellTopBar
import com.partitionsoft.bookshelf.ui.theme.LocalSpacing

@Composable
fun FavoritesRoute(
    onBackClicked: () -> Unit,
    onBookClicked: (Book) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            InkwellTopBar(
                title = stringResource(id = R.string.favorites_title),
                onBackClick = onBackClicked,
                backContentDescription = stringResource(id = R.string.back)
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingScreen(modifier = Modifier.fillMaxSize().padding(paddingValues))
            state.books.isEmpty() -> FavoritesEmptyState(modifier = Modifier.padding(paddingValues))
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    itemsIndexed(
                        items = state.books,
                        key = { index, book -> "${book.id}_$index" }
                    ) { _, book ->
                        BooksCard(
                            book = book,
                            onBookClicked = onBookClicked,
                            onFavoriteClicked = {
                                viewModel.handleIntent(FavoritesIntent.ToggleFavorite(book))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesEmptyState(modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.favorites_empty_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            InkwellSupportText(
                text = stringResource(id = R.string.favorites_empty_subtitle),
                centered = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.sm, start = spacing.lg, end = spacing.lg)
            )
        }
    }
}

