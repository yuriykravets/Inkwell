package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.model.optimizedBookCoverUrlOrNull
import com.partitionsoft.bookshelf.ui.theme.LocalSpacing

@Composable
fun BooksGridScreen(
    books: List<Book>,
    modifier: Modifier = Modifier,
    onBookClicked: (Book) -> Unit,
    onFavoriteClicked: (Book) -> Unit = {}
) {
    val gridState = rememberLazyGridState()
    val spacing = LocalSpacing.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        itemsIndexed(
            items = books,
            key = { index, book ->
                if (book.id.isBlank()) "placeholder_$index" else "${book.id}_$index"
            }
        ) { _, book ->
            BooksCard(
                book = book,
                onBookClicked = onBookClicked,
                onFavoriteClicked = onFavoriteClicked
            )
        }
    }
}

@Composable
fun BooksCard(
    book: Book,
    modifier: Modifier = Modifier,
    onBookClicked: (Book) -> Unit,
    onFavoriteClicked: (Book) -> Unit = {}
) {
    val spacing = LocalSpacing.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onBookClicked(book) },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            BookCover(
                thumbnail = book.thumbnail,
                title = book.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                useDefaultAspectRatio = false
            )
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                FavoriteToggleButton(
                    isFavorite = book.isFavorite,
                    onClick = { onFavoriteClicked(book) },
                    modifier = Modifier.align(Alignment.TopEnd),
                    style = FavoriteToggleStyle.Compact,
                    buttonSize = 34.dp,
                    iconSize = 20.dp
                )
            }
            BookRating(
                rating = book.rating,
                ratingsCount = book.ratingsCount,
                compact = true
            )
            if (book.authors.isNotEmpty()) {
                Text(
                    text = book.authors.joinToString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!book.publishedDate.isNullOrBlank()) {
                Text(
                    text = stringResource(id = R.string.published_in, book.publishedDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BookCover(
    thumbnail: String?,
    title: String,
    modifier: Modifier = Modifier,
    useDefaultAspectRatio: Boolean = true
) {
    val placeholder = painterResource(id = R.drawable.ic_book_96)
    val coverModifier = if (useDefaultAspectRatio) {
        modifier
            .fillMaxWidth()
            .aspectRatio(0.66f)
    } else {
        modifier.fillMaxWidth()
    }
    val imageContentScale = if (useDefaultAspectRatio) {
        ContentScale.Crop
    } else {
        ContentScale.FillWidth
    }
    Box(
        modifier = coverModifier,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(thumbnail.optimizedBookCoverUrlOrNull())
                .crossfade(true)
                .allowRgb565(false)
                .precision(Precision.INEXACT)
                .build(),
            fallback = placeholder,
            error = placeholder,
            placeholder = painterResource(id = R.drawable.loading_img),
            contentDescription = stringResource(
                id = R.string.book_cover_content_description,
                title
            ),
            contentScale = imageContentScale,
            filterQuality = FilterQuality.High
        )
    }
}