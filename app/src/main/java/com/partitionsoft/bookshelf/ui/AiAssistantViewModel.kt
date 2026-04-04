package com.partitionsoft.bookshelf.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookshelf.R
import com.partitionsoft.bookshelf.domain.model.AiRecommendation
import com.partitionsoft.bookshelf.domain.repository.RecommendationRepository
import com.partitionsoft.bookshelf.domain.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiAssistantMessage(
    val text: String? = null,
    val isUser: Boolean,
    val textResId: Int? = null,
    val intArg: Int? = null
)

data class AiAssistantUiState(
    val isLoading: Boolean = false,
    val messages: List<AiAssistantMessage> = emptyList(),
    val recommendations: List<AiRecommendation> = emptyList()
)

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AiAssistantUiState(
            messages = listOf(
                AiAssistantMessage(
                    isUser = false,
                    textResId = R.string.ai_assistant_welcome
                )
            )
        )
    )
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    fun submitPrompt(prompt: String) {
        val cleanedPrompt = prompt.trim()
        if (cleanedPrompt.isBlank()) return

        _uiState.update {
            it.copy(
                isLoading = true,
                messages = it.messages + AiAssistantMessage(text = cleanedPrompt, isUser = true)
            )
        }

        viewModelScope.launch {
            when (val result = recommendationRepository.recommend(cleanedPrompt)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recommendations = result.data,
                            messages = it.messages + AiAssistantMessage(
                                isUser = false,
                                textResId = R.string.ai_assistant_found_recommendations,
                                intArg = result.data.size
                            )
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = it.messages + AiAssistantMessage(
                                isUser = false,
                                textResId = R.string.ai_assistant_load_error
                            )
                        )
                    }
                }
                Result.Loading -> Unit
            }
        }
    }
}


