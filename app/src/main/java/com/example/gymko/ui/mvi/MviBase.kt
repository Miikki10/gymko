package com.example.gymko.ui.mvi

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface MviState

interface MviIntent

interface MviEffect

interface MviViewModel<S : MviState, I : MviIntent> {
    val state: StateFlow<S>
    val effect: SharedFlow<MviEffect>
    fun onIntent(intent: I)
}
