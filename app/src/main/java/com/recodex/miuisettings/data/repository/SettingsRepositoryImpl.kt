package com.recodex.miuisettings.data.repository

import com.recodex.miuisettings.data.local.SeedCatalogProvider
import com.recodex.miuisettings.data.local.SettingsDao
import com.recodex.miuisettings.data.mapper.toDomain
import com.recodex.miuisettings.data.mapper.toEntities
import com.recodex.miuisettings.BuildConfig
import com.recodex.miuisettings.domain.model.CatalogPayload
import com.recodex.miuisettings.domain.model.HiddenSetting
import com.recodex.miuisettings.domain.repository.CatalogVersionStore
import com.recodex.miuisettings.domain.repository.SettingsRepository
import com.recodex.miuisettings.domain.util.CatalogSignature
import com.recodex.miuisettings.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao,
    private val seedCatalogProvider: SeedCatalogProvider,
    private val catalogVersionStore: CatalogVersionStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SettingsRepository {
    override fun observeSettings(): Flow<List<HiddenSetting>> =
        settingsDao.observeSettings().map { list -> list.map { it.toDomain() } }

    override suspend fun getSettings(): List<HiddenSetting> = withContext(ioDispatcher) {
        settingsDao.getSettings().map { it.toDomain() }
    }

    override suspend fun getSettingById(settingId: String): HiddenSetting? = withContext(ioDispatcher) {
        settingsDao.getSetting(settingId)?.toDomain()
    }

    override suspend fun replaceCatalog(catalog: CatalogPayload) = withContext(ioDispatcher) {
        val (settings, targets) = catalog.toEntities()
        settingsDao.replaceCatalog(settings, targets)
    }

    override suspend fun ensureSeeded() = withContext(ioDispatcher) {
        if (settingsDao.countSettings() == 0) {
            val seed = seedCatalogProvider.provide()
            val (settings, targets) = seed.toEntities()
            settingsDao.replaceCatalog(settings, targets)
            catalogVersionStore.setVersion(seed.version)
            val signature = CatalogSignature.compute(seed, BuildConfig.REMOTE_CATALOG_SIGNATURE_SALT)
            catalogVersionStore.setSignature(signature)
        }
    }
}
