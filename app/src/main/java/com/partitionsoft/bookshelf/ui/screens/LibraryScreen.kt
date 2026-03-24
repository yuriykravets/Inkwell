package com.partitionsoft.bookshelf.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.ReaderDocument
import com.partitionsoft.bookshelf.ui.LibraryEvent
import com.partitionsoft.bookshelf.ui.LibraryViewModel
import com.partitionsoft.bookshelf.ui.components.InkwellEmptyStateCard
import com.partitionsoft.bookshelf.ui.components.InkwellTopBar
import com.partitionsoft.bookshelf.ui.theme.LocalSpacing

private val supportedReaderMimeTypes = arrayOf(
    "application/pdf",
    "application/epub+zip",
    "application/x-fictionbook+xml",
    "application/fb2+xml",
    "application/xml",
    "text/xml",
    "*/*"
)

@Composable
fun LibraryRoute(
    onBackClicked: () -> Unit,
    onOpenDocument: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val spacing = LocalSpacing.current

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.importDocument(uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.OpenDocument -> onOpenDocument(event.id)
                is LibraryEvent.Message -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            InkwellTopBar(
                title = stringResource(id = R.string.library_title),
                onBackClick = onBackClicked,
                backContentDescription = stringResource(id = R.string.back),
                actions = {
                    IconButton(onClick = { importLauncher.launch(supportedReaderMimeTypes) }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(id = R.string.import_book)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingScreen(modifier = Modifier.fillMaxSize().padding(paddingValues))
            uiState.documents.isEmpty() -> {
                EmptyLibraryState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onImportClicked = { importLauncher.launch(supportedReaderMimeTypes) }
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(spacing.md),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    items(uiState.documents, key = { it.id }) { document ->
                        LibraryDocumentRow(document = document, onOpen = { onOpenDocument(document.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryDocumentRow(
    document: ReaderDocument,
    onOpen: () -> Unit
) {
    val spacing = LocalSpacing.current
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardShape = RoundedCornerShape(16.dp)
    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(if (isDarkTheme) 8.dp else 2.dp)
    val borderColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.34f)
    }

    Card(
        modifier = Modifier
            .shadow(
                elevation = if (isDarkTheme) 14.dp else 6.dp,
                shape = cardShape,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.22f else 0.10f),
                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = if (isDarkTheme) 0.38f else 0.12f)
            )
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = cardShape,
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 8.dp else 3.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                ) {
                    Text(
                        text = document.format.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (!document.lastLocation.isNullOrBlank()) {
                    Text(
                        text = stringResource(id = R.string.continue_reading_at, document.lastLocation),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryState(
    modifier: Modifier = Modifier,
    onImportClicked: () -> Unit
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier.padding(horizontal = spacing.lg),
        verticalArrangement = Arrangement.Center
    ) {
        InkwellEmptyStateCard(
            title = stringResource(id = R.string.library_empty_title),
            message = stringResource(id = R.string.library_empty_subtitle),
            actionLabel = stringResource(id = R.string.import_book),
            onActionClick = onImportClicked
        )
    }
}

