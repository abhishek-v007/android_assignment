package com.learningdashboard.data

import com.learningdashboard.data.local.CourseLocalStore
import com.learningdashboard.data.remote.CourseApi
import com.learningdashboard.data.remote.NoNetworkException
import com.learningdashboard.data.remote.toCourse
import com.learningdashboard.domain.model.Course
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class CourseRepository(
    private val api: CourseApi,
    private val localStore: CourseLocalStore,
) {
    fun observeCourses(): Flow<List<Course>> = localStore.observeCourses()

    fun observeCourse(courseId: Int): Flow<Course?> = localStore.observeCourse(courseId)

    suspend fun refresh(): RefreshResult {
        return try {
            val remote = api.getCourses().map { it.toCourse() }
            if (remote.isEmpty()) {
                localStore.clear()
                RefreshResult.Empty
            } else {
                val merged = mergeCompletion(
                    existing = localStore.getCourses(),
                    incoming = remote,
                )
                localStore.replaceAll(merged)
                RefreshResult.Success
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: NoNetworkException) {
            failure(offline = true)
        } catch (error: Exception) {
            failure(offline = false)
        }
    }

    suspend fun markLessonComplete(courseId: Int, lessonId: Int) {
        localStore.markLessonComplete(courseId, lessonId)
    }

    private suspend fun failure(offline: Boolean): RefreshResult.Failure =
        RefreshResult.Failure(
            offline = offline,
            hasCache = localStore.getCourses().isNotEmpty(),
        )
}

/** Keeps a local completed flag when the same lesson id is returned again. */
internal fun mergeCompletion(existing: List<Course>, incoming: List<Course>): List<Course> {
    val completedKeys = existing.flatMap { course ->
        course.lessons.filter { it.completed }.map { course.id to it.id }
    }.toSet()
    return incoming.map { course ->
        course.copy(
            lessons = course.lessons.map { lesson ->
                if ((course.id to lesson.id) in completedKeys) {
                    lesson.copy(completed = true)
                } else {
                    lesson
                }
            },
        )
    }
}
