package com.learningdashboard.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.learningdashboard.data.CourseRepository
import com.learningdashboard.domain.model.Course
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CourseDetailsUiState {
    data object Loading : CourseDetailsUiState
    data class Ready(val course: Course) : CourseDetailsUiState
    data object Missing : CourseDetailsUiState
}

class CourseDetailsViewModel(
    private val courseRepository: CourseRepository,
    private val courseId: Int,
) : ViewModel() {
    val uiState: StateFlow<CourseDetailsUiState> = courseRepository.observeCourse(courseId)
        .map { course ->
            if (course == null) CourseDetailsUiState.Missing else CourseDetailsUiState.Ready(course)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CourseDetailsUiState.Loading,
        )

    fun markComplete(lessonId: Int) {
        viewModelScope.launch {
            courseRepository.markLessonComplete(courseId, lessonId)
        }
    }
}

class CourseDetailsViewModelFactory(
    private val courseRepository: CourseRepository,
    private val courseId: Int,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(CourseDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CourseDetailsViewModel(courseRepository, courseId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
