package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.WeeklyReadingDay
import com.partitionsoft.bookshelf.ui.StatsUiState
import com.partitionsoft.bookshelf.ui.StatsViewModel
import com.partitionsoft.bookshelf.ui.components.InkwellTopBar
import com.partitionsoft.bookshelf.ui.theme.LocalSpacing
import java.util.Locale
import kotlin.math.max

@Composable
fun StatsRoute(
    onBackClicked: (() -> Unit)?,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            InkwellTopBar(
                title = stringResource(id = R.string.stats_title),
                onBackClick = onBackClicked,
                backContentDescription = stringResource(id = R.string.back)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            StatsContent(
                state = uiState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun StatsContent(
    state: StatsUiState,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        StreakCard(
            streakDays = state.streakDays,
            todayMinutes = state.todayMinutes
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            MetricCard(
                title = stringResource(id = R.string.stats_books_read),
                value = state.booksRead.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = stringResource(id = R.string.stats_pages_read),
                value = state.pagesRead.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = stringResource(id = R.string.stats_hours_read),
                value = String.format(Locale.US, "%.1f", state.totalHours),
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                Text(
                    text = stringResource(id = R.string.stats_weekly_progress),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                WeeklyBars(days = state.weeklyProgress)
            }
        }
    }
}

@Composable
private fun StreakCard(streakDays: Int, todayMinutes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(id = R.string.stats_streak_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(id = R.string.stats_streak_days, streakDays),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(id = R.string.stats_today_minutes, todayMinutes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WeeklyBars(days: List<WeeklyReadingDay>) {
    val maxMinutes = max(days.maxOfOrNull { it.minutesRead } ?: 0, 1)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val ratio = day.minutesRead / maxMinutes.toFloat()
            val barColor = if (day.isToday) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }

            val finalBarHeight = 24.dp + (96.dp * ratio)
            var startAnimation by remember(day.label, day.minutesRead) { mutableStateOf(false) }
            LaunchedEffect(day.label, day.minutesRead) {
                startAnimation = true
            }
            val animatedBarHeight by animateDpAsState(
                targetValue = if (day.minutesRead > 0 && startAnimation) finalBarHeight else 24.dp,
                animationSpec = tween(durationMillis = 650),
                label = "weekly_reading_bar_height"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = readingActivityEmoji(
                        minutesRead = day.minutesRead,
                        isToday = day.isToday
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (day.minutesRead > 0) day.minutesRead.toString() else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .width(28.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(animatedBarHeight)
                            .clip(RoundedCornerShape(10.dp))
                            .background(barColor)
                    )
                }
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun readingActivityEmoji(minutesRead: Int, isToday: Boolean): String {
    return when {
        minutesRead >= 120 -> "🔥"
        minutesRead >= 60 -> "📚"
        minutesRead > 0 -> if (isToday) "✨" else "🙂"
        else -> "😴"
    }
}
