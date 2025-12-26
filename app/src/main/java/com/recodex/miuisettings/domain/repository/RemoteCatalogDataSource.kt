package com.recodex.miuisettings.domain.repository

import com.recodex.miuisettings.domain.model.CatalogPayload

interface RemoteCatalogDataSource {
    suspend fun fetchCatalog(): Result<CatalogPayload>
}
