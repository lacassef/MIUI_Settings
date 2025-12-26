package com.recodex.miuisettings.data.mapper

import com.recodex.miuisettings.data.local.HiddenSettingEntity
import com.recodex.miuisettings.data.local.HiddenSettingWithTargets
import com.recodex.miuisettings.data.local.SettingTargetEntity
import com.recodex.miuisettings.domain.model.CatalogPayload
import com.recodex.miuisettings.domain.model.HiddenSetting
import com.recodex.miuisettings.domain.model.SettingTarget
import com.recodex.miuisettings.domain.util.TextNormalizer

fun HiddenSettingWithTargets.toDomain(): HiddenSetting =
    setting.toDomain(targets)

fun HiddenSettingEntity.toDomain(targets: List<SettingTargetEntity>): HiddenSetting =
    HiddenSetting(
        id = id,
        title = title,
        titleResId = titleResId,
        iconResId = iconResId,
        category = category,
        minSdkVersion = minSdkVersion,
        maxSdkVersion = maxSdkVersion,
        requiredMiuiVersion = requiredMiuiVersion,
        isLegacyOnly = isLegacyOnly,
        searchKeywords = searchKeywords.split(" ").filter { it.isNotBlank() },
        targets = targets.map { it.toDomain() }
    )

fun SettingTargetEntity.toDomain(): SettingTarget =
    SettingTarget(
        id = id,
        settingId = settingId,
        packageName = packageName,
        className = className,
        action = action,
        priority = priority,
        minSdkVersion = minSdkVersion,
        maxSdkVersion = maxSdkVersion,
        requiredMiuiVersion = requiredMiuiVersion,
        requiresExported = requiresExported,
        notes = notes
    )

fun CatalogPayload.toEntities(): Pair<List<HiddenSettingEntity>, List<SettingTargetEntity>> {
    val settings = mutableListOf<HiddenSettingEntity>()
    val targets = mutableListOf<SettingTargetEntity>()
    for (setting in this.settings) {
        settings.add(setting.toEntity())
        targets.addAll(setting.targets.map { it.toEntity() })
    }
    return settings to targets
}

fun HiddenSetting.toEntity(): HiddenSettingEntity =
    HiddenSettingEntity(
        id = id,
        title = title,
        titleResId = titleResId,
        iconResId = iconResId,
        category = category,
        minSdkVersion = minSdkVersion,
        maxSdkVersion = maxSdkVersion,
        requiredMiuiVersion = requiredMiuiVersion,
        isLegacyOnly = isLegacyOnly,
        searchKeywords = buildSearchKeywords(title, searchKeywords, category)
    )

fun SettingTarget.toEntity(): SettingTargetEntity =
    SettingTargetEntity(
        id = id,
        settingId = settingId,
        packageName = packageName,
        className = className,
        action = action,
        priority = priority,
        minSdkVersion = minSdkVersion,
        maxSdkVersion = maxSdkVersion,
        requiredMiuiVersion = requiredMiuiVersion,
        requiresExported = requiresExported,
        notes = notes
    )

private fun buildSearchKeywords(title: String, keywords: List<String>, category: String): String {
    val tokens = buildList {
        add(title)
        if (category.isNotBlank()) add(category)
        addAll(keywords)
    }.joinToString(" ")
    return TextNormalizer.normalize(tokens)
}
