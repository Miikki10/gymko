package com.example.gymko.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gymko.ui.mvi.ActiveWorkoutViewModel
import com.example.gymko.ui.mvi.HistoryViewModel
import com.example.gymko.ui.mvi.OverviewViewModel
import com.example.gymko.ui.mvi.SettingsEvent
import com.example.gymko.ui.mvi.SettingsViewModel
import com.example.gymko.ui.mvi.TrainViewModel
import com.example.gymko.ui.screens.*

@Composable
fun NavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = viewModel(),
    trainViewModel: TrainViewModel = viewModel(),
    overviewViewModel: OverviewViewModel = viewModel(),
    activeWorkoutViewModel: ActiveWorkoutViewModel = viewModel(),
    historyViewModel: HistoryViewModel = viewModel()
) {
    val settingsState by settingsViewModel.state.collectAsState()
    val trainState by trainViewModel.state.collectAsState()
    val overviewState by overviewViewModel.state.collectAsState()
    val activeWorkoutState by activeWorkoutViewModel.state.collectAsState()
    val historyState by historyViewModel.state.collectAsState()

    if (settingsState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(Unit) {
        settingsViewModel.events.collect { event ->
            when (event) {
                SettingsEvent.ProfileSaved -> {
                    navController.navigate(Screen.Overview.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (settingsState.isFirstLaunch) Screen.Onboarding.route else Screen.Overview.route,

        // --- OVDJE SU IZMJENE: Dodane brze i glatke animacije ---
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) }
        // -------------------------------------------------------

    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                state = settingsState,
                onIntent = settingsViewModel::onIntent
            )
        }
        composable(Screen.Overview.route) {
            OverviewScreen(
                state = overviewState,
                onIntent = overviewViewModel::onIntent,
                effect = overviewViewModel.effect,
                navController = navController
            )
        }
        composable(Screen.Train.route) {
            TrainScreen(
                state = trainState,
                onIntent = trainViewModel::onIntent,
                effect = trainViewModel.effect,
                navController = navController
            )
        }
        composable(Screen.CreateWorkout.route) {
            CreateWorkoutScreen(
                state = trainState,
                onIntent = trainViewModel::onIntent,
                effect = trainViewModel.effect,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                state = historyState,
                onIntent = historyViewModel::onIntent,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.ActiveWorkout.route,
            arguments = listOf(androidx.navigation.navArgument("workoutId") { type = androidx.navigation.NavType.LongType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: 0L
            ActiveWorkoutScreen(
                workoutId = workoutId,
                state = activeWorkoutState,
                onIntent = activeWorkoutViewModel::onIntent,
                effect = activeWorkoutViewModel.effect,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                state = settingsState,
                onIntent = settingsViewModel::onIntent
            )
        }
    }
}
