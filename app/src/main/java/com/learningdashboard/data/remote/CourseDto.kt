package com.learningdashboard.data.remote

import com.learningdashboard.domain.model.Course
import com.learningdashboard.domain.model.Lesson
import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val id: Int,
    val title: String,
    val instructor: String,
    val lessons: List<LessonDto> = emptyList(),
)

@Serializable
data class LessonDto(
    val id: Int,
    val title: String,
    val completed: Boolean = false,
)

fun CourseDto.toCourse(): Course = Course(
    id = id,
    title = title,
    instructor = instructor,
    lessons = lessons.map { lesson ->
        Lesson(
            id = lesson.id,
            title = lesson.title,
            completed = lesson.completed,
        )
    },
)
