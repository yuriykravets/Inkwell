package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bookshelf.R
import java.util.Locale

@Composable
fun BookRating(
    rating: Double?,
    ratingsCount: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = true
) {
    if (rating == null || rating <= 0.0) return

    val ratingText = String.format(Locale.US, "%.1f", rating)
    val countText = if (ratingsCount > 0) {
        stringResource(id = R.string.rating_count, ratingsCount)
    } else {
        stringResource(id = R.string.rating_no_count)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = stringResource(id = R.string.details_rating),
            tint = MaterialTheme.colors.primary
        )
        Text(
            text = ratingText,
            style = if (compact) MaterialTheme.typography.caption else MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface
        )
        Text(
            text = countText,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.65f)
        )
    }
}

