package com.learningdashboard.data

import com.learningdashboard.data.local.CourseLocalStore
import com.learningdashboard.data.remote.CourseApi
import com.learningdashboard.data.remote.CourseDto
import com.learningdashboard.data.remote.NoNetworkException
import com.learningdashboard.domain.model.Course
import com.learningdashboard.domain.model.Lesson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseRepositoryTest {
    private val api = FakeCourseApi()
    private val localStore = FakeCourseLocalStore()
    private val repository = CourseRepository(api, localStore)

    @Test
    fun failedRefreshKeepsCachedCourses() = runTest {
        val course = sampleCourse()
        localStore.replaceAll(listOf(course))
        api.error = NoNetworkException()

        val result = repository.refresh()

        val failure = result as RefreshResult.Failure
        assertTrue(failure.offline)
        assertTrue(failure.hasCache)
        assertEquals(listOf(course), repository.observeCourses().first())
    }

    @Test
    fun markingLessonsCompleteUpdatesProgress() = runTest {
        localStore.replaceAll(listOf(sampleCourse()))

        repository.markLessonComplete(courseId = 1, lessonId = 1)
        assertEquals(25, repository.observeCourses().first().single().progressPercent)

        listOf(2, 3, 4).forEach { lessonId ->
            repository.markLessonComplete(courseId = 1, lessonId = lessonId)
        }
        assertEquals(100, repository.observeCourses().first().single().progressPercent)
    }
}

private fun sampleCourse(): Course = Course(
    id = 1,
    title = "Sample",
    instructor = "Ada",
    lessons = List(4) { index ->
        Lesson(id = index + 1, title = "Lesson ${index + 1}", completed = false)
    },
)

private class FakeCourseApi : CourseApi {
    var error: Exception? = null

    override suspend fun getCourses(): List<CourseDto> {
        error?.let { throw it }
        return emptyList()
    }
}

private class FakeCourseLocalStore : CourseLocalStore {
    private val courses = MutableStateFlow<List<Course>>(emptyList())

    override fun observeCourses(): Flow<List<Course>> = courses

    override fun observeCourse(courseId: Int): Flow<Course?> =
        courses.map { list -> list.find { it.id == courseId } }

    override suspend fun getCourses(): List<Course> = courses.value

    override suspend fun replaceAll(courses: List<Course>) {
        this.courses.value = courses
    }

    override suspend fun clear() {
        courses.value = emptyList()
    }

    override suspend fun markLessonComplete(courseId: Int, lessonId: Int) {
        courses.update { list ->
            list.map { course ->
                if (course.id != courseId) {
                    course
                } else {
                    course.copy(
                        lessons = course.lessons.map { lesson ->
                            if (lesson.id == lessonId) lesson.copy(completed = true) else lesson
                        },
                    )
                }
            }
        }
    }
}
