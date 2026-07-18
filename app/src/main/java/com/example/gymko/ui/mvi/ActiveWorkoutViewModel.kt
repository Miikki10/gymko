package com.example.gymko.ui.mvi

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymko.data.local.database.GymKoDatabase
import com.example.gymko.data.model.WorkoutStatus
import com.example.gymko.service.WorkoutService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ActiveWorkoutViewModel(application: Application) : AndroidViewModel(application), MviViewModel<ActiveWorkoutState, ActiveWorkoutIntent> {

    private val dao = GymKoDatabase.getDatabase(application).gymKoDao()

    private val _state = MutableStateFlow(ActiveWorkoutState())
    override val state: StateFlow<ActiveWorkoutState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ActiveWorkoutEffect>()
    override val effect: SharedFlow<ActiveWorkoutEffect> = _effect.asSharedFlow()

    private var workoutService: WorkoutService? = null
    private var timerJob: Job? = null
    private var dbJob: Job? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as WorkoutService.WorkoutBinder
            workoutService = binder.getService()
            if (_state.value.workout != null) {
                observeTimer()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            workoutService = null
            timerJob?.cancel()
        }
    }

    init {
        application.bindService(
            Intent(application, WorkoutService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        observeDbForActiveWorkout()
    }

    private fun observeDbForActiveWorkout() {
        dbJob?.cancel()
        dbJob = dao.getAllWorkoutsWithSets()
            .onEach { workouts ->
                val active = workouts.find { it.workout.status == WorkoutStatus.ACTIVE }
                if (active != null) {
                    val isNew = _state.value.workout?.workout?.id != active.workout.id
                    val completedIds = active.sets.filter { it.set.isCompleted }.map { it.set.id }.toSet()
                    
                    _state.update { it.copy(
                        workout = active,
                        completedSets = completedIds
                    ) }
                    
                    if (isNew) {
                        startWorkoutService()
                    }
                    
                    if (workoutService != null) {
                        observeTimer()
                    }
                } else {
                    if (_state.value.workout != null) {
                        _state.update { it.copy(workout = null, durationSeconds = 0, completedSets = emptySet()) }
                        stopWorkoutService()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun startWorkoutService() {
        val hasHealthPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        } else true

        if (hasHealthPermission) {
            val intent = Intent(getApplication(), WorkoutService::class.java).apply {
                action = "START"
            }
            getApplication<Application>().startForegroundService(intent)
        }
    }

    private fun stopWorkoutService() {
        val intent = Intent(getApplication(), WorkoutService::class.java).apply {
            action = "STOP"
        }
        getApplication<Application>().startService(intent)
        timerJob?.cancel()
    }

    private fun observeTimer() {
        if (timerJob?.isActive == true) return
        timerJob = workoutService?.duration?.onEach { duration ->
            _state.update { it.copy(durationSeconds = duration) }
        }?.launchIn(viewModelScope)
    }

    override fun onIntent(intent: ActiveWorkoutIntent) {
        when (intent) {
            is ActiveWorkoutIntent.LoadWorkout -> {
                // If it's already loaded by observeDb, we don't need to do much
                // But let's ensure the service is running for this ID
                if (_state.value.workout?.workout?.id == intent.workoutId) {
                    startWorkoutService()
                }
            }
            is ActiveWorkoutIntent.ToggleSet -> toggleSet(intent.setId)
            ActiveWorkoutIntent.ToggleAllSets -> toggleAllSets()
            is ActiveWorkoutIntent.ToggleExerciseSets -> toggleExerciseSets(intent.exerciseId)
            ActiveWorkoutIntent.EndWorkout -> endWorkout()
            ActiveWorkoutIntent.HideWorkout -> {
                _state.update { it.copy(isHidden = true) }
                viewModelScope.launch { _effect.emit(ActiveWorkoutEffect.NavigateToOverview) }
            }
            ActiveWorkoutIntent.ShowWorkout -> {
                _state.update { it.copy(isHidden = false) }
                observeTimer()
            }
        }
    }

    private fun toggleSet(setId: Long) {
        viewModelScope.launch {
            val workout = _state.value.workout ?: return@launch
            val setWithExercise = workout.sets.find { it.set.id == setId } ?: return@launch
            dao.updateSet(setWithExercise.set.copy(isCompleted = !setWithExercise.set.isCompleted))
        }
    }

    private fun toggleAllSets() {
        viewModelScope.launch {
            val workout = _state.value.workout ?: return@launch
            val allSets = workout.sets
            val allChecked = allSets.all { it.set.isCompleted }
            dao.updateSets(allSets.map { it.set.copy(isCompleted = !allChecked) })
        }
    }

    private fun toggleExerciseSets(exerciseId: Long) {
        viewModelScope.launch {
            val workout = _state.value.workout ?: return@launch
            val exerciseSets = workout.sets.filter { it.set.exerciseId == exerciseId }
            val allChecked = exerciseSets.all { it.set.isCompleted }
            dao.updateSets(exerciseSets.map { it.set.copy(isCompleted = !allChecked) })
        }
    }

    private fun endWorkout() {
        val workout = _state.value.workout?.workout ?: return
        viewModelScope.launch {
            dao.updateWorkout(workout.copy(status = WorkoutStatus.COMPLETED, timestamp = System.currentTimeMillis()))
            // observeDbForActiveWorkout will handle service stop and state clearing
            _effect.emit(ActiveWorkoutEffect.NavigateToOverview)
        }
    }

    override fun onCleared() {
        super.onCleared()
        dbJob?.cancel()
        timerJob?.cancel()
        getApplication<Application>().unbindService(serviceConnection)
    }
}
