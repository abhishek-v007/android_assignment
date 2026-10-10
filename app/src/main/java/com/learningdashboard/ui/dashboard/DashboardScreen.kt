package com.learningdashboard.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import com.learningdashboard.R
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learningdashboard.domain.model.Course

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onRetry: () -> Unit,
    onOpenCourse: (Int) -> Unit,
    onViewVideos: (Int) -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Courses") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            painter = painterResource(R.drawable.ic_power),
                            contentDescription = "Log out",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState) {
                DashboardUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is DashboardUiState.Empty -> {
                    Text(
                        text = "No courses yet.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is DashboardUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(uiState.message)
                        Button(onClick = onRetry) { Text("Retry") }
                    }
                }

                is DashboardUiState.Content -> {
                    Column(Modifier.fillMaxSize()) {
                        if (uiState.isRefreshing) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                        }
                        uiState.banner?.let { banner ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = banner,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                TextButton(onClick = onRetry, enabled = !uiState.isRefreshing) {
                                    Text("Retry")
                                }
                            }
                        }
                        LazyColumn(
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.courses, key = { it.id }) { course ->
                                CourseCard(
                                    course = course,
                                    onContinue = { onOpenCourse(course.id) },
                                    onViewVideos = { onViewVideos(course.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCard(
    course: Course,
    onContinue: () -> Unit,
    onViewVideos: () -> Unit,
) {
    if (course.isComplete) {
        Card(modifier = Modifier.fillMaxWidth()) {
            CourseCardContent(course, onContinue, onViewVideos)
        }
    } else {
        Card(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            CourseCardContent(course, onContinue, onViewVideos)
        }
    }
}

@Composable
private fun CourseCardContent(
    course: Course,
    onContinue: () -> Unit,
    onViewVideos: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(course.title, style = MaterialTheme.typography.titleMedium)
        Text(course.instructor, style = MaterialTheme.typography.bodyMedium)
        Text("${course.progressPercent}% · ${course.lessonCount} lessons")
        LinearProgressIndicator(
            progress = { course.progressPercent / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (course.isComplete) {
                Text(
                    text = "Completed",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = onViewVideos) { Text("View videos") }
            } else {
                Button(onClick = onContinue) { Text("Continue") }
            }
        }
    }
}
