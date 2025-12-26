package com.recodex.miuisettings.presentation.mapper

import com.recodex.miuisettings.domain.model.HiddenSetting
import com.recodex.miuisettings.presentation.model.SettingSummary

fun HiddenSetting.toSummary(): SettingSummary =
    SettingSummary(
        id = id,
        title = title,
        category = category,
        targetCount = targets.size,
        isLegacyOnly = isLegacyOnly
    )
