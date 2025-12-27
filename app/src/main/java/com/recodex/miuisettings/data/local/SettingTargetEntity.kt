package com.recodex.miuisettings.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "setting_targets",
    foreignKeys = [
        ForeignKey(
            entity = HiddenSettingEntity::class,
            parentColumns = ["id"],
            childColumns = ["settingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("settingId")]
)
data class SettingTargetEntity(
    @PrimaryKey val id: String,
    val settingId: String,
    val packageName: String,
    val className: String?,
    val action: String?,
    val extrasJson: String?,
    val priority: Int,
    val minSdkVersion: Int?,
    val maxSdkVersion: Int?,
    val requiredMiuiVersion: String?,
    val requiresExported: Boolean,
    val notes: String?
)
