package com.learningdashboard.domain.model

data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val lessons: List<Lesson>,
) {
    val lessonCount: Int
        get() = lessons.size

    val progressPercent: Int
        get() {
            if (lessons.isEmpty()) return 0
            return lessons.count { it.completed } * 100 / lessons.size
        }

    val isComplete: Boolean
        get() = lessons.isNotEmpty() && progressPercent == 100
}
