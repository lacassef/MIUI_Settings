package com.recodex.miuisettings.domain.model

data class SettingTarget(
    val id: String,
    val settingId: String,
    val packageName: String,
    val className: String? = null,
    val action: String? = null,
    val priority: Int = 0,
    val minSdkVersion: Int? = null,
    val maxSdkVersion: Int? = null,
    val requiredMiuiVersion: String? = null,
    val requiresExported: Boolean = true,
    val notes: String? = null
)
