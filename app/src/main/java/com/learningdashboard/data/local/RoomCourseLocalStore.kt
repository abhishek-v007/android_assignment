package com.learningdashboard.data.local

import com.learningdashboard.data.local.db.CourseDao
import com.learningdashboard.data.local.db.CourseEntity
import com.learningdashboard.data.local.db.CourseWithLessons
import com.learningdashboard.data.local.db.LessonEntity
import com.learningdashboard.domain.model.Course
import com.learningdashboard.domain.model.Lesson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCourseLocalStore(
    private val dao: CourseDao,
) : CourseLocalStore {
    override fun observeCourses(): Flow<List<Course>> =
        dao.observeCourses().map { rows -> rows.map { it.toCourse() } }

    override fun observeCourse(courseId: Int): Flow<Course?> =
        dao.observeCourse(courseId).map { row -> row?.toCourse() }

    override suspend fun getCourses(): List<Course> =
        dao.getCourses().map { it.toCourse() }

    override suspend fun replaceAll(courses: List<Course>) {
        dao.replaceAll(
            courses = courses.map { it.toEntity() },
            lessons = courses.flatMap { it.toLessonEntities() },
        )
    }

    override suspend fun clear() {
        dao.replaceAll(emptyList(), emptyList())
    }

    override suspend fun markLessonComplete(courseId: Int, lessonId: Int) {
        dao.markLessonComplete(courseId, lessonId)
    }
}

private fun CourseWithLessons.toCourse(): Course = Course(
    id = course.id,
    title = course.title,
    instructor = course.instructor,
    lessons = lessons.sortedBy { it.position }.map { lesson ->
        Lesson(
            id = lesson.lessonId,
            title = lesson.title,
            completed = lesson.completed,
        )
    },
)

private fun Course.toEntity(): CourseEntity = CourseEntity(
    id = id,
    title = title,
    instructor = instructor,
)

private fun Course.toLessonEntities(): List<LessonEntity> = lessons.mapIndexed { index, lesson ->
    LessonEntity(
        courseId = id,
        lessonId = lesson.id,
        title = lesson.title,
        completed = lesson.completed,
        position = index,
    )
}
