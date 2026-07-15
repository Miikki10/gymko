package com.example.gymko.ui.mvi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymko.data.local.database.GymKoDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application), MviViewModel<HistoryState, HistoryIntent> {
    private val dao = GymKoDatabase.getDatabase(application).gymKoDao()

    private val _state = MutableStateFlow(HistoryState(isLoading = true))
    override val state: StateFlow<HistoryState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HistoryEffect>()
    override val effect: SharedFlow<HistoryEffect> = _effect.asSharedFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        dao.getRecentCompletedWorkouts(10).onEach { workouts ->
            _state.update { it.copy(workouts = workouts, isLoading = false) }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: HistoryIntent) {
        // No specific intents for now
    }
}
