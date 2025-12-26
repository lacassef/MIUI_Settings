package com.recodex.miuisettings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recodex.miuisettings.domain.usecase.GetCompatibilityReportUseCase
import com.recodex.miuisettings.domain.usecase.GetAvailableSettingsUseCase
import com.recodex.miuisettings.domain.usecase.GetDeviceProfileUseCase
import com.recodex.miuisettings.domain.usecase.SyncCatalogUseCase
import com.recodex.miuisettings.presentation.mapper.toSummary
import com.recodex.miuisettings.presentation.model.SettingsListState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsListViewModel @Inject constructor(
    private val getAvailableSettingsUseCase: GetAvailableSettingsUseCase,
    private val syncCatalogUseCase: SyncCatalogUseCase,
    private val getDeviceProfileUseCase: GetDeviceProfileUseCase,
    private val getCompatibilityReportUseCase: GetCompatibilityReportUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsListState())
    val state: StateFlow<SettingsListState> = _state.asStateFlow()

    fun load(categoryFilter: String? = null) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    categoryFilter = categoryFilter,
                    errorMessage = null
                )
            }
            try {
                val profile = getDeviceProfileUseCase()
                val settings = getAvailableSettingsUseCase(categoryFilter)
                val compatibilityReport = getCompatibilityReportUseCase()
                _state.update {
                    it.copy(
                        isLoading = false,
                        settings = settings.map { setting -> setting.toSummary() },
                        deviceProfile = profile,
                        compatibilityReport = compatibilityReport
                    )
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Falha ao carregar")
                }
            }
        }
    }

    fun syncCatalog(categoryFilter: String? = null) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    categoryFilter = categoryFilter,
                    errorMessage = null
                )
            }
            try {
                val syncResult = syncCatalogUseCase()
                val profile = getDeviceProfileUseCase()
                val settings = getAvailableSettingsUseCase(categoryFilter)
                val compatibilityReport = getCompatibilityReportUseCase()
                _state.update {
                    it.copy(
                        isLoading = false,
                        settings = settings.map { setting -> setting.toSummary() },
                        deviceProfile = profile,
                        compatibilityReport = compatibilityReport,
                        lastSync = syncResult
                    )
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Falha ao sincronizar")
                }
            }
        }
    }
}
