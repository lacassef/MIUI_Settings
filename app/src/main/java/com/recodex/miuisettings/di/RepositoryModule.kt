package com.recodex.miuisettings.di

import com.recodex.miuisettings.data.local.SharedPreferencesCatalogVersionStore
import com.recodex.miuisettings.data.remote.JsonRemoteCatalogDataSource
import com.recodex.miuisettings.data.repository.SettingsRepositoryImpl
import com.recodex.miuisettings.domain.repository.CatalogVersionStore
import com.recodex.miuisettings.domain.repository.RemoteCatalogDataSource
import com.recodex.miuisettings.domain.repository.SettingsRepository
import com.recodex.miuisettings.infra.LogcatTelemetryLogger
import com.recodex.miuisettings.infra.TelemetryLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    abstract fun bindCatalogVersionStore(
        impl: SharedPreferencesCatalogVersionStore
    ): CatalogVersionStore

    @Binds
    abstract fun bindRemoteCatalogDataSource(
        impl: JsonRemoteCatalogDataSource
    ): RemoteCatalogDataSource

    @Binds
    abstract fun bindTelemetryLogger(
        impl: LogcatTelemetryLogger
    ): TelemetryLogger
}
