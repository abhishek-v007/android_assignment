package com.learningdashboard.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.learningdashboard.domain.model.Lesson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailsScreen(
    uiState: CourseDetailsUiState,
    onBack: () -> Unit,
    onMarkComplete: (Int) -> Unit,
) {
    val title = (uiState as? CourseDetailsUiState.Ready)?.course?.title ?: "Course"
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState) {
                CourseDetailsUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                CourseDetailsUiState.Missing -> {
                    Text(
                        text = "This course is not available.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                    )
                }

                is CourseDetailsUiState.Ready -> {
                    val course = uiState.course
                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    "${course.progressPercent}% complete",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                LinearProgressIndicator(
                                    progress = { course.progressPercent / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                        items(course.lessons, key = { it.id }) { lesson ->
                            LessonRow(lesson = lesson, onMarkComplete = { onMarkComplete(lesson.id) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, onMarkComplete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(if (lesson.completed) "✓" else "○")
        Column(Modifier.weight(1f)) {
            Text(lesson.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = if (lesson.completed) "Completed" else "Pending",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!lesson.completed) {
            TextButton(onClick = onMarkComplete) { Text("Mark complete") }
        }
    }
}
