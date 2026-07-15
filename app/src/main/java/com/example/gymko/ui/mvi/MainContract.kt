package com.example.gymko.ui.mvi

import com.example.gymko.ui.navigation.Screen

data class MainState(
    val currentScreen: Screen = Screen.Overview
) : MviState

sealed interface MainIntent : MviIntent {
    data class NavigateTo(val screen: Screen) : MainIntent
}
