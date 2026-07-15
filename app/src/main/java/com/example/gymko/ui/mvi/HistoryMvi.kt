package com.example.gymko.ui.mvi

import com.example.gymko.data.local.relation.WorkoutWithSets

data class HistoryState(
    val workouts: List<WorkoutWithSets> = emptyList(),
    val isLoading: Boolean = false
) : MviState

sealed class HistoryIntent : MviIntent {
    object LoadHistory : HistoryIntent()
}

sealed class HistoryEffect : MviEffect
