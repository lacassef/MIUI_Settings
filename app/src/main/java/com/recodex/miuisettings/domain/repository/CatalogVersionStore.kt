package com.recodex.miuisettings.domain.repository

interface CatalogVersionStore {
    suspend fun getVersion(): String?
    suspend fun setVersion(version: String)
    suspend fun getSignature(): String? = null
    suspend fun setSignature(signature: String) {}
}
