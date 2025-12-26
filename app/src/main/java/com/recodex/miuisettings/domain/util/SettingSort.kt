package com.recodex.miuisettings.domain.util

import com.recodex.miuisettings.domain.model.HiddenSetting

fun HiddenSetting.maxTargetPriority(): Int =
    targets.maxOfOrNull { it.priority } ?: 0
