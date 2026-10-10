package com.learningdashboard.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseDao {
    @Transaction
    @Query("SELECT * FROM courses ORDER BY id ASC")
    abstract fun observeCourses(): Flow<List<CourseWithLessons>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :courseId")
    abstract fun observeCourse(courseId: Int): Flow<CourseWithLessons?>

    @Transaction
    @Query("SELECT * FROM courses ORDER BY id ASC")
    abstract suspend fun getCourses(): List<CourseWithLessons>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons")
    abstract suspend fun deleteAllLessons()

    @Query("DELETE FROM courses")
    abstract suspend fun deleteAllCourses()

    @Query(
        "UPDATE lessons SET completed = 1 WHERE courseId = :courseId AND lessonId = :lessonId",
    )
    abstract suspend fun markLessonComplete(courseId: Int, lessonId: Int)

    @Transaction
    open suspend fun replaceAll(courses: List<CourseEntity>, lessons: List<LessonEntity>) {
        deleteAllLessons()
        deleteAllCourses()
        if (courses.isNotEmpty()) insertCourses(courses)
        if (lessons.isNotEmpty()) insertLessons(lessons)
    }
}
