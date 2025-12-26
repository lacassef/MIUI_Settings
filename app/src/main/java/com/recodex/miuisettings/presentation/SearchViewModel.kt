package com.recodex.miuisettings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recodex.miuisettings.domain.usecase.SearchSettingsUseCase
import com.recodex.miuisettings.presentation.mapper.toSummary
import com.recodex.miuisettings.presentation.model.SearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchSettingsUseCase: SearchSettingsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    fun search(query: String, categoryFilter: String? = null) {
        _state.update {
            it.copy(
                query = query,
                categoryFilter = categoryFilter,
                isLoading = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            try {
                val results = searchSettingsUseCase(query, categoryFilter)
                _state.update {
                    it.copy(
                        isLoading = false,
                        results = results.map { setting -> setting.toSummary() }
                    )
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Falha na busca")
                }
            }
        }
    }
}
