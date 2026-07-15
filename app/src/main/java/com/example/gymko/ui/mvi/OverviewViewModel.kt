package com.example.gymko.ui.mvi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymko.data.local.database.GymKoDatabase
import com.example.gymko.data.local.entity.ExerciseEntity
import com.example.gymko.data.model.WorkoutStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OverviewViewModel(application: Application) : AndroidViewModel(application), MviViewModel<OverviewState, OverviewIntent> {

    private val dao = GymKoDatabase.getDatabase(application).gymKoDao()

    private val _state = MutableStateFlow(OverviewState())
    override val state: StateFlow<OverviewState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<OverviewEffect>()
    override val effect: SharedFlow<OverviewEffect> = _effect.asSharedFlow()

    init {
        observeData()
    }

    private fun observeData() {
        dao.getAllWorkoutsWithSets().onEach { workouts ->
            val history = workouts.filter { it.workout.status == WorkoutStatus.COMPLETED }
                .sortedByDescending { it.workout.timestamp }
                .take(5)
            
            val selected = workouts.find { it.workout.status == WorkoutStatus.SELECTED }
            
            _state.update { 
                it.copy(
                    workouts = workouts.filter { w -> w.workout.status != WorkoutStatus.COMPLETED }
                        .sortedByDescending { w -> w.workout.timestamp }
                        .take(4),
                    recentActivity = history,
                    selectedWorkout = selected
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: OverviewIntent) {
        when (intent) {
            is OverviewIntent.SelectWorkout -> {
                viewModelScope.launch {
                    dao.deactivateAllWorkouts()
                    dao.updateWorkout(intent.workout.workout.copy(status = WorkoutStatus.SELECTED))
                }
            }
            is OverviewIntent.StartWorkout -> {
                viewModelScope.launch {
                    val newWorkoutId = dao.startWorkoutSession(intent.workoutId)
                    if (newWorkoutId != -1L) {
                        _effect.emit(OverviewEffect.StartWorkoutSession(newWorkoutId))
                    }
                }
            }
            OverviewIntent.CreateFirstWorkout -> {
                viewModelScope.launch { _effect.emit(OverviewEffect.NavigateToCreateWorkout) }
            }
            OverviewIntent.AddNewExercise -> {
                _state.update { it.copy(exerciseDialog = ExerciseDialogState()) }
            }
            OverviewIntent.SeeAllWorkouts -> {
                viewModelScope.launch { _effect.emit(OverviewEffect.NavigateToTrain) }
            }
            OverviewIntent.SeeHistory -> {
                viewModelScope.launch { _effect.emit(OverviewEffect.NavigateToHistory) }
            }
            OverviewIntent.ShowAddExerciseDialog -> {
                _state.update { it.copy(exerciseDialog = ExerciseDialogState()) }
            }
            is OverviewIntent.UpdateExerciseDialogName -> {
                _state.update { it.copy(exerciseDialog = it.exerciseDialog?.copy(name = intent.name)) }
            }
            is OverviewIntent.UpdateExerciseDialogDescription -> {
                _state.update { it.copy(exerciseDialog = it.exerciseDialog?.copy(description = intent.description)) }
            }
            is OverviewIntent.ToggleExerciseDialogCategory -> {
                _state.update { state ->
                    val current = state.exerciseDialog?.selectedCategories ?: emptySet()
                    val newCategories = if (current.contains(intent.category)) {
                        current - intent.category
                    } else {
                        current + intent.category
                    }
                    state.copy(exerciseDialog = state.exerciseDialog?.copy(selectedCategories = newCategories))
                }
            }
            is OverviewIntent.UpdateExerciseDialogMuscleSearch -> {
                _state.update { it.copy(exerciseDialog = it.exerciseDialog?.copy(muscleSearchQuery = intent.query)) }
            }
            is OverviewIntent.ToggleExerciseDialogMuscle -> {
                _state.update { state ->
                    val current = state.exerciseDialog?.selectedMuscles ?: emptySet()
                    val newMuscles = if (current.contains(intent.muscle)) {
                        current - intent.muscle
                    } else {
                        current + intent.muscle
                    }
                    state.copy(exerciseDialog = state.exerciseDialog?.copy(selectedMuscles = newMuscles))
                }
            }
            OverviewIntent.SaveExercise -> {
                saveExercise()
            }
            OverviewIntent.DismissExerciseDialog -> {
                _state.update { it.copy(exerciseDialog = null) }
            }
        }
    }

    private fun saveExercise() {
        val dialogState = _state.value.exerciseDialog ?: return
        if (dialogState.name.isBlank() || dialogState.selectedCategories.isEmpty() || dialogState.selectedMuscles.isEmpty()) return
        
        viewModelScope.launch {
            val exercise = ExerciseEntity(
                id = dialogState.id ?: 0,
                name = dialogState.name,
                description = dialogState.description,
                category = dialogState.selectedCategories.joinToString(", "),
                muscles = dialogState.selectedMuscles.joinToString(", ")
            )
            dao.insertExercise(exercise)
            _state.update { it.copy(exerciseDialog = null) }
        }
    }
}
