package com.recodex.miuisettings.domain.util

import com.recodex.miuisettings.domain.model.CatalogPayload
import com.recodex.miuisettings.domain.model.SettingTarget
import java.security.MessageDigest

object CatalogSignature {
    fun compute(payload: CatalogPayload, salt: String? = null): String {
        val base = buildCanonical(payload)
        val input = if (salt.isNullOrBlank()) base else "$base|$salt"
        return sha256(input)
    }

    private fun buildCanonical(payload: CatalogPayload): String {
        val builder = StringBuilder()
        builder.append("version=").append(payload.version)
        payload.settings.sortedBy { it.id }.forEach { setting ->
            builder.append("|setting=").append(setting.id)
            builder.append(";title=").append(setting.title)
            builder.append(";titleResId=").append(setting.titleResId ?: 0)
            builder.append(";iconResId=").append(setting.iconResId ?: 0)
            builder.append(";category=").append(setting.category)
            builder.append(";minSdk=").append(setting.minSdkVersion ?: 0)
            builder.append(";maxSdk=").append(setting.maxSdkVersion ?: 0)
            builder.append(";requiredMiui=").append(setting.requiredMiuiVersion ?: "")
            builder.append(";legacy=").append(setting.isLegacyOnly)
            val keywords = setting.searchKeywords.map { it.trim() }
                .filter { it.isNotBlank() }
                .sorted()
                .joinToString(",")
            builder.append(";keywords=").append(keywords)
            setting.targets.sortedWith(compareBy<SettingTarget> { it.id }.thenByDescending { it.priority })
                .forEach { target ->
                    builder.append("|target=").append(target.id)
                    builder.append(";package=").append(target.packageName)
                    builder.append(";class=").append(target.className ?: "")
                    builder.append(";action=").append(target.action ?: "")
                    builder.append(";priority=").append(target.priority)
                    builder.append(";minSdk=").append(target.minSdkVersion ?: 0)
                    builder.append(";maxSdk=").append(target.maxSdkVersion ?: 0)
                    builder.append(";requiredMiui=").append(target.requiredMiuiVersion ?: "")
                    builder.append(";exported=").append(target.requiresExported)
                    builder.append(";notes=").append(target.notes ?: "")
                    if (target.extras.isNotEmpty()) {
                        val extras = target.extras.toSortedMap().entries.joinToString(",") { entry ->
                            "${escape(entry.key)}=${escape(entry.value)}"
                        }
                        builder.append(";extras=").append(extras)
                    }
                }
        }
        return builder.toString()
    }

    private fun escape(value: String): String {
        return value.replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace("=", "\\=")
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashed = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashed.joinToString("") { "%02x".format(it) }
    }
}
