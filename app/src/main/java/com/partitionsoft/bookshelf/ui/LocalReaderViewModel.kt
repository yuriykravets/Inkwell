package com.partitionsoft.bookshelf.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partitionsoft.bookshelf.domain.model.ReaderDocument
import com.partitionsoft.bookshelf.domain.model.ReaderDocumentFormat
import com.partitionsoft.bookshelf.domain.repository.ReaderRepository
import com.partitionsoft.bookshelf.ui.navigation.BooksDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LocalReaderUiState {
    data object Loading : LocalReaderUiState
    data class Ready(val document: ReaderDocument) : LocalReaderUiState
    data object Unsupported : LocalReaderUiState
    data object Error : LocalReaderUiState
}

@HiltViewModel
class LocalReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val readerRepository: ReaderRepository
) : ViewModel() {

    private val documentId: Long? = savedStateHandle[BooksDestinations.DOCUMENT_ID_ARG]

    private val _uiState = MutableStateFlow<LocalReaderUiState>(LocalReaderUiState.Loading)
    val uiState: StateFlow<LocalReaderUiState> = _uiState.asStateFlow()

    init {
        loadDocument()
    }

    fun loadDocument() {
        viewModelScope.launch {
            val safeDocumentId = documentId
            if (safeDocumentId == null) {
                _uiState.value = LocalReaderUiState.Error
                return@launch
            }
            val document = readerRepository.getDocument(safeDocumentId)
            _uiState.value = when {
                document == null -> LocalReaderUiState.Error
                document.format == ReaderDocumentFormat.UNKNOWN -> LocalReaderUiState.Unsupported
                else -> LocalReaderUiState.Ready(document)
            }
        }
    }

    fun updateProgress(location: String) {
        viewModelScope.launch {
            val safeDocumentId = documentId ?: return@launch
            readerRepository.updateProgress(safeDocumentId, location)
        }
    }
}

