package com.example.gymko.ui.mvi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymko.data.local.database.GymKoDatabase
import com.example.gymko.data.local.entity.UserEntity
import com.example.gymko.data.model.UnitSystem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application), MviViewModel<SettingsState, SettingsIntent> {

    private val dao = GymKoDatabase.getDatabase(application).gymKoDao()

    private val _state = MutableStateFlow(SettingsState())
    override val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MviEffect>()
    override val effect: SharedFlow<MviEffect> = _effect.asSharedFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    init {
        onIntent(SettingsIntent.LoadProfile)
    }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.UpdateName -> {
                _state.update { it.copy(name = intent.name) }
            }
            is SettingsIntent.UpdateHeight -> {
                _state.update { it.copy(height = intent.height) }
            }
            is SettingsIntent.UpdateWeight -> {
                _state.update { it.copy(weight = intent.weight) }
            }
            SettingsIntent.ToggleUnitSystem -> {
                _state.update {
                    it.copy(unitSystem = if (it.unitSystem == UnitSystem.SI) UnitSystem.IMPERIAL else UnitSystem.SI)
                }
            }
            SettingsIntent.SaveProfile -> {
                saveProfile()
            }
            SettingsIntent.LoadProfile -> {
                loadProfile()
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            dao.getUser().firstOrNull()?.let { user ->
                _state.update {
                    it.copy(
                        name = user.name,
                        height = user.height.toString(),
                        weight = user.weight.toString(),
                        unitSystem = user.unitSystem,
                        isFirstLaunch = false,
                        isLoading = false
                    )
                }
            } ?: run {
                _state.update { it.copy(isFirstLaunch = true, isLoading = false) }
            }
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            val currentState = _state.value
            val user = UserEntity(
                name = currentState.name,
                height = currentState.height.toDoubleOrNull() ?: 0.0,
                weight = currentState.weight.toDoubleOrNull() ?: 0.0,
                unitSystem = currentState.unitSystem
            )
            dao.upsertUser(user)
            _state.update { it.copy(isSaved = true, isFirstLaunch = false) }
            _events.emit(SettingsEvent.ProfileSaved)
        }
    }
}
