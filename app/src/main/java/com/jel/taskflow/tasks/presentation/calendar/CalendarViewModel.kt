package com.jel.taskflow.tasks.presentation.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository
import com.jel.taskflow.tasks.domain.use_case.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    val viewMode = _viewMode.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksByDate: StateFlow<Map<LocalDate, List<Task>>> = combine(
        userPreferencesRepository.taskSettingsFlow,
        _selectedDate,
        _viewMode
    ) { settings, date, mode ->
        Triple(settings, date, mode)
    }.flatMapLatest { (settings, date, mode) ->

        val zoneId = ZoneId.systemDefault()

        val (startLocalDate, endLocalDate) = when (mode) {
            CalendarViewMode.DAY -> Pair(date, date)

            CalendarViewMode.WEEK -> {
                val startOfWeek = date.minusDays((date.dayOfWeek.value % 7).toLong())
                Pair(startOfWeek, startOfWeek.plusDays(6))
            }

            CalendarViewMode.MONTH -> {
                val yearMonth = YearMonth.from(date)
                Pair(yearMonth.atDay(1), yearMonth.atEndOfMonth())
            }

            CalendarViewMode.YEAR -> {
                Pair(LocalDate.of(date.year, 1, 1), LocalDate.of(date.year, 12, 31))
            }
        }

        // start of the day (00:00)
        val startMillis = startLocalDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        // end of the day (23:59:59)
        val endMillis = endLocalDate.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        taskUseCases.getFilteredTasks(
            settings = settings,
            searchQuery = "",
            requireDueDate = true,
            dueDateStart = startMillis,
            dueDateEnd = endMillis
        )
    }.map { tasks ->
        Log.d("tasksByDate", "size: ${tasks.size}")
        tasks.groupBy { task ->
            Instant.ofEpochMilli(task.dueDate!!.toEpochMilliseconds())
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.also { Log.d("tasksByDate", "Keys in map: ${it.keys}") }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun onViewModeChanged(mode: CalendarViewMode) {
        _viewMode.value = mode
    }
}