package com.example.gymko.ui.mvi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymko.data.local.database.GymKoDatabase
import com.example.gymko.data.local.entity.ExerciseEntity
import com.example.gymko.data.local.entity.WorkoutEntity
import com.example.gymko.data.model.WorkoutStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TrainViewModel(application: Application) : AndroidViewModel(application), MviViewModel<TrainState, TrainIntent> {

    private val dao = GymKoDatabase.getDatabase(application).gymKoDao()

    private val _state = MutableStateFlow(TrainState())
    override val state: StateFlow<TrainState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<TrainEffect>()
    override val effect: SharedFlow<TrainEffect> = _effect.asSharedFlow()

    init {
        observeExercises()
        observeWorkouts()
    }

    private fun observeExercises() {
        dao.getAllExercises().combine(_state.map { it.searchQuery }.distinctUntilChanged()) { exercises, query ->
            if (query.isBlank()) exercises
            else exercises.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.muscles.contains(query, ignoreCase = true) 
            }
        }.onEach { filtered ->
            _state.update { it.copy(exercises = filtered) }
        }.launchIn(viewModelScope)
    }

    private fun observeWorkouts() {
        dao.getAllWorkoutsWithSets().combine(_state.map { it.searchQuery }.distinctUntilChanged()) { workouts, query ->
            val filteredByStatus = workouts.filter { it.workout.status != WorkoutStatus.COMPLETED }
            if (query.isBlank()) filteredByStatus
            else filteredByStatus.filter {
                it.workout.name.contains(query, ignoreCase = true) 
            }
        }.onEach { filtered ->
            _state.update { it.copy(workouts = filtered) }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: TrainIntent) {
        when (intent) {
            is TrainIntent.UpdateSearchQuery -> {
                _state.update { it.copy(searchQuery = intent.query) }
            }
            is TrainIntent.SelectTab -> {
                _state.update { it.copy(selectedTab = intent.tab) }
            }
            TrainIntent.ShowAddExerciseDialog -> {
                _state.update { it.copy(exerciseDialog = ExerciseDialogState()) }
            }
            is TrainIntent.ShowEditExerciseDialog -> {
                _state.update {
                    it.copy(
                        exerciseDialog = ExerciseDialogState(
                            id = intent.exercise.id,
                            name = intent.exercise.name,
                            description = intent.exercise.description,
                            selectedCategories = intent.exercise.category.split(", ").filter { it.isNotBlank() }.toSet(),
                            selectedMuscles = intent.exercise.muscles.split(", ").filter { it.isNotBlank() }.toSet()
                        )
                    )
                }
            }
            is TrainIntent.UpdateExerciseDialogName -> {
                _state.update { it.copy(exerciseDialog = it.exerciseDialog?.copy(name = intent.name)) }
            }
            is TrainIntent.UpdateExerciseDialogDescription -> {
                _state.update { it.copy(exerciseDialog = it.exerciseDialog?.copy(description = intent.description)) }
            }
            is TrainIntent.ToggleExerciseDialogCategory -> {
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
            is TrainIntent.UpdateExerciseDialogMuscleSearch -> {
                _state.update { it.copy(exerciseDialog = it.exerciseDialog?.copy(muscleSearchQuery = intent.query)) }
            }
            is TrainIntent.ToggleExerciseDialogMuscle -> {
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
            TrainIntent.SaveExercise -> {
                saveExercise()
            }
            TrainIntent.DismissExerciseDialog -> {
                _state.update { it.copy(exerciseDialog = null) }
            }
            is TrainIntent.ShowEditWorkoutScreen -> {
                val workoutWithSets = intent.workoutWithSets
                val exercises = workoutWithSets.sets.groupBy { it.set.exerciseId }.map { entry ->
                    val setWithExerciseList = entry.value
                    val exercise = setWithExerciseList.first().exercise
                    WorkoutExerciseState(
                        exercise = exercise,
                        sets = setWithExerciseList.map { it.set }.sortedBy { it.order }.map { s ->
                            WorkoutSetState(weight = s.weight.toString(), reps = s.reps.toString()) 
                        }
                    )
                }
                _state.update {
                    it.copy(
                        editingWorkoutId = workoutWithSets.workout.id,
                        createWorkoutName = workoutWithSets.workout.name,
                        createWorkoutExercises = exercises
                    )
                }
                viewModelScope.launch { _effect.emit(TrainEffect.NavigateToCreateWorkout) }
            }
            is TrainIntent.StartWorkout -> {
                startWorkout(intent.workoutId)
            }
            is TrainIntent.ShowDeleteExerciseConfirmation -> {
                _state.update { it.copy(deleteExerciseConfirmation = intent.exercise) }
            }
            is TrainIntent.ShowDeleteWorkoutConfirmation -> {
                _state.update { it.copy(deleteWorkoutConfirmation = intent.workout) }
            }
            TrainIntent.DeleteExercise -> {
                deleteExercise()
            }
            TrainIntent.DeleteWorkout -> {
                deleteWorkout()
            }
            TrainIntent.DismissDeleteConfirmation -> {
                _state.update { it.copy(deleteExerciseConfirmation = null, deleteWorkoutConfirmation = null) }
            }
            TrainIntent.ShowCreateWorkoutScreen -> {
                _state.update { it.copy(editingWorkoutId = null, createWorkoutName = "", createWorkoutExercises = emptyList()) }
                viewModelScope.launch { _effect.emit(TrainEffect.NavigateToCreateWorkout) }
            }
            is TrainIntent.UpdateCreateWorkoutName -> {
                _state.update { it.copy(createWorkoutName = intent.name) }
            }
            TrainIntent.ShowAddExerciseToWorkoutDialog -> {
                _state.update { it.copy(showAddExerciseToWorkoutDialog = true, exerciseSearchQuery = "") }
            }
            TrainIntent.DismissAddExerciseToWorkoutDialog -> {
                _state.update { it.copy(showAddExerciseToWorkoutDialog = false) }
            }
            is TrainIntent.UpdateExerciseSearchQuery -> {
                _state.update { it.copy(exerciseSearchQuery = intent.query) }
            }
            is TrainIntent.AddExerciseToWorkout -> {
                _state.update { 
                    it.copy(
                        createWorkoutExercises = it.createWorkoutExercises + WorkoutExerciseState(intent.exercise),
                        showAddExerciseToWorkoutDialog = false
                    )
                }
            }
            is TrainIntent.RemoveExerciseFromWorkout -> {
                _state.update { 
                    val newList = it.createWorkoutExercises.toMutableList()
                    newList.removeAt(intent.index)
                    it.copy(createWorkoutExercises = newList)
                }
            }
            is TrainIntent.AddSetToExercise -> {
                _state.update { 
                    val newList = it.createWorkoutExercises.toMutableList()
                    val oldEx = newList[intent.exerciseIndex]
                    newList[intent.exerciseIndex] = oldEx.copy(sets = oldEx.sets + WorkoutSetState())
                    it.copy(createWorkoutExercises = newList)
                }
            }
            is TrainIntent.RemoveSetFromExercise -> {
                _state.update { 
                    val newList = it.createWorkoutExercises.toMutableList()
                    val oldEx = newList[intent.exerciseIndex]
                    val newSets = oldEx.sets.toMutableList()
                    newSets.removeAt(intent.setIndex)
                    newList[intent.exerciseIndex] = oldEx.copy(sets = newSets)
                    it.copy(createWorkoutExercises = newList)
                }
            }
            is TrainIntent.UpdateSetWeight -> {
                _state.update { 
                    val newList = it.createWorkoutExercises.toMutableList()
                    val oldEx = newList[intent.exerciseIndex]
                    val newSets = oldEx.sets.toMutableList()
                    newSets[intent.setIndex] = newSets[intent.setIndex].copy(weight = intent.weight)
                    newList[intent.exerciseIndex] = oldEx.copy(sets = newSets)
                    it.copy(createWorkoutExercises = newList)
                }
            }
            is TrainIntent.UpdateSetReps -> {
                _state.update { 
                    val newList = it.createWorkoutExercises.toMutableList()
                    val oldEx = newList[intent.exerciseIndex]
                    val newSets = oldEx.sets.toMutableList()
                    newSets[intent.setIndex] = newSets[intent.setIndex].copy(reps = intent.reps)
                    newList[intent.exerciseIndex] = oldEx.copy(sets = newSets)
                    it.copy(createWorkoutExercises = newList)
                }
            }
            TrainIntent.SaveNewWorkout -> {
                saveNewWorkout()
            }
            TrainIntent.CancelCreateWorkout -> {
                _state.update { it.copy(editingWorkoutId = null, createWorkoutName = "", createWorkoutExercises = emptyList()) }
                viewModelScope.launch { _effect.emit(TrainEffect.NavigateBack) }
            }
        }
    }

    private fun saveNewWorkout() {
        val name = _state.value.createWorkoutName
        if (name.isBlank()) return
        
        viewModelScope.launch {
            val existingTemplate = dao.getTemplateByName(name)
            val editingId = _state.value.editingWorkoutId

            if (existingTemplate != null && existingTemplate.id != editingId) {
                // Another template with the same name already exists. 
                _state.update { it.copy(editingWorkoutId = null, createWorkoutName = "", createWorkoutExercises = emptyList()) }
                _effect.emit(TrainEffect.NavigateBack)
                return@launch
            }

            // Create or update the workout, ensuring status is INACTIVE so it shows in Train tab
            val workoutId = if (existingTemplate != null) {
                dao.insertWorkout(existingTemplate.copy(name = name, status = WorkoutStatus.INACTIVE))
            } else if (editingId != null) {
                val editingWorkout = dao.getWorkoutWithSetsById(editingId)?.workout
                dao.insertWorkout(
                    editingWorkout?.copy(name = name, status = WorkoutStatus.INACTIVE) 
                        ?: WorkoutEntity(id = editingId, name = name, status = WorkoutStatus.INACTIVE)
                )
            } else {
                dao.insertWorkout(WorkoutEntity(name = name, status = WorkoutStatus.INACTIVE))
            }

            // Replace sets for the workout
            dao.deleteSetsByWorkoutId(workoutId)
            _state.value.createWorkoutExercises.forEachIndexed { exIndex, exState ->
                exState.sets.forEachIndexed { setIndex, setState ->
                    dao.insertSet(
                        com.example.gymko.data.local.entity.SetEntity(
                            workoutId = workoutId,
                            exerciseId = exState.exercise.id,
                            weight = setState.weight.toDoubleOrNull() ?: 0.0,
                            reps = setState.reps.toIntOrNull() ?: 0,
                            order = setIndex
                        )
                    )
                }
            }
            
            _state.update { it.copy(editingWorkoutId = null, createWorkoutName = "", createWorkoutExercises = emptyList()) }
            _effect.emit(TrainEffect.NavigateBack)
        }
    }

    private fun deleteExercise() {
        val exercise = _state.value.deleteExerciseConfirmation ?: return
        viewModelScope.launch {
            dao.deleteExercise(exercise)
            _state.update { it.copy(deleteExerciseConfirmation = null) }
        }
    }

    private fun deleteWorkout() {
        val workout = _state.value.deleteWorkoutConfirmation ?: return
        viewModelScope.launch {
            dao.deleteWorkout(workout)
            _state.update { it.copy(deleteWorkoutConfirmation = null) }
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

    private fun saveWorkout() {
        val dialogState = _state.value.workoutDialog ?: return
        viewModelScope.launch {
            val workout = WorkoutEntity(
                id = dialogState.id ?: 0,
                name = dialogState.name
            )
            dao.insertWorkout(workout)
            _state.update { it.copy(workoutDialog = null) }
        }
    }

    private fun startWorkout(workoutId: Long) {
        viewModelScope.launch {
            val newWorkoutId = dao.startWorkoutSession(workoutId)
            if (newWorkoutId != -1L) {
                _effect.emit(TrainEffect.StartWorkoutSession(newWorkoutId))
            }
        }
    }
}
