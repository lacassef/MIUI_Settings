package com.recodex.miuisettings.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Transaction
    @Query("SELECT * FROM hidden_settings")
    fun observeSettings(): Flow<List<HiddenSettingWithTargets>>

    @Transaction
    @Query("SELECT * FROM hidden_settings")
    suspend fun getSettings(): List<HiddenSettingWithTargets>

    @Transaction
    @Query("SELECT * FROM hidden_settings WHERE id = :settingId")
    suspend fun getSetting(settingId: String): HiddenSettingWithTargets?

    @Query("SELECT COUNT(*) FROM hidden_settings")
    suspend fun countSettings(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<HiddenSettingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTargets(targets: List<SettingTargetEntity>)

    @Query("DELETE FROM setting_targets")
    suspend fun clearTargets()

    @Query("DELETE FROM hidden_settings")
    suspend fun clearSettings()

    @Transaction
    suspend fun replaceCatalog(
        settings: List<HiddenSettingEntity>,
        targets: List<SettingTargetEntity>
    ) {
        clearTargets()
        clearSettings()
        insertSettings(settings)
        insertTargets(targets)
    }
}
