package com.example.gymko.ui.mvi

import com.example.gymko.data.local.entity.ExerciseEntity
import com.example.gymko.data.local.relation.WorkoutWithSets

data class ActiveWorkoutState(
    val workout: WorkoutWithSets? = null,
    val durationSeconds: Long = 0,
    val completedSets: Set<Long> = emptySet(), // Set of set IDs
    val isHidden: Boolean = false
) : MviState

sealed class ActiveWorkoutIntent : MviIntent {
    data class LoadWorkout(val workoutId: Long) : ActiveWorkoutIntent()
    data class ToggleSet(val setId: Long) : ActiveWorkoutIntent()
    object ToggleAllSets : ActiveWorkoutIntent()
    data class ToggleExerciseSets(val exerciseId: Long) : ActiveWorkoutIntent()
    object EndWorkout : ActiveWorkoutIntent()
    object HideWorkout : ActiveWorkoutIntent()
    object ShowWorkout : ActiveWorkoutIntent()
}

sealed class ActiveWorkoutEffect : MviEffect {
    object NavigateToOverview : ActiveWorkoutEffect()
    object WorkoutAutoClosed : ActiveWorkoutEffect()
}
