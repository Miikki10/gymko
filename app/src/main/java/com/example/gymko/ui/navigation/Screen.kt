package com.example.gymko.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Overview : Screen("overview", "Overview", Icons.Default.Home)
    object Train : Screen("train", "Train", Icons.Default.FitnessCenter)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Onboarding : Screen("onboarding", "Welcome")
    
    // Workout creation
    object CreateWorkout : Screen("create_workout", "Create Workout")

    // Active workout
    object ActiveWorkout : Screen("active_workout/{workoutId}", "Active Workout") {
        fun createRoute(workoutId: Long) = "active_workout/$workoutId"
    }
}
