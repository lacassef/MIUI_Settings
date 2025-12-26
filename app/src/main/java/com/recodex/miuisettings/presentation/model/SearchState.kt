package com.recodex.miuisettings.presentation.model

data class SearchState(
    val query: String = "",
    val categoryFilter: String? = null,
    val isLoading: Boolean = false,
    val results: List<SettingSummary> = emptyList(),
    val errorMessage: String? = null
)
