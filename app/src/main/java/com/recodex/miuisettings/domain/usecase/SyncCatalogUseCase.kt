package com.recodex.miuisettings.domain.usecase

import com.recodex.miuisettings.domain.model.SyncResult
import com.recodex.miuisettings.domain.repository.CatalogVersionStore
import com.recodex.miuisettings.domain.repository.RemoteCatalogDataSource
import com.recodex.miuisettings.domain.repository.SettingsRepository
import com.recodex.miuisettings.domain.util.CatalogSignature
import com.recodex.miuisettings.domain.util.VersionComparator
import com.recodex.miuisettings.infra.TelemetryLogger
import com.recodex.miuisettings.BuildConfig
import javax.inject.Inject

class SyncCatalogUseCase @Inject constructor(
    private val remoteCatalogDataSource: RemoteCatalogDataSource,
    private val settingsRepository: SettingsRepository,
    private val catalogVersionStore: CatalogVersionStore,
    private val telemetryLogger: TelemetryLogger
) {
    suspend operator fun invoke(): SyncResult {
        settingsRepository.ensureSeeded()
        val remoteResult = remoteCatalogDataSource.fetchCatalog()
        if (remoteResult.isFailure) {
            val error = remoteResult.exceptionOrNull()
            telemetryLogger.logError(
                "sync_catalog_failed_fetch",
                error ?: IllegalStateException("remote_fetch_failed")
            )
            return SyncResult.Failed(error?.message ?: "Falha ao buscar catalogo remoto")
        }

        val catalog = remoteResult.getOrNull()
            ?: return SyncResult.Failed("Catalogo remoto vazio")
        if (catalog.version.isBlank()) {
            return SyncResult.Failed("Catalogo remoto sem versao")
        }
        val currentVersion = catalogVersionStore.getVersion()
        val currentSignature = catalogVersionStore.getSignature()
        val newSignature = CatalogSignature.compute(catalog, BuildConfig.REMOTE_CATALOG_SIGNATURE_SALT)
        if (currentVersion != null) {
            val compare = VersionComparator.compare(catalog.version, currentVersion)
            if (compare < 0) {
                return SyncResult.Noop(currentVersion)
            }
            if (compare == 0 && currentSignature != null && currentSignature == newSignature) {
                return SyncResult.Noop(currentVersion)
            }
        }
        settingsRepository.replaceCatalog(catalog)
        catalogVersionStore.setVersion(catalog.version)
        catalogVersionStore.setSignature(newSignature)
        telemetryLogger.logEvent("sync_catalog_updated", mapOf("version" to catalog.version))
        return SyncResult.Updated(catalog.version)
    }
}
