package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.ui.BookDetailsUiState
import com.partitionsoft.bookshelf.ui.BookDetailsViewModel

@Composable
fun BookDetailsRoute(
    onBackClicked: () -> Unit,
    onReadClicked: (String) -> Unit,
    onPreviewClicked: (String) -> Unit,
    viewModel: BookDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        topBar = {
            TopAppBar(
                title = { Text(text = "Book details") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            BookDetailsUiState.Loading -> LoadingScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            BookDetailsUiState.Error -> ErrorScreen(
                retryAction = viewModel::loadBookDetails,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            is BookDetailsUiState.Success -> BookDetailsContent(
                book = state.book,
                contentPadding = paddingValues,
                onReadClicked = onReadClicked,
                onPreviewClicked = onPreviewClicked,
                onFavoriteClicked = viewModel::onFavoriteClicked
            )
        }
    }
}

@Composable
private fun BookDetailsContent(
    book: Book,
    contentPadding: PaddingValues,
    onReadClicked: (String) -> Unit,
    onPreviewClicked: (String) -> Unit,
    onFavoriteClicked: (Book) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = book.title,
            style = MaterialTheme.typography.h5,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        if (book.authors.isNotEmpty()) {
            Text(
                text = book.authors.joinToString(),
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.75f)
            )
        }

        Card(elevation = 8.dp) {
            Box {
                BookCover(
                    thumbnail = book.thumbnail,
                    title = book.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    useDefaultAspectRatio = false
                )
                FavoriteCoverButton(
                    isFavorite = book.isFavorite,
                    onClick = { onFavoriteClicked(book) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
            }
        }

        MetadataRow(label = "Published", value = book.publishedDate)
        MetadataRow(label = "Pages", value = book.pageCount?.toString())
        MetadataRow(label = "Language", value = book.language)
        MetadataRow(
            label = "Rating",
            value = book.rating?.let { "$it (${book.ratingsCount})" }
        )

        if (!book.description.isNullOrBlank()) {
            Text(text = "About this book", style = MaterialTheme.typography.subtitle1)
            Text(text = book.description, style = MaterialTheme.typography.body1)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onReadClicked(book.id) },
            enabled = book.embeddable,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Read preview")
        }

        if (!book.previewLink.isNullOrBlank()) {
            OutlinedButton(
                onClick = { onPreviewClicked(book.previewLink) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Open in browser")
            }
        }
    }
}

@Composable
private fun FavoriteCoverButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FavoriteToggleButton(
        isFavorite = isFavorite,
        onClick = onClick,
        modifier = modifier,
        style = FavoriteToggleStyle.Overlay,
        buttonSize = 50.dp,
        iconSize = 28.dp
    )
}

@Composable
private fun MetadataRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.body2)
        Text(text = value, style = MaterialTheme.typography.body2)
    }
}
