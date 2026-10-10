package com.learningdashboard.di

import android.content.Context
import com.learningdashboard.data.AuthRepository
import com.learningdashboard.data.CourseRepository
import com.learningdashboard.data.local.RoomCourseLocalStore
import com.learningdashboard.data.local.db.AppDatabase
import com.learningdashboard.data.network.NetworkMonitor
import com.learningdashboard.data.remote.AssetCourseApi
import kotlinx.serialization.json.Json

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.create(appContext)
    private val json = Json { ignoreUnknownKeys = true }

    val authRepository = AuthRepository()

    val courseRepository = CourseRepository(
        api = AssetCourseApi(
            context = appContext,
            networkMonitor = NetworkMonitor(appContext),
            json = json,
        ),
        localStore = RoomCourseLocalStore(database.courseDao()),
    )
}
