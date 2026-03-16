package com.partitionsoft.bookshelf.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text(text = stringResource(id = R.string.library_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
    androidx.compose.material.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = document.format.name,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
                if (!document.lastLocation.isNullOrBlank()) {
                    Text(
                        text = stringResource(id = R.string.continue_reading_at, document.lastLocation),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
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
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.library_empty_title),
            style = MaterialTheme.typography.h6
        )
        Text(
            text = stringResource(id = R.string.library_empty_subtitle),
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )
        androidx.compose.material.Button(onClick = onImportClicked) {
            Text(text = stringResource(id = R.string.import_book))
        }
    }
}

