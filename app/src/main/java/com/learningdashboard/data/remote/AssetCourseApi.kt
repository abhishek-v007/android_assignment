package com.learningdashboard.data.remote

import android.content.Context
import com.learningdashboard.data.network.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Stand-in for a remote course API. The payload is bundled so this assignment
 * needs no backend, but the read is refused while the device is offline so a
 * failed refresh behaves like a failed HTTP call.
 */
class AssetCourseApi(
    private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val json: Json,
) : CourseApi {
    override suspend fun getCourses(): List<CourseDto> {
        if (!networkMonitor.isOnline()) throw NoNetworkException()
        delay(LOADING_DELAY_MS)
        val body = withContext(Dispatchers.IO) {
            context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        }
        return json.decodeFromString(body)
    }

    private companion object {
        const val ASSET_NAME = "courses.json"
        const val LOADING_DELAY_MS = 400L
    }
}
