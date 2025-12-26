package com.recodex.miuisettings.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidden_settings")
data class HiddenSettingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val titleResId: Int?,
    val iconResId: Int?,
    val category: String,
    val minSdkVersion: Int?,
    val maxSdkVersion: Int?,
    val requiredMiuiVersion: String?,
    val isLegacyOnly: Boolean,
    val searchKeywords: String
)
