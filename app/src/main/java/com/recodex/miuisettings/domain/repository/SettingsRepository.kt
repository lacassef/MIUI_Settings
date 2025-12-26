package com.recodex.miuisettings.domain.repository

import com.recodex.miuisettings.domain.model.CatalogPayload
import com.recodex.miuisettings.domain.model.HiddenSetting
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<List<HiddenSetting>>
    suspend fun getSettings(): List<HiddenSetting>
    suspend fun getSettingById(settingId: String): HiddenSetting?
    suspend fun replaceCatalog(catalog: CatalogPayload)
    suspend fun ensureSeeded()
}
