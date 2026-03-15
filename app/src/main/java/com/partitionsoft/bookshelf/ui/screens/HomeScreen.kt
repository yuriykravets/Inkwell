package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.model.BookCategory
import com.partitionsoft.bookshelf.domain.model.BookSection
import com.partitionsoft.bookshelf.domain.model.SectionLayout
import com.partitionsoft.bookshelf.ui.BooksUiState
import com.partitionsoft.bookshelf.ui.CategoryShelfUiState
import com.partitionsoft.bookshelf.ui.HomeUiState

@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    categoryShelfUiState: CategoryShelfUiState,
    booksUiState: BooksUiState,
    isSearchActive: Boolean,
    retryAction: () -> Unit,
    onCategorySelected: (BookCategory) -> Unit,
    modifier: Modifier = Modifier,
    onBookClicked: (Book) -> Unit,
    onSearchRetry: () -> Unit
) {
    if (isSearchActive) {
        SearchResults(
            booksUiState = booksUiState,
            onRetry = onSearchRetry,
            modifier = modifier,
            onBookClicked = onBookClicked
        )
        return
    }

    when {
        homeUiState.isLoading -> LoadingScreen(modifier = modifier.fillMaxSize())
        homeUiState.error != null -> ErrorScreen(
            retryAction = retryAction,
            modifier = modifier.fillMaxSize()
        )

        else -> HomeFeedList(
            homeUiState = homeUiState,
            categoryShelfUiState = categoryShelfUiState,
            onCategorySelected = onCategorySelected,
            modifier = modifier,
            onBookClicked = onBookClicked
        )
    }
}

@Composable
private fun SearchResults(
    booksUiState: BooksUiState,
    onRetry: () -> Unit,
    modifier: Modifier,
    onBookClicked: (Book) -> Unit
) {
    when (booksUiState) {
        BooksUiState.Loading -> LoadingScreen(modifier = modifier.fillMaxSize())
        is BooksUiState.Success -> {
            if (booksUiState.bookSearch.isEmpty()) {
                EmptyScreen(onRefresh = onRetry)
            } else {
                BooksGridScreen(
                    books = booksUiState.bookSearch,
                    modifier = Modifier.fillMaxSize(),
                    onBookClicked = onBookClicked
                )
            }
        }

        BooksUiState.Error -> ErrorScreen(
            retryAction = onRetry,
            modifier = modifier.fillMaxSize()
        )
    }
}

@Composable
private fun HomeFeedList(
    homeUiState: HomeUiState,
    categoryShelfUiState: CategoryShelfUiState,
    onCategorySelected: (BookCategory) -> Unit,
    modifier: Modifier,
    onBookClicked: (Book) -> Unit
) {
    val selectedCategory =
        remember(homeUiState.categories, categoryShelfUiState.selectedCategoryId) {
            homeUiState.categories.firstOrNull { it.id == categoryShelfUiState.selectedCategoryId }
        }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
    ) {
        if (homeUiState.featured.isNotEmpty()) {
            item(key = "featured") {
                SectionHeader(title = stringResource(id = R.string.home_featured_title))
                FeaturedSection(books = homeUiState.featured, onBookClicked = onBookClicked)
            }
        }
        if (homeUiState.categories.isNotEmpty()) {
            item(key = "categories") {
                SectionHeader(title = stringResource(id = R.string.home_categories_title))
                CategoryRow(
                    categories = homeUiState.categories,
                    selectedId = categoryShelfUiState.selectedCategoryId,
                    onCategorySelected = onCategorySelected
                )
            }
        }
        if (categoryShelfUiState.isLoading || categoryShelfUiState.books.isNotEmpty() || categoryShelfUiState.error != null) {
            item(key = "category_shelf") {
                CategoryShelfSection(
                    selectedCategory = selectedCategory,
                    categoryShelfUiState = categoryShelfUiState,
                    onRetry = selectedCategory?.let { { onCategorySelected(it) } },
                    onBookClicked = onBookClicked
                )
            }
        }
        items(items = homeUiState.sections, key = { it.id }) { section ->
            HomeSection(section = section, onBookClicked = onBookClicked)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.h6,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun FeaturedSection(
    books: List<Book>,
    onBookClicked: (Book) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(books, key = { it.id }) { book ->
            FeaturedBookCard(book = book, onBookClicked = onBookClicked)
        }
    }
}

@Composable
private fun FeaturedBookCard(
    book: Book,
    onBookClicked: (Book) -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable { onBookClicked(book) },
        elevation = 10.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            BookCover(
                thumbnail = book.thumbnail,
                title = book.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                useDefaultAspectRatio = false
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = book.title,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (book.authors.isNotEmpty()) {
                Text(
                    text = book.authors.joinToString(),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    categories: List<BookCategory>,
    selectedId: String?,
    onCategorySelected: (BookCategory) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            val isSelected = category.id == selectedId
            CategoryChip(
                text = category.title,
                selected = isSelected,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val targetBackground = if (selected) {
        MaterialTheme.colors.primary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
    }
    val targetContent =
        if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
    val background by animateColorAsState(
        targetValue = targetBackground,
        animationSpec = tween(250),
        label = "chip_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = targetContent,
        animationSpec = tween(250),
        label = "chip_text"
    )

    Card(
        modifier = Modifier.clickable { onClick() },
        backgroundColor = background
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = contentColor,
            style = MaterialTheme.typography.body2
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CategoryShelfSection(
    selectedCategory: BookCategory?,
    categoryShelfUiState: CategoryShelfUiState,
    onRetry: (() -> Unit)?,
    onBookClicked: (Book) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val title = selectedCategory?.title ?: stringResource(id = R.string.home_categories_title)
        Text(
            text = stringResource(id = R.string.home_category_shelf_title, title),
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        AnimatedContent(
            targetState = categoryShelfUiState,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(200)
                ) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "category_shelf"
        ) { state ->
            when {
                state.isLoading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Text(
                        text = stringResource(id = R.string.home_category_error),
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body2
                    )
                    if (onRetry != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRetry) {
                            Text(text = stringResource(id = R.string.retry))
                        }
                    }
                }

                state.books.isEmpty() -> {
                    Text(
                        text = stringResource(id = R.string.empty_results_supporting),
                        style = MaterialTheme.typography.caption
                    )
                }

                else -> {
                    LazyRow(
                        contentPadding = PaddingValues(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.books, key = { it.id }) { book ->
                            HorizontalBookCard(book = book, onBookClicked = onBookClicked)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSection(
    section: BookSection,
    onBookClicked: (Book) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        when (section.layout) {
            SectionLayout.Carousel -> FeaturedSection(
                books = section.books,
                onBookClicked = onBookClicked
            )

            SectionLayout.Horizontal -> LazyRow(
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(section.books, key = { it.id }) { book ->
                    HorizontalBookCard(book = book, onBookClicked = onBookClicked)
                }
            }
        }
    }
}

@Composable
private fun HorizontalBookCard(
    book: Book,
    onBookClicked: (Book) -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onBookClicked(book) }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            BookCover(
                thumbnail = book.thumbnail,
                title = book.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                useDefaultAspectRatio = false
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = book.title,
                style = MaterialTheme.typography.body2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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