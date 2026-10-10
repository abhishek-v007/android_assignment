package com.learningdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.learningdashboard.ui.navigation.AppNav
import com.learningdashboard.ui.theme.LearningDashboardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as LearningDashboardApp).container
        setContent {
            LearningDashboardTheme {
                AppNav(container)
            }
        }
    }
}
