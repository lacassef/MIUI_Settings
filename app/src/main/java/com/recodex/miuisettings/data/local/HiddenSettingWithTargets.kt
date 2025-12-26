package com.recodex.miuisettings.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class HiddenSettingWithTargets(
    @Embedded val setting: HiddenSettingEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "settingId"
    )
    val targets: List<SettingTargetEntity>
)
