package com.learningdashboard.data.remote

interface CourseApi {
    suspend fun getCourses(): List<CourseDto>
}
