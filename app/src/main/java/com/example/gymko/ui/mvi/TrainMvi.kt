package com.example.gymko.ui.mvi

import com.example.gymko.data.local.entity.ExerciseEntity
import com.example.gymko.data.local.entity.WorkoutEntity
import com.example.gymko.data.local.relation.WorkoutWithSets

data class TrainState(
    val searchQuery: String = "",
    val selectedTab: TrainTab = TrainTab.Workouts,
    val exercises: List<ExerciseEntity> = emptyList(),
    val workouts: List<WorkoutWithSets> = emptyList(),
    
    // Dialog States
    val exerciseDialog: ExerciseDialogState? = null,
    val workoutDialog: WorkoutDialogState? = null, // Used for the "add exercise to workout" search
    val deleteExerciseConfirmation: ExerciseEntity? = null,
    val deleteWorkoutConfirmation: WorkoutEntity? = null,
    
    // Create Workout State
    val editingWorkoutId: Long? = null,
    val createWorkoutName: String = "",
    val createWorkoutExercises: List<WorkoutExerciseState> = emptyList(),
    val showAddExerciseToWorkoutDialog: Boolean = false,
    val exerciseSearchQuery: String = ""
) : MviState

data class WorkoutExerciseState(
    val exercise: ExerciseEntity,
    val sets: List<WorkoutSetState> = listOf(WorkoutSetState())
)

data class WorkoutSetState(
    val weight: String = "",
    val reps: String = ""
)

enum class TrainTab {
    Exercises, Workouts
}

data class ExerciseDialogState(
    val id: Long? = null, // null for add, non-null for edit
    val name: String = "",
    val description: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val selectedMuscles: Set<String> = emptySet(),
    val muscleSearchQuery: String = ""
)

data class WorkoutDialogState(
    val id: Long? = null,
    val name: String = ""
)

sealed class TrainIntent : MviIntent {
    data class UpdateSearchQuery(val query: String) : TrainIntent()
    data class SelectTab(val tab: TrainTab) : TrainIntent()
    
    // Exercise Actions
    object ShowAddExerciseDialog : TrainIntent()
    data class ShowEditExerciseDialog(val exercise: ExerciseEntity) : TrainIntent()
    data class UpdateExerciseDialogName(val name: String) : TrainIntent()
    data class UpdateExerciseDialogDescription(val description: String) : TrainIntent()
    data class ToggleExerciseDialogCategory(val category: String) : TrainIntent()
    data class UpdateExerciseDialogMuscleSearch(val query: String) : TrainIntent()
    data class ToggleExerciseDialogMuscle(val muscle: String) : TrainIntent()
    object SaveExercise : TrainIntent()
    object DismissExerciseDialog : TrainIntent()
    
    // Workout Actions
    data class ShowEditWorkoutScreen(val workoutWithSets: WorkoutWithSets) : TrainIntent()
    data class StartWorkout(val workoutId: Long) : TrainIntent()

    // Create Workout Actions
    object ShowCreateWorkoutScreen : TrainIntent()
    data class UpdateCreateWorkoutName(val name: String) : TrainIntent()
    object ShowAddExerciseToWorkoutDialog : TrainIntent()
    object DismissAddExerciseToWorkoutDialog : TrainIntent()
    data class UpdateExerciseSearchQuery(val query: String) : TrainIntent()
    data class AddExerciseToWorkout(val exercise: ExerciseEntity) : TrainIntent()
    data class RemoveExerciseFromWorkout(val index: Int) : TrainIntent()
    data class AddSetToExercise(val exerciseIndex: Int) : TrainIntent()
    data class RemoveSetFromExercise(val exerciseIndex: Int, val setIndex: Int) : TrainIntent()
    data class UpdateSetWeight(val exerciseIndex: Int, val setIndex: Int, val weight: String) : TrainIntent()
    data class UpdateSetReps(val exerciseIndex: Int, val setIndex: Int, val reps: String) : TrainIntent()
    object SaveNewWorkout : TrainIntent()
    object CancelCreateWorkout : TrainIntent()

    // Delete Actions
    data class ShowDeleteExerciseConfirmation(val exercise: ExerciseEntity) : TrainIntent()
    data class ShowDeleteWorkoutConfirmation(val workout: WorkoutEntity) : TrainIntent()
    object DeleteExercise : TrainIntent()
    object DeleteWorkout : TrainIntent()
    object DismissDeleteConfirmation : TrainIntent()
}

sealed class TrainEffect : MviEffect {
    object NavigateToCreateWorkout : TrainEffect()
    object NavigateBack : TrainEffect()
    data class StartWorkoutSession(val workoutId: Long) : TrainEffect()
}
