package com.learningdashboard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.learningdashboard.di.AppContainer
import com.learningdashboard.ui.dashboard.DashboardScreen
import com.learningdashboard.ui.dashboard.DashboardViewModel
import com.learningdashboard.ui.dashboard.DashboardViewModelFactory
import com.learningdashboard.ui.details.CourseDetailsScreen
import com.learningdashboard.ui.details.CourseDetailsViewModel
import com.learningdashboard.ui.details.CourseDetailsViewModelFactory
import com.learningdashboard.ui.login.LoginScreen
import com.learningdashboard.ui.login.LoginViewModel
import com.learningdashboard.ui.login.LoginViewModelFactory
import com.learningdashboard.ui.videos.SessionVideosScreen
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private object Routes {
    const val Login = "login"
    const val Dashboard = "dashboard"
    const val Course = "course/{courseId}"
    const val Videos = "videos/{courseId}"
    fun course(courseId: Int) = "course/$courseId"
    fun videos(courseId: Int) = "videos/$courseId"
}

@Composable
fun AppNav(container: AppContainer) {
    val navController = rememberNavController()

    // Navigation restores the last screen after process death. The session
    // lives only in memory, so send the user back to login when that happens.
    LaunchedEffect(Unit) {
        val route = navController.currentBackStackEntryFlow
            .map { it.destination.route }
            .filterNotNull()
            .first()
        if (!container.authRepository.isLoggedIn && route != Routes.Login) {
            navController.navigate(Routes.Login) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.Login) {
        composable(Routes.Login) {
            val viewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(container.authRepository),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LoginScreen(
                uiState = uiState,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLogin = viewModel::login,
                onLoggedIn = {
                    navController.navigate(Routes.Dashboard) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Dashboard) {
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(container.courseRepository),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            DashboardScreen(
                uiState = uiState,
                onRetry = viewModel::retry,
                onOpenCourse = { courseId -> navController.navigate(Routes.course(courseId)) },
                onViewVideos = { courseId -> navController.navigate(Routes.videos(courseId)) },
                onLogout = {
                    container.authRepository.logout()
                    navController.navigate(Routes.Login) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.Course,
            arguments = listOf(navArgument("courseId") { type = NavType.IntType }),
        ) { entry ->
            val courseId = entry.arguments?.getInt("courseId") ?: return@composable
            val viewModel: CourseDetailsViewModel = viewModel(
                key = "course-$courseId",
                factory = CourseDetailsViewModelFactory(container.courseRepository, courseId),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            CourseDetailsScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onMarkComplete = viewModel::markComplete,
            )
        }
        composable(
            route = Routes.Videos,
            arguments = listOf(navArgument("courseId") { type = NavType.IntType }),
        ) { entry ->
            val courseId = entry.arguments?.getInt("courseId") ?: return@composable
            val viewModel: CourseDetailsViewModel = viewModel(
                key = "videos-$courseId",
                factory = CourseDetailsViewModelFactory(container.courseRepository, courseId),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SessionVideosScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
