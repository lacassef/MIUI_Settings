package com.recodex.miuisettings.di

import android.content.Context
import androidx.room.Room
import com.recodex.miuisettings.data.local.SettingsDao
import com.recodex.miuisettings.data.local.SettingsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SettingsDatabase =
        Room.databaseBuilder(context, SettingsDatabase::class.java, "eclipse_settings.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSettingsDao(database: SettingsDatabase): SettingsDao = database.settingsDao()
}
