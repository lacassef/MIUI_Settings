package com.recodex.miuisettings.presentation.model

import com.recodex.miuisettings.domain.model.CompatibilityReport
import com.recodex.miuisettings.domain.model.DeviceProfile
import com.recodex.miuisettings.domain.model.SyncResult

data class SettingsListState(
    val isLoading: Boolean = false,
    val categoryFilter: String? = null,
    val settings: List<SettingSummary> = emptyList(),
    val deviceProfile: DeviceProfile? = null,
    val compatibilityReport: CompatibilityReport? = null,
    val lastSync: SyncResult? = null,
    val errorMessage: String? = null
)
