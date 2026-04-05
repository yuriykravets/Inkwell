package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.domain.model.BookCategory
import com.partitionsoft.bookshelf.domain.model.BookSection
import com.partitionsoft.bookshelf.domain.model.ReaderDocument
import com.partitionsoft.bookshelf.domain.model.ReaderDocumentFormat
import com.partitionsoft.bookshelf.domain.model.SectionLayout
import com.partitionsoft.bookshelf.ui.BooksUiState
import com.partitionsoft.bookshelf.ui.CategoryShelfUiState
import com.partitionsoft.bookshelf.ui.HomeUiState
import com.partitionsoft.bookshelf.ui.components.InkwellSectionTitle
import com.partitionsoft.bookshelf.ui.theme.LocalSpacing
import kotlinx.coroutines.delay
import retrofit2.HttpException

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
    onFavoriteClicked: (Book) -> Unit,
    onSearchRetry: () -> Unit,
    onBrowseRequested: (title: String, query: String, orderBy: String?, filter: String?) -> Unit,
    onAiAssistantClicked: () -> Unit,
    onContinueReadingClicked: (Long) -> Unit
) {
    val hasHomeContent = homeUiState.hasRenderableContent()

    if (isSearchActive) {
        SearchResults(
            booksUiState = booksUiState,
            onRetry = onSearchRetry,
            modifier = modifier,
            onBookClicked = onBookClicked,
            onFavoriteClicked = onFavoriteClicked
        )
        return
    }

    when {
        homeUiState.isLoading -> LoadingScreen(modifier = modifier.fillMaxSize())
        homeUiState.error != null && !hasHomeContent -> {
            if (homeUiState.error.isQuotaLimitIssue()) {
                HomeQuotaErrorScreen(
                    retryAction = retryAction,
                    modifier = modifier.fillMaxSize()
                )
            } else {
                ErrorScreen(
                    retryAction = retryAction,
                    modifier = modifier.fillMaxSize()
                )
            }
        }

        else -> HomeFeedList(
            homeUiState = homeUiState,
            homeError = homeUiState.error,
            categoryShelfUiState = categoryShelfUiState,
            onCategorySelected = onCategorySelected,
            modifier = modifier,
            onBookClicked = onBookClicked,
            onFavoriteClicked = onFavoriteClicked,
            onBrowseRequested = onBrowseRequested,
            onAiAssistantClicked = onAiAssistantClicked,
            onContinueReadingClicked = onContinueReadingClicked,
            onRetryHome = retryAction
        )
    }
}

@Composable
private fun HomeQuotaErrorScreen(
    retryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.08f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.home_quota_status_label),
                    style = MaterialTheme.typography.overline,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.home_quota_title),
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = stringResource(id = R.string.home_quota_supporting),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.78f)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = retryAction) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

private fun Throwable.isQuotaLimitIssue(): Boolean {
    return generateSequence(this) { it.cause }.any { throwable ->
        when (throwable) {
            is HttpException -> throwable.code() == 429
            else -> {
                val message = throwable.message.orEmpty().lowercase()
                message.contains("quota") ||
                        message.contains("resource_exhausted") ||
                        message.contains("rate limit")
            }
        }
    }
}

@Composable
private fun SearchResults(
    booksUiState: BooksUiState,
    onRetry: () -> Unit,
    modifier: Modifier,
    onBookClicked: (Book) -> Unit,
    onFavoriteClicked: (Book) -> Unit
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
                    onBookClicked = onBookClicked,
                    onFavoriteClicked = onFavoriteClicked
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
    homeError: Throwable?,
    categoryShelfUiState: CategoryShelfUiState,
    onCategorySelected: (BookCategory) -> Unit,
    modifier: Modifier,
    onBookClicked: (Book) -> Unit,
    onFavoriteClicked: (Book) -> Unit,
    onBrowseRequested: (title: String, query: String, orderBy: String?, filter: String?) -> Unit,
    onAiAssistantClicked: () -> Unit,
    onContinueReadingClicked: (Long) -> Unit,
    onRetryHome: () -> Unit
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
        item(key = "home_intro") {
            HomeIntroCard(onAiAssistantClicked = onAiAssistantClicked)
        }

        homeError?.let { error ->
            item(key = "home_status_banner") {
                HomeInlineStatusCard(
                    isQuotaIssue = error.isQuotaLimitIssue(),
                    onRetry = onRetryHome
                )
            }
        }

        homeUiState.continueReading?.let { document ->
            item(key = "continue_reading") {
                SectionHeader(title = stringResource(id = R.string.home_continue_reading_title))
                ContinueReadingCard(
                    document = document,
                    onOpen = { onContinueReadingClicked(document.id) }
                )
            }
        }

        if (homeUiState.featured.isNotEmpty()) {
            item(key = "featured") {
                SectionHeader(title = stringResource(id = R.string.home_featured_title))
                FeaturedSection(
                    books = homeUiState.featured, onBookClicked = onBookClicked,
                    onFavoriteClicked = onFavoriteClicked
                )
            }
        }
        if (homeUiState.categories.isNotEmpty()) {
            item(key = "categories_block") {
                SectionHeader(title = stringResource(id = R.string.home_categories_title))
                CategoryRow(
                    categories = homeUiState.categories,
                    selectedId = categoryShelfUiState.selectedCategoryId,
                    onCategorySelected = onCategorySelected
                )
                if (
                    categoryShelfUiState.isLoading ||
                    categoryShelfUiState.books.isNotEmpty() ||
                    categoryShelfUiState.error != null
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryShelfSection(
                        selectedCategory = selectedCategory,
                        categoryShelfUiState = categoryShelfUiState,
                        onRetry = selectedCategory?.let { { onCategorySelected(it) } },
                        onBookClicked = onBookClicked,
                        onFavoriteClicked = onFavoriteClicked,
                        onBrowseRequested = onBrowseRequested
                    )
                }
            }
        }
        items(items = homeUiState.sections, key = { it.id }) { section ->
            HomeSection(
                section = section,
                onBookClicked = onBookClicked,
                onFavoriteClicked = onFavoriteClicked,
                onBrowseRequested = onBrowseRequested
            )
        }
    }
}

@Composable
private fun HomeIntroCard(onAiAssistantClicked: () -> Unit) {
    val isLightTheme = MaterialTheme.colors.isLight
    val cardBackground = if (isLightTheme) Color(0xFFF7F3FF) else MaterialTheme.colors.surface
    val borderColor = if (isLightTheme) {
        MaterialTheme.colors.primary.copy(alpha = 0.24f)
    } else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = cardBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.home_intro_title),
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(id = R.string.home_intro_supporting),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )
            TextButton(onClick = onAiAssistantClicked) {
                Text(text = stringResource(id = R.string.home_ai_assistant_action))
            }
        }
    }
}

@Composable
private fun HomeInlineStatusCard(
    isQuotaIssue: Boolean,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isQuotaIssue) {
                    stringResource(id = R.string.home_quota_status_label)
                } else {
                    stringResource(id = R.string.loading_failed)
                },
                style = MaterialTheme.typography.overline,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isQuotaIssue) {
                    stringResource(id = R.string.home_quota_supporting)
                } else {
                    stringResource(id = R.string.error_supporting_copy)
                },
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.82f)
            )
            TextButton(onClick = onRetry) {
                Text(text = stringResource(id = R.string.retry))
            }
        }
    }
}

private fun HomeUiState.hasRenderableContent(): Boolean =
    featured.isNotEmpty() || sections.isNotEmpty() || categories.isNotEmpty() || continueReading != null

@Composable
private fun ContinueReadingCard(
    document: ReaderDocument,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = continueReadingProgressLabel(document),
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.72f)
            )
            TextButton(onClick = onOpen) {
                Text(text = stringResource(id = R.string.home_continue_reading_action))
            }
        }
    }
}

@Composable
private fun continueReadingProgressLabel(document: ReaderDocument): String {
    val index = document.lastLocation?.toIntOrNull()?.plus(1)
    return when (document.format) {
        ReaderDocumentFormat.PDF -> {
            if (index != null && index > 0) {
                stringResource(id = R.string.home_continue_page, index)
            } else {
                document.format.name
            }
        }

        ReaderDocumentFormat.EPUB,
        ReaderDocumentFormat.FB2 -> {
            if (index != null && index > 0) {
                stringResource(id = R.string.home_continue_chapter, index)
            } else {
                document.format.name
            }
        }

        ReaderDocumentFormat.UNKNOWN -> document.format.name
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val spacing = LocalSpacing.current
    InkwellSectionTitle(
        title = title,
        actionLabel = actionLabel,
        onActionClick = onActionClick,
        modifier = Modifier.padding(bottom = spacing.sm)
    )
}

@Composable
private fun FeaturedSection(
    books: List<Book>,
    onBookClicked: (Book) -> Unit,
    onFavoriteClicked: (Book) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(books, key = { it.id }) { book ->
            FeaturedBookCard(
                book = book,
                onBookClicked = onBookClicked,
                onFavoriteClicked = onFavoriteClicked
            )
        }
    }
}

@Composable
private fun FeaturedBookCard(
    book: Book,
    onBookClicked: (Book) -> Unit,
    onFavoriteClicked: (Book) -> Unit
) {
    val isLightTheme = MaterialTheme.colors.isLight
    val cardBackground = if (isLightTheme) Color(0xFFFCFBFF) else MaterialTheme.colors.surface
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable { onBookClicked(book) },
        elevation = 10.dp,
        backgroundColor = cardBackground,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isLightTheme) {
                MaterialTheme.colors.onSurface.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colors.onSurface.copy(alpha = 0.16f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FavoriteToggleButton(
                    isFavorite = book.isFavorite,
                    onClick = { onFavoriteClicked(book) },
                    style = FavoriteToggleStyle.Compact,
                    buttonSize = 36.dp,
                    iconSize = 20.dp
                )
            }
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
            BookRating(
                rating = book.rating,
                ratingsCount = book.ratingsCount,
                compact = true
            )
            Spacer(modifier = Modifier.height(12.dp))
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
    val isLightTheme = MaterialTheme.colors.isLight
    val targetBackground = when {
        selected -> MaterialTheme.colors.primary.copy(alpha = if (isLightTheme) 0.2f else 0.22f)
        isLightTheme -> MaterialTheme.colors.onSurface.copy(alpha = 0.045f)
        else -> MaterialTheme.colors.onSurface.copy(alpha = 0.08f)
    }
    val targetContent = if (selected) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.86f)
    }
    val targetBorder = when {
        selected -> MaterialTheme.colors.primary.copy(alpha = if (isLightTheme) 0.48f else 0.52f)
        isLightTheme -> MaterialTheme.colors.onSurface.copy(alpha = 0.22f)
        else -> MaterialTheme.colors.onSurface.copy(alpha = 0.20f)
    }
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
    val borderColor by animateColorAsState(
        targetValue = targetBorder,
        animationSpec = tween(250),
        label = "chip_border"
    )

    Card(
        modifier = Modifier.clickable { onClick() },
        backgroundColor = background,
        shape = RoundedCornerShape(22.dp),
        elevation = if (selected) 3.dp else 1.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = contentColor,
            style = MaterialTheme.typography.body2,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CategoryShelfSection(
    selectedCategory: BookCategory?,
    categoryShelfUiState: CategoryShelfUiState,
    onRetry: (() -> Unit)?,
    onBookClicked: (Book) -> Unit,
    onFavoriteClicked: (Book) -> Unit,
    onBrowseRequested: (title: String, query: String, orderBy: String?, filter: String?) -> Unit
) {
    var lastAutoRetriedCategoryId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(categoryShelfUiState.error, categoryShelfUiState.selectedCategoryId, onRetry) {
        val selectedId = categoryShelfUiState.selectedCategoryId
        if (categoryShelfUiState.error != null && onRetry != null && selectedId != lastAutoRetriedCategoryId) {
            lastAutoRetriedCategoryId = selectedId
            delay(900)
            onRetry()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val title = selectedCategory?.title ?: stringResource(id = R.string.home_categories_title)
        SectionHeader(
            title = stringResource(id = R.string.home_category_shelf_title, title),
            actionLabel = stringResource(id = R.string.see_all),
            onActionClick = selectedCategory?.let {
                { onBrowseRequested(it.title, it.query, POPULAR_ORDER, null) }
            }
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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.loading),
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.65f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(4) {
                                ShimmerBookCard(modifier = Modifier.width(150.dp))
                            }
                        }
                    }
                }

                state.error != null -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.home_category_error),
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(4) {
                                ShimmerBookCard(modifier = Modifier.width(150.dp))
                            }
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
                            HorizontalBookCard(
                                book = book,
                                onBookClicked = onBookClicked,
                                onFavoriteClicked = onFavoriteClicked
                            )
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
    onBookClicked: (Book) -> Unit,
    onFavoriteClicked: (Book) -> Unit,
    onBrowseRequested: (title: String, query: String, orderBy: String?, filter: String?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SectionHeader(
            title = section.title,
            actionLabel = stringResource(id = R.string.see_all),
            onActionClick = {
                onBrowseRequested(
                    section.title,
                    sectionQuery(section),
                    sectionOrderBy(section),
                    sectionFilter(section)
                )
            }
        )
        when (section.layout) {
            SectionLayout.Carousel -> FeaturedSection(
                books = section.books,
                onBookClicked = onBookClicked,
                onFavoriteClicked = onFavoriteClicked
            )

            SectionLayout.Horizontal -> LazyRow(
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(section.books, key = { it.id }) { book ->
                    HorizontalBookCard(
                        book = book,
                        onBookClicked = onBookClicked,
                        onFavoriteClicked = onFavoriteClicked
                    )
                }
            }
        }
    }
}

private fun sectionQuery(section: BookSection): String = when (section.id) {
    "android_trending" -> "android development"
    "design_spotlight" -> "design thinking"
    "business_moves" -> "subject:business"
    "science_breakthroughs" -> "subject:science"
    else -> section.title
}

private fun sectionOrderBy(section: BookSection): String? = when (section.id) {
    else -> POPULAR_ORDER
}

private fun sectionFilter(section: BookSection): String? = when (section.id) {
    "featured" -> "ebooks"
    else -> null
}

private const val POPULAR_ORDER = "popular"

@Composable
private fun HorizontalBookCard(
    book: Book,
    onBookClicked: (Book) -> Unit,
    onFavoriteClicked: (Book) -> Unit
) {
    val isLightTheme = MaterialTheme.colors.isLight
    val cardBackground = if (isLightTheme) Color(0xFFFCFBFF) else MaterialTheme.colors.surface
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { onBookClicked(book) },
        elevation = 8.dp,
        backgroundColor = cardBackground,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isLightTheme) {
                MaterialTheme.colors.onSurface.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colors.onSurface.copy(alpha = 0.16f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FavoriteToggleButton(
                    isFavorite = book.isFavorite,
                    onClick = { onFavoriteClicked(book) },
                    style = FavoriteToggleStyle.Compact,
                    buttonSize = 34.dp,
                    iconSize = 20.dp
                )
            }
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
            BookRating(
                rating = book.rating,
                ratingsCount = book.ratingsCount,
                compact = true
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