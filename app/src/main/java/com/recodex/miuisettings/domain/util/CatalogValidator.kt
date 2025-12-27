package com.recodex.miuisettings.domain.util

import com.recodex.miuisettings.domain.model.CatalogPayload

data class CatalogValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

object CatalogValidator {
    fun validate(payload: CatalogPayload): CatalogValidationResult {
        val errors = mutableListOf<String>()
        if (payload.version.isBlank()) {
            errors.add("version_missing")
        }
        if (payload.settings.isEmpty()) {
            errors.add("settings_empty")
        }

        val settingIds = mutableSetOf<String>()
        payload.settings.forEachIndexed { index, setting ->
            if (setting.id.isBlank()) {
                errors.add("settings[$index].id_blank")
            } else if (!settingIds.add(setting.id)) {
                errors.add("settings[$index].id_duplicate:${setting.id}")
            }
            if (setting.title.isBlank()) {
                errors.add("settings[$index].title_blank")
            }
            if (setting.category.isBlank()) {
                errors.add("settings[$index].category_blank")
            }
            if (setting.minSdkVersion != null && setting.maxSdkVersion != null
                && setting.minSdkVersion > setting.maxSdkVersion
            ) {
                errors.add("settings[$index].sdk_range_invalid")
            }
            if (setting.targets.isEmpty()) {
                errors.add("settings[$index].targets_empty")
            }

            val targetIds = mutableSetOf<String>()
            setting.targets.forEachIndexed { targetIndex, target ->
                if (target.id.isBlank()) {
                    errors.add("settings[$index].targets[$targetIndex].id_blank")
                } else if (!targetIds.add(target.id)) {
                    errors.add("settings[$index].targets[$targetIndex].id_duplicate:${target.id}")
                }
                if (target.className != null && target.packageName.isBlank()) {
                    errors.add("settings[$index].targets[$targetIndex].package_missing")
                }
                if (target.action == null && target.className == null) {
                    errors.add("settings[$index].targets[$targetIndex].intent_missing")
                }
                if (target.extras.keys.any { it.isBlank() }) {
                    errors.add("settings[$index].targets[$targetIndex].extras_key_blank")
                }
                if (target.minSdkVersion != null && target.maxSdkVersion != null
                    && target.minSdkVersion > target.maxSdkVersion
                ) {
                    errors.add("settings[$index].targets[$targetIndex].sdk_range_invalid")
                }
            }
        }
        return CatalogValidationResult(errors.isEmpty(), errors)
    }
}
