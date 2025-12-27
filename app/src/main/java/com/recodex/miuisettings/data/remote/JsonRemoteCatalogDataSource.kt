package com.recodex.miuisettings.data.remote

import com.recodex.miuisettings.BuildConfig
import com.recodex.miuisettings.domain.model.CatalogPayload
import com.recodex.miuisettings.domain.model.HiddenSetting
import com.recodex.miuisettings.domain.model.SettingTarget
import com.recodex.miuisettings.domain.repository.RemoteCatalogDataSource
import com.recodex.miuisettings.domain.util.CatalogSignature
import com.recodex.miuisettings.domain.util.CatalogValidator
import com.recodex.miuisettings.domain.util.SettingCategories
import com.recodex.miuisettings.di.IoDispatcher
import com.recodex.miuisettings.infra.TelemetryLogger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class JsonRemoteCatalogDataSource @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val telemetryLogger: TelemetryLogger
) : RemoteCatalogDataSource {
    override suspend fun fetchCatalog(): Result<CatalogPayload> = withContext(ioDispatcher) {
        val url = BuildConfig.REMOTE_CATALOG_URL
        if (url.isBlank()) {
            return@withContext Result.failure(IllegalStateException("REMOTE_CATALOG_URL nao configurado"))
        }
        try {
            val json = fetchJson(url)
            val parsed = parseCatalog(json)
            validateCatalog(parsed)
            Result.success(parsed.payload)
        } catch (error: Exception) {
            telemetryLogger.logError("remote_catalog_fetch_failed", error)
            Result.failure(error)
        }
    }

    private fun fetchJson(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 7000
            connection.readTimeout = 7000
            connection.inputStream.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseCatalog(rawJson: String): ParsedCatalog {
        val root = JSONObject(rawJson)
        val version = root.optString("version", "").trim()
        val signature = root.optString("signature", "").trim().takeIf { it.isNotBlank() }
        val settingsArray = root.optJSONArray("settings") ?: JSONArray()
        val settings = mutableListOf<HiddenSetting>()
        for (index in 0 until settingsArray.length()) {
            val settingObj = settingsArray.getJSONObject(index)
            val settingId = settingObj.getString("id")
            val title = settingObj.optString("title", settingId)
            val category = settingObj.optString("category", SettingCategories.OTHER)
            val searchKeywords = parseKeywords(settingObj.opt("searchKeywords"))
            val targets = parseTargets(settingId, settingObj.optJSONArray("targets"))
            settings.add(
                HiddenSetting(
                    id = settingId,
                    title = title,
                    titleResId = settingObj.optInt("titleResId").takeIf { it != 0 },
                    iconResId = settingObj.optInt("iconResId").takeIf { it != 0 },
                    category = category,
                    minSdkVersion = settingObj.optInt("minSdkVersion").takeIf { it > 0 },
                    maxSdkVersion = settingObj.optInt("maxSdkVersion").takeIf { it > 0 },
                    requiredMiuiVersion = settingObj.optString("requiredMiuiVersion").takeIf { it.isNotBlank() },
                    isLegacyOnly = settingObj.optBoolean("isLegacyOnly", false),
                    searchKeywords = searchKeywords,
                    targets = targets
                )
            )
        }
        return ParsedCatalog(
            payload = CatalogPayload(version = version, settings = settings),
            signature = signature
        )
    }

    private fun parseKeywords(raw: Any?): List<String> {
        return when (raw) {
            is JSONArray -> (0 until raw.length()).mapNotNull { index ->
                raw.optString(index, "").takeIf { it.isNotBlank() }
            }
            is String -> raw.split(",").mapNotNull { it.trim().takeIf { value -> value.isNotBlank() } }
            else -> emptyList()
        }
    }

    private fun parseTargets(settingId: String, targetsArray: JSONArray?): List<SettingTarget> {
        if (targetsArray == null) return emptyList()
        val targets = mutableListOf<SettingTarget>()
        for (index in 0 until targetsArray.length()) {
            val targetObj = targetsArray.getJSONObject(index)
            val targetId = targetObj.optString("id").ifBlank { "${settingId}_$index" }
            val packageName = targetObj.optString("packageName", "")
            val className = targetObj.optString("className").takeIf { it.isNotBlank() }
            val action = targetObj.optString("action").takeIf { it.isNotBlank() }
            val extras = parseExtras(targetObj.opt("extras"))
            if (className == null && action == null) continue
            if (className != null && packageName.isBlank()) continue
            targets.add(
                SettingTarget(
                    id = targetId,
                    settingId = settingId,
                    packageName = packageName,
                    className = className,
                    action = action,
                    extras = extras,
                    priority = targetObj.optInt("priority", 0),
                    minSdkVersion = targetObj.optInt("minSdkVersion").takeIf { it > 0 },
                    maxSdkVersion = targetObj.optInt("maxSdkVersion").takeIf { it > 0 },
                    requiredMiuiVersion = targetObj.optString("requiredMiuiVersion").takeIf { it.isNotBlank() },
                    requiresExported = targetObj.optBoolean("requiresExported", true),
                    notes = targetObj.optString("notes").takeIf { it.isNotBlank() }
                )
            )
        }
        return targets
    }

    private fun parseExtras(raw: Any?): Map<String, String> {
        val obj = raw as? JSONObject ?: return emptyMap()
        val extras = mutableMapOf<String, String>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (key.isBlank()) continue
            val value = obj.opt(key)
            if (value != null) {
                extras[key] = value.toString()
            }
        }
        return extras
    }

    private fun validateCatalog(parsed: ParsedCatalog) {
        val validation = CatalogValidator.validate(parsed.payload)
        if (!validation.isValid) {
            val message = "Catalogo remoto invalido: ${validation.errors.joinToString()}"
            throw IllegalStateException(message)
        }
        validateSignature(parsed.signature, parsed.payload)
    }

    private fun validateSignature(signature: String?, payload: CatalogPayload) {
        val required = BuildConfig.REMOTE_CATALOG_SIGNATURE_REQUIRED
        if (signature.isNullOrBlank()) {
            if (required) {
                throw IllegalStateException("Catalogo remoto sem assinatura")
            }
            return
        }
        val computed = CatalogSignature.compute(payload, BuildConfig.REMOTE_CATALOG_SIGNATURE_SALT)
        if (!signature.equals(computed, ignoreCase = true)) {
            throw IllegalStateException("Assinatura do catalogo invalida")
        }
    }

    private data class ParsedCatalog(
        val payload: CatalogPayload,
        val signature: String?
    )
}
