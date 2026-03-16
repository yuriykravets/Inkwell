package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.ui.BrowseBooksViewModel
import kotlinx.coroutines.delay

@Composable
fun BrowseBooksRoute(
    onBackClicked: () -> Unit,
    onBookClicked: (Book) -> Unit,
    viewModel: BrowseBooksViewModel = hiltViewModel()
) {
    val books = viewModel.booksPagingFlow.collectAsLazyPagingItems()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    BrowseBooksScreen(
        title = viewModel.title,
        onBackClicked = onBackClicked,
        onBookClicked = onBookClicked,
        books = books,
        favoriteIds = favoriteIds,
        onFavoriteClicked = viewModel::onFavoriteClicked
    )
}

@Composable
private fun BrowseBooksScreen(
    title: String,
    onBackClicked: () -> Unit,
    onBookClicked: (Book) -> Unit,
    books: androidx.paging.compose.LazyPagingItems<Book>,
    favoriteIds: Set<String>,
    onFavoriteClicked: (Book) -> Unit
) {
    var appendRetryCount by remember { mutableStateOf(0) }
    val appendState = books.loadState.append

    LaunchedEffect(appendState) {
        when (appendState) {
            is LoadState.Error -> {
                if (appendRetryCount < MAX_APPEND_RETRIES) {
                    appendRetryCount += 1
                    delay((appendRetryCount * 700L).coerceAtMost(2_000L))
                    books.retry()
                }
            }

            is LoadState.NotLoading -> appendRetryCount = 0
            is LoadState.Loading -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val refreshState = books.loadState.refresh
        when {
            refreshState is LoadState.Loading -> {
                PagingShimmerGrid(modifier = Modifier.padding(paddingValues))
            }

            refreshState is LoadState.Error -> {
                PagingError(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onRetry = books::retry
                )
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        count = books.itemCount,
                        key = { index ->
                            val id = books[index]?.id.orEmpty()
                            if (id.isBlank()) "placeholder_$index" else "${id}_$index"
                        }
                    ) { index ->
                        val item = books[index]
                        if (item != null) {
                            BooksCard(
                                book = item.copy(isFavorite = favoriteIds.contains(item.id)),
                                onBookClicked = onBookClicked,
                                onFavoriteClicked = onFavoriteClicked
                            )
                        }
                    }

                    if (books.loadState.append is LoadState.Loading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    if (books.loadState.append is LoadState.Error && appendRetryCount >= MAX_APPEND_RETRIES) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = stringResource(id = R.string.loading_failed),
                                style = MaterialTheme.typography.caption,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val MAX_APPEND_RETRIES = 3

@Composable
private fun PagingError(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.loading_failed),
            style = MaterialTheme.typography.h6
        )
        Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

@Composable
private fun PagingShimmerGrid(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(8) {
            ShimmerBookCard()
        }
    }
}

@Composable
fun ShimmerBookCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(16.dp)
                    .alpha(alpha)
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(12.dp)
                    .alpha(alpha)
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .alpha(alpha)
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
            )
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(10.dp)
                    .alpha(alpha)
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
            )
        }
    }
}

@Preview
@Composable
fun ShimmerBookCardPreview() {
    ShimmerBookCard()
}

