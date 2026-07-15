package com.example.gymko.ui.mvi

import com.example.gymko.data.local.entity.UserEntity
import com.example.gymko.data.model.UnitSystem

data class SettingsState(
    val name: String = "",
    val height: String = "",
    val weight: String = "",
    val unitSystem: UnitSystem = UnitSystem.SI,
    val isLoading: Boolean = true,
    val isFirstLaunch: Boolean = false,
    val isSaved: Boolean = false
) : MviState

sealed class SettingsIntent : MviIntent {
    data class UpdateName(val name: String) : SettingsIntent()
    data class UpdateHeight(val height: String) : SettingsIntent()
    data class UpdateWeight(val weight: String) : SettingsIntent()
    object ToggleUnitSystem : SettingsIntent()
    object SaveProfile : SettingsIntent()
    object LoadProfile : SettingsIntent()
}

sealed class SettingsEvent {
    object ProfileSaved : SettingsEvent()
}
