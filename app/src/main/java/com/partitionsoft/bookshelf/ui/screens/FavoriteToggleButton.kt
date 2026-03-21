package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.bookshelf.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

enum class FavoriteToggleStyle {
    Compact,
    Overlay
}

@Composable
fun FavoriteToggleButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: FavoriteToggleStyle = FavoriteToggleStyle.Compact,
    buttonSize: Dp = 40.dp,
    iconSize: Dp = 22.dp
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.favorite_burst)
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var playBurst by remember { mutableStateOf(false) }

    LaunchedEffect(isFavorite) {
        if (isFavorite) {
            playBurst = true
        }
    }

    val burstProgress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = playBurst,
        iterations = 1,
        restartOnPlay = true
    )

    LaunchedEffect(playBurst, burstProgress) {
        if (playBurst && burstProgress >= 0.999f) {
            playBurst = false
        }
    }

    val favoriteScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.42f, stiffness = 280f),
        label = "favorite_scale"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
        label = "favorite_press"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            style == FavoriteToggleStyle.Overlay && isFavorite -> MaterialTheme.colors.primary.copy(alpha = 0.24f)
            style == FavoriteToggleStyle.Overlay -> MaterialTheme.colors.surface.copy(alpha = 0.92f)
            isFavorite -> MaterialTheme.colors.primary.copy(alpha = 0.14f)
            else -> MaterialTheme.colors.onSurface.copy(alpha = 0.06f)
        },
        animationSpec = tween(durationMillis = 220),
        label = "favorite_container"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isFavorite) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.82f),
        animationSpec = tween(durationMillis = 210),
        label = "favorite_tint"
    )

    val elevation by animateFloatAsState(
        targetValue = when {
            isPressed -> 3f
            style == FavoriteToggleStyle.Overlay -> 8f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 130),
        label = "favorite_elevation"
    )

    Surface(
        modifier = modifier.shadow(elevation = elevation.dp, shape = CircleShape),
        shape = CircleShape,
        color = containerColor
    ) {
        IconButton(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier
                .size(buttonSize)
                .graphicsLayer {
                    val combinedScale = favoriteScale * pressScale
                    scaleX = combinedScale
                    scaleY = combinedScale
                }
        ) {
            Box(
                modifier = Modifier.size(buttonSize),
                contentAlignment = Alignment.Center
            ) {
                if (composition != null) {
                    LottieAnimation(
                        composition = composition,
                        progress = { burstProgress },
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                clip = true
                                shape = CircleShape
                            },
                        clipToCompositionBounds = true
                    )
                }
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) {
                        stringResource(id = R.string.remove_from_favorites)
                    } else {
                        stringResource(id = R.string.add_to_favorites)
                    },
                    modifier = Modifier.size(iconSize),
                    tint = iconTint
                )
            }
        }
    }
}

