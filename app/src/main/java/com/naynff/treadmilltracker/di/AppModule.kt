package com.naynff.treadmilltracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.naynff.treadmilltracker.data.local.TreadmillDao
import com.naynff.treadmilltracker.data.local.TreadmillDatabase
import com.naynff.treadmilltracker.data.local.WorkoutSessionDao
import com.naynff.treadmilltracker.data.repository.DataStoreSettingsRepository
import com.naynff.treadmilltracker.data.repository.RoomTreadmillRepository
import com.naynff.treadmilltracker.data.repository.RoomWorkoutRepository
import com.naynff.treadmilltracker.domain.repository.SettingsRepository
import com.naynff.treadmilltracker.domain.repository.TreadmillRepository
import com.naynff.treadmilltracker.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

private const val DATABASE_NAME = "treadmills.db"
private const val SETTINGS_FILE_NAME = "settings"

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindTreadmillRepository(impl: RoomTreadmillRepository): TreadmillRepository

    @Binds
    abstract fun bindWorkoutRepository(impl: RoomWorkoutRepository): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TreadmillDatabase =
        Room.databaseBuilder(context, TreadmillDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideTreadmillDao(database: TreadmillDatabase): TreadmillDao = database.treadmillDao()

    @Provides
    fun provideWorkoutSessionDao(database: TreadmillDatabase): WorkoutSessionDao = database.workoutSessionDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(SETTINGS_FILE_NAME)
        }

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
