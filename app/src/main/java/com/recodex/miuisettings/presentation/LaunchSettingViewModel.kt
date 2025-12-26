package com.recodex.miuisettings.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recodex.miuisettings.domain.usecase.LaunchSettingUseCase
import com.recodex.miuisettings.presentation.model.LaunchState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LaunchSettingViewModel @Inject constructor(
    private val launchSettingUseCase: LaunchSettingUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(LaunchState())
    val state: StateFlow<LaunchState> = _state.asStateFlow()

    fun launch(settingId: String) {
        _state.update { it.copy(isLaunching = true, lastResult = null) }
        viewModelScope.launch {
            val result = launchSettingUseCase(context, settingId)
            _state.update { it.copy(isLaunching = false, lastResult = result) }
        }
    }
}
