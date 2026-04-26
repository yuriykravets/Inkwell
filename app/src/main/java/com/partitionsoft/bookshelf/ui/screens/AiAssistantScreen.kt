package com.partitionsoft.bookshelf.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.Book
import com.partitionsoft.bookshelf.ui.AiAssistantMessage
import com.partitionsoft.bookshelf.ui.AiAssistantUiState
import com.partitionsoft.bookshelf.ui.AiAssistantViewModel
import com.partitionsoft.bookshelf.ui.components.InkwellTopBar

@Composable
fun AiAssistantRoute(
    onBackClicked: () -> Unit,
    onBookClicked: (Book) -> Unit,
    viewModel: AiAssistantViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AiAssistantScreen(
        uiState = state,
        onBackClicked = onBackClicked,
        onBookClicked = onBookClicked,
        onSendPrompt = viewModel::submitPrompt
    )
}

@Composable
fun AiAssistantScreen(
    uiState: AiAssistantUiState,
    onBackClicked: () -> Unit,
    onBookClicked: (Book) -> Unit,
    onSendPrompt: (String) -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }
    val sciFiQuery = stringResource(id = R.string.ai_prompt_sci_fi_query)
    val productivityQuery = stringResource(id = R.string.ai_prompt_productivity_query)

    Scaffold(
        topBar = {
            InkwellTopBar(
                title = stringResource(id = R.string.ai_assistant_title),
                onBackClick = onBackClicked,
                backContentDescription = stringResource(id = R.string.back)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { input = sciFiQuery },
                    label = { Text(text = stringResource(id = R.string.ai_prompt_sci_fi)) }
                )
                AssistChip(
                    onClick = { input = productivityQuery },
                    label = { Text(text = stringResource(id = R.string.ai_prompt_productivity)) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatBubble(message = message)
                }

                items(uiState.recommendations, key = { it.book.id }) { recommendation ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBookClicked(recommendation.book) },
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = recommendation.book.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (recommendation.book.authors.isNotEmpty()) {
                                Text(
                                    text = recommendation.book.authors.joinToString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = recommendation.reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(text = stringResource(id = R.string.ai_assistant_input_hint)) },
                    singleLine = true,
                    enabled = !uiState.isLoading
                )
                FilledIconButton(
                    onClick = {
                        onSendPrompt(input)
                        input = ""
                    },
                    enabled = input.isNotBlank() && !uiState.isLoading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(id = R.string.ai_assistant_send)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: AiAssistantMessage) {
    val background = if (message.isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleText = when {
        message.textResId != null && message.intArg != null -> stringResource(
            id = message.textResId,
            message.intArg
        )
        message.textResId != null -> stringResource(id = message.textResId)
        else -> message.text.orEmpty()
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Text(
            text = bubbleText,
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(background)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


