package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
internal fun ReadingSessionLifecycleEffect(
    enabled: Boolean,
    onSessionStart: () -> Unit,
    onSessionStop: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, enabled) {
        if (!enabled) {
            onSessionStop()
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> onSessionStart()
                    Lifecycle.Event.ON_STOP -> onSessionStop()
                    else -> Unit
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)
            onSessionStart()

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                onSessionStop()
            }
        }
    }
}

