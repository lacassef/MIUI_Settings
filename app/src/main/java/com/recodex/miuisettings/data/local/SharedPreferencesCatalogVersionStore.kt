package com.recodex.miuisettings.data.local

import android.content.Context
import com.recodex.miuisettings.domain.repository.CatalogVersionStore
import com.recodex.miuisettings.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesCatalogVersionStore @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CatalogVersionStore {
    override suspend fun getVersion(): String? = withContext(ioDispatcher) {
        prefs.getString(KEY_VERSION, null)
    }

    override suspend fun setVersion(version: String) = withContext(ioDispatcher) {
        prefs.edit().putString(KEY_VERSION, version).apply()
    }

    override suspend fun getSignature(): String? = withContext(ioDispatcher) {
        prefs.getString(KEY_SIGNATURE, null)
    }

    override suspend fun setSignature(signature: String) = withContext(ioDispatcher) {
        prefs.edit().putString(KEY_SIGNATURE, signature).apply()
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private companion object {
        private const val PREFS_NAME = "eclipse_catalog"
        private const val KEY_VERSION = "catalog_version"
        private const val KEY_SIGNATURE = "catalog_signature"
    }
}
