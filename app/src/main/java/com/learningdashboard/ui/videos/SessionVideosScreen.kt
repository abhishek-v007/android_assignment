package com.learningdashboard.ui.videos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
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
import com.learningdashboard.ui.details.CourseDetailsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionVideosScreen(
    uiState: CourseDetailsUiState,
    onBack: () -> Unit,
) {
    val title = (uiState as? CourseDetailsUiState.Ready)?.course?.title ?: "Session videos"
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
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
                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = "Session videos",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        items(uiState.course.lessons, key = { it.id }) { lesson ->
                            ListItem(
                                headlineContent = { Text(lesson.title) },
                                supportingContent = { Text("Video") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
