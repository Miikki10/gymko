package com.example.gymko.ui.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel(), MviViewModel<MainState, MainIntent> {

    private val _state = MutableStateFlow(MainState())
    override val state: StateFlow<MainState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MviEffect>()
    override val effect: SharedFlow<MviEffect> = _effect.asSharedFlow()

    override fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.NavigateTo -> {
                _state.update { it.copy(currentScreen = intent.screen) }
            }
        }
    }
}
