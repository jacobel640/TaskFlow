package com.jel.taskflow.tasks.domain.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jel.taskflow.tasks.domain.model.NotificationSettings
import com.jel.taskflow.tasks.domain.model.TaskSettings
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.SortDirection
import com.jel.taskflow.tasks.domain.model.enums.SortType
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.presentation.calendar.components.daysStartingSunday
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val NOTIFICATION_DAYS = stringSetPreferencesKey("notification_days")
        val SORT_TYPE = stringPreferencesKey("sort_type")
        val SORT_DIRECTION = stringPreferencesKey("sort_direction")
        val FILTER_BY_PRIORITY = stringSetPreferencesKey("filter_by_priority")
        val FILTER_BY_STATUS = stringSetPreferencesKey("filter_by_status")
        val SHOW_COMPLETED_TASKS = booleanPreferencesKey("show_completed_tasks")
    }

    val notificationSettingsFlow: Flow<NotificationSettings> = context.dataStore.data
        .map { preferences ->
            val hour = preferences[PreferencesKeys.NOTIFICATION_HOUR] ?: 9
            val minute = preferences[PreferencesKeys.NOTIFICATION_MINUTE] ?: 0
            val days = preferences[PreferencesKeys.NOTIFICATION_DAYS]
                ?.map { DayOfWeek.valueOf(it) }
                ?.toSet() ?: daysStartingSunday.toSet()

            NotificationSettings(hour, minute, days)
        }

    suspend fun updateNotificationSettings(hour: Int, minute: Int, days: Set<DayOfWeek>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_HOUR] = hour
            preferences[PreferencesKeys.NOTIFICATION_MINUTE] = minute
            preferences[PreferencesKeys.NOTIFICATION_DAYS] = days.map { it.name }.toSet()
        }
    }

    val taskSettingsFlow: Flow<TaskSettings> = context.dataStore.data
        .map { preferences ->
            val sortType = SortType.valueOf(
                preferences[PreferencesKeys.SORT_TYPE] ?: SortType.CREATED.name
            )
            val sortDirection = SortDirection.valueOf(
                preferences[PreferencesKeys.SORT_DIRECTION] ?: SortDirection.DESC.name
            )
            val filterByPriority = preferences[PreferencesKeys.FILTER_BY_PRIORITY]
                ?.map { Priority.valueOf(it) }
                ?.toSet() ?: emptySet()

            val filterByStatus = preferences[PreferencesKeys.FILTER_BY_STATUS]
                ?.map { Status.valueOf(it) }
                ?.toSet()
                ?: setOf(Status.TODO, Status.IN_PROGRESS)
            val showCompletedTasks = preferences[PreferencesKeys.SHOW_COMPLETED_TASKS] ?: false

            TaskSettings(sortType, sortDirection, filterByPriority, filterByStatus, showCompletedTasks)
        }

    suspend fun updateSortType(sortType: SortType) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_TYPE] = sortType.name
        }
    }

    suspend fun updateSortDirection(sortDirection: SortDirection) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_DIRECTION] = sortDirection.name
        }
    }

    suspend fun togglePriorityFilter(priority: Priority) {
        context.dataStore.edit { preferences ->
            val currentPriorities = preferences[PreferencesKeys.FILTER_BY_PRIORITY] ?: emptySet()
            val newPriorities = if (priority.name in currentPriorities) {
                currentPriorities - priority.name
            } else {
                currentPriorities + priority.name
            }
            preferences[PreferencesKeys.FILTER_BY_PRIORITY] = newPriorities
        }
    }

    suspend fun toggleStatusFilter(status: Status) {
        context.dataStore.edit { preferences ->
            val currentStatuses = preferences[PreferencesKeys.FILTER_BY_STATUS] ?: emptySet()
            val newStatuses = if (status.name in currentStatuses) {
                currentStatuses - status.name
            } else {
                currentStatuses + status.name
            }
            preferences[PreferencesKeys.FILTER_BY_STATUS] = newStatuses
        }
    }

    suspend fun clearPriorityFilters() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FILTER_BY_PRIORITY] = emptySet()
        }
    }

    suspend fun clearStatusFilters() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FILTER_BY_STATUS] = emptySet()
        }
    }

    suspend fun toggleShowCompletedTasks(showCompletedTasks: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_COMPLETED_TASKS] = showCompletedTasks
        }
    }

    suspend fun clearFilters() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FILTER_BY_STATUS] = emptySet()
            preferences[PreferencesKeys.FILTER_BY_PRIORITY] = emptySet()
        }
    }
}