package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.partitionsoft.bookshelf.ui.components.InkwellPrimaryButton
import com.partitionsoft.bookshelf.ui.components.InkwellSecondaryButton
import com.partitionsoft.bookshelf.ui.components.InkwellTopBar
import com.partitionsoft.bookshelf.ui.theme.LocalSpacing

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
            InkwellTopBar(
                title = stringResource(id = R.string.book_details),
                onBackClick = onBackClicked,
                backContentDescription = stringResource(id = R.string.back)
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
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        if (book.authors.isNotEmpty()) {
            Text(
                text = book.authors.joinToString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BookRating(
            rating = book.rating,
            ratingsCount = book.ratingsCount,
            compact = false
        )

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box {
                BookCover(
                    thumbnail = book.thumbnail,
                    title = book.title,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
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

        MetadataRow(label = stringResource(id = R.string.details_published), value = book.publishedDate)
        MetadataRow(label = stringResource(id = R.string.details_pages), value = book.pageCount?.toString())
        MetadataRow(label = stringResource(id = R.string.details_language), value = book.language)

        Spacer(modifier = Modifier.height(spacing.xs))

        if (!book.description.isNullOrBlank()) {
            Text(
                text = stringResource(id = R.string.details_about_book),
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = book.description, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(spacing.xs))
        InkwellPrimaryButton(
            text = stringResource(id = R.string.read_preview),
            onClick = { onReadClicked(book.id) },
            enabled = book.embeddable,
            modifier = Modifier.fillMaxWidth()
        )

        if (!book.previewLink.isNullOrBlank()) {
            InkwellSecondaryButton(
                text = stringResource(id = R.string.open_in_browser),
                onClick = { onPreviewClicked(book.previewLink) },
                modifier = Modifier.fillMaxWidth()
            )
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
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
