package com.naynff.treadmilltracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naynff.treadmilltracker.domain.model.AppSettings
import com.naynff.treadmilltracker.domain.model.FreshnessThresholds
import com.naynff.treadmilltracker.domain.model.TreadmillSide
import com.naynff.treadmilltracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val TREADMILL_COUNT = intPreferencesKey("treadmill_count")
private val RECENT_DAYS = intPreferencesKey("recent_days")
private val OLD_DAYS = intPreferencesKey("old_days")
private val TREADMILL_SIDE = stringPreferencesKey("treadmill_side")

class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override val settings: Flow<AppSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { it.toSettings() }

    override suspend fun setTreadmillCount(count: Int) {
        dataStore.edit { it[TREADMILL_COUNT] = count }
    }

    override suspend fun setThresholds(thresholds: FreshnessThresholds) {
        dataStore.edit {
            it[RECENT_DAYS] = thresholds.recentDays
            it[OLD_DAYS] = thresholds.oldDays
        }
    }

    override suspend fun setTreadmillSide(side: TreadmillSide) {
        dataStore.edit { it[TREADMILL_SIDE] = side.name }
    }

    private fun Preferences.toSettings(): AppSettings {
        val recentDays = this[RECENT_DAYS] ?: FreshnessThresholds.DEFAULT_RECENT_DAYS
        val oldDays = this[OLD_DAYS] ?: FreshnessThresholds.DEFAULT_OLD_DAYS
        return AppSettings(
            treadmillCount = this[TREADMILL_COUNT],
            thresholds = if (FreshnessThresholds.isValid(recentDays, oldDays)) {
                FreshnessThresholds(recentDays, oldDays)
            } else {
                FreshnessThresholds()
            },
            treadmillSide = TreadmillSide.entries.find { it.name == this[TREADMILL_SIDE] }
                ?: TreadmillSide.LEFT,
        )
    }
}
