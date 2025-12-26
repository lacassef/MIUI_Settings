package com.recodex.miuisettings.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HiddenSettingEntity::class, SettingTargetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SettingsDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
}
