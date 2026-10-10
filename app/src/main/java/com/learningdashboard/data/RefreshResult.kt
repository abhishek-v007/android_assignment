package com.learningdashboard.data

sealed interface RefreshResult {
    data object Success : RefreshResult

    data object Empty : RefreshResult

    data class Failure(
        val offline: Boolean,
        val hasCache: Boolean,
    ) : RefreshResult
}
