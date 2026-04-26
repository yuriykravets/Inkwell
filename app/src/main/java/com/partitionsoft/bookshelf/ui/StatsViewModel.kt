package com.partitionsoft.bookshelf.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partitionsoft.bookshelf.domain.model.ReadingStatsSnapshot
import com.partitionsoft.bookshelf.domain.model.WeeklyReadingDay
import com.partitionsoft.bookshelf.domain.repository.ReadingStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class StatsUiState(
    val isLoading: Boolean = true,
    val streakDays: Int = 0,
    val todayMinutes: Int = 0,
    val booksRead: Int = 0,
    val pagesRead: Int = 0,
    val totalHours: Double = 0.0,
    val weeklyProgress: List<WeeklyReadingDay> = emptyList()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val readingStatsRepository: ReadingStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        readingStatsRepository.observeReadingStats()
            .onEach { stats -> _uiState.value = stats.toUiState() }
            .launchIn(viewModelScope)
    }

    private fun ReadingStatsSnapshot.toUiState(): StatsUiState = StatsUiState(
        isLoading = false,
        streakDays = streakDays,
        todayMinutes = todayMinutes,
        booksRead = booksRead,
        pagesRead = pagesRead,
        totalHours = totalHours,
        weeklyProgress = weeklyProgress
    )
}

