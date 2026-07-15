package com.example.gymko.ui.mvi

import com.example.gymko.data.local.relation.WorkoutWithSets
import com.example.gymko.data.local.entity.ExerciseEntity

data class OverviewState(
    val workouts: List<WorkoutWithSets> = emptyList(),
    val recentActivity: List<WorkoutWithSets> = emptyList(), // History
    val selectedWorkout: WorkoutWithSets? = null,
    val exerciseDialog: ExerciseDialogState? = null,
    val isLoading: Boolean = false
) : MviState

sealed class OverviewIntent : MviIntent {
    data class SelectWorkout(val workout: WorkoutWithSets) : OverviewIntent()
    data class StartWorkout(val workoutId: Long) : OverviewIntent()
    object CreateFirstWorkout : OverviewIntent()
    object AddNewExercise : OverviewIntent()
    object SeeAllWorkouts : OverviewIntent()
    object SeeHistory : OverviewIntent()
    
    // Exercise Dialog actions (reusing logic)
    object ShowAddExerciseDialog : OverviewIntent()
    data class UpdateExerciseDialogName(val name: String) : OverviewIntent()
    data class UpdateExerciseDialogDescription(val description: String) : OverviewIntent()
    data class ToggleExerciseDialogCategory(val category: String) : OverviewIntent()
    data class UpdateExerciseDialogMuscleSearch(val query: String) : OverviewIntent()
    data class ToggleExerciseDialogMuscle(val muscle: String) : OverviewIntent()
    object SaveExercise : OverviewIntent()
    object DismissExerciseDialog : OverviewIntent()
}

sealed class OverviewEffect : MviEffect {
    object NavigateToCreateWorkout : OverviewEffect()
    object NavigateToTrain : OverviewEffect()
    object NavigateToHistory : OverviewEffect()
    data class StartWorkoutSession(val workoutId: Long) : OverviewEffect()
}
