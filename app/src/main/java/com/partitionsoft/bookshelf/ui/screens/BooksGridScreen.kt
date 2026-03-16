package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.Book

@Composable
fun BooksGridScreen(
    books: List<Book>,
    modifier: Modifier = Modifier,
    onBookClicked: (Book) -> Unit,
    onFavoriteClicked: (Book) -> Unit = {}
) {
    val gridState = rememberLazyGridState()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onBookClicked(book) },
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.subtitle1,
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
            if (book.authors.isNotEmpty()) {
                Text(
                    text = book.authors.joinToString(),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BookCover(thumbnail = book.thumbnail, title = book.title)
            if (!book.publishedDate.isNullOrBlank()) {
                Text(
                    text = stringResource(id = R.string.published_in, book.publishedDate),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
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
    Box(
        modifier = coverModifier,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(thumbnail?.replace("http:", "https:"))
                .crossfade(true)
                .build(),
            error = placeholder,
            placeholder = painterResource(id = R.drawable.loading_img),
            contentDescription = stringResource(
                id = R.string.book_cover_content_description,
                title
            ),
            contentScale = ContentScale.Crop
        )
    }
}