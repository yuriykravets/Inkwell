package com.partitionsoft.bookshelf.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partitionsoft.bookshelf.domain.model.ReaderDocument
import com.partitionsoft.bookshelf.domain.repository.ReaderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val isLoading: Boolean = true,
    val documents: List<ReaderDocument> = emptyList(),
    val errorMessage: String? = null
)

sealed interface LibraryEvent {
    data class OpenDocument(val id: Long) : LibraryEvent
    data class Message(val text: String) : LibraryEvent
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val readerRepository: ReaderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LibraryEvent>()
    val events: SharedFlow<LibraryEvent> = _events.asSharedFlow()

    init {
        readerRepository.observeLibrary()
            .onEach { docs ->
                _uiState.value = LibraryUiState(
                    isLoading = false,
                    documents = docs,
                    errorMessage = null
                )
            }
            .launchIn(viewModelScope)
    }

    fun importDocument(uri: Uri) {
        viewModelScope.launch {
            val result = readerRepository.importDocument(uri)
            result.onSuccess { document ->
                _events.emit(LibraryEvent.OpenDocument(document.id))
            }.onFailure {
                _events.emit(LibraryEvent.Message("Unsupported or unreadable file"))
            }
        }
    }
}

