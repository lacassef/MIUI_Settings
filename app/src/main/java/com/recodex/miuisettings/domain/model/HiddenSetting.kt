package com.recodex.miuisettings.domain.model

data class HiddenSetting(
    val id: String,
    val title: String,
    val titleResId: Int? = null,
    val iconResId: Int? = null,
    val category: String,
    val minSdkVersion: Int? = null,
    val maxSdkVersion: Int? = null,
    val requiredMiuiVersion: String? = null,
    val isLegacyOnly: Boolean = false,
    val searchKeywords: List<String> = emptyList(),
    val targets: List<SettingTarget> = emptyList()
)
