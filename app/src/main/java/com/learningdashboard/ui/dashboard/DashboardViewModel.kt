package com.learningdashboard.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.learningdashboard.data.CourseRepository
import com.learningdashboard.data.RefreshResult
import com.learningdashboard.domain.model.Course
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Content(
        val courses: List<Course>,
        val banner: String?,
        val isRefreshing: Boolean,
    ) : DashboardUiState

    data class Empty(val isRefreshing: Boolean) : DashboardUiState

    data class Error(
        val message: String,
        val isRefreshing: Boolean,
    ) : DashboardUiState
}

class DashboardViewModel(
    private val courseRepository: CourseRepository,
) : ViewModel() {
    private val refreshPhase = MutableStateFlow<RefreshPhase>(RefreshPhase.Loading)
    private var refreshJob: Job? = null

    val uiState: StateFlow<DashboardUiState> = combine(
        courseRepository.observeCourses(),
        refreshPhase,
    ) { courses, phase ->
        toUiState(courses, phase)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState.Loading,
    )

    init {
        refresh()
    }

    fun retry() = refresh()

    private fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            refreshPhase.value = RefreshPhase.Loading
            refreshPhase.value = when (val result = courseRepository.refresh()) {
                RefreshResult.Success -> RefreshPhase.Success
                RefreshResult.Empty -> RefreshPhase.Empty
                is RefreshResult.Failure -> RefreshPhase.Failure(result)
            }
        }
    }
}

private sealed interface RefreshPhase {
    data object Loading : RefreshPhase
    data object Success : RefreshPhase
    data object Empty : RefreshPhase
    data class Failure(val result: RefreshResult.Failure) : RefreshPhase
}

private fun toUiState(courses: List<Course>, phase: RefreshPhase): DashboardUiState {
    if (courses.isNotEmpty()) {
        val failure = (phase as? RefreshPhase.Failure)?.result
        return DashboardUiState.Content(
            courses = courses,
            banner = failure?.let(::cachedFailureMessage),
            isRefreshing = phase is RefreshPhase.Loading,
        )
    }
    return when (phase) {
        RefreshPhase.Loading -> DashboardUiState.Loading
        RefreshPhase.Success, RefreshPhase.Empty -> DashboardUiState.Empty(isRefreshing = false)
        is RefreshPhase.Failure -> DashboardUiState.Error(
            message = emptyFailureMessage(phase.result),
            isRefreshing = false,
        )
    }
}

private fun cachedFailureMessage(failure: RefreshResult.Failure): String =
    if (failure.offline) {
        "You're offline. Showing saved courses."
    } else {
        "Couldn't refresh courses. Showing saved courses."
    }

private fun emptyFailureMessage(failure: RefreshResult.Failure): String =
    if (failure.offline) {
        "No internet connection. Connect and try again."
    } else {
        "Couldn't load courses. Try again."
    }

class DashboardViewModelFactory(
    private val courseRepository: CourseRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(courseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
