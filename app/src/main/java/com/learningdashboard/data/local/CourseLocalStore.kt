package com.learningdashboard.data.local

import com.learningdashboard.domain.model.Course
import kotlinx.coroutines.flow.Flow

interface CourseLocalStore {
    fun observeCourses(): Flow<List<Course>>

    fun observeCourse(courseId: Int): Flow<Course?>

    suspend fun getCourses(): List<Course>

    suspend fun replaceAll(courses: List<Course>)

    suspend fun clear()

    suspend fun markLessonComplete(courseId: Int, lessonId: Int)
}
