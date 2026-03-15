package com.jel.taskflow.tasks.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.tasks.domain.model.NotificationSettings
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository
import com.jel.taskflow.tasks.domain.use_case.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import java.time.Instant as JavaInstant

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val totalCountFlow = taskUseCases.getTasksCount()

    @OptIn(FlowPreview::class)
    private val filteredTasksFlow: StateFlow<Pair<List<Task>, Boolean>> = combine(
        userPreferencesRepository.taskSettingsFlow,
        _searchQuery.debounce { if (it.isNotEmpty()) 300L else 0L }
    ) { settings, searchQuery ->
        taskUseCases.getFilteredTasks(settings, searchQuery)
            .map { tasks -> Pair(tasks, false) }
            .onStart { emit(Pair(emptyList(), true)) }

    }.flatMapLatest { it }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = Pair(emptyList(), true)
    )

    private val _uiEvent = Channel<HomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        filteredTasksFlow,
        userPreferencesRepository.taskSettingsFlow,
        _searchQuery,
        totalCountFlow
    ) { (filteredTasks, isLoading), settings, searchQuery, tasksCount ->

        val today = LocalDate.now()
        val todayTasks = mutableListOf<Task>()
        val otherTasks = mutableListOf<Task>()

        filteredTasks.forEach { task ->
            if (task.dueDate != null) {
                // convert Kotlin Instant to LocalDate from Java
                val taskDate = JavaInstant.ofEpochMilli(task.dueDate.toEpochMilliseconds())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                if (taskDate == today) {
                    todayTasks.add(task)
                } else otherTasks.add(task)
            } else otherTasks.add(task)
        }

        HomeUiState(
            todayTasks = todayTasks,
            otherTasks = otherTasks,
            settings = settings,
            searchQuery = searchQuery,
            isLoading = isLoading,
            tasksCount = tasksCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState()
    )

    fun onUiAction(uiActions: HomeUiActions) {
        when(uiActions) {
            is HomeUiActions.OnToggleCompleteTask -> toggleCompleteTask(uiActions.task)
            is HomeUiActions.OnUndoToggleCompleteTask -> undoToggleCompleteTask()
            is HomeUiActions.OnDeleteTask -> deleteTask(uiActions.taskId)
            is HomeUiActions.OnUndoDeleteTask -> restoreDeletedTask()
            is HomeUiActions.OnClearFilters -> viewModelScope.launch {
                userPreferencesRepository.clearFilters()
                _searchQuery.value = ""
            }
            is HomeUiActions.OnSearchQueryChange -> { _searchQuery.value = uiActions.query }
            is HomeUiActions.OnToggleStatusFilters -> viewModelScope.launch {
                userPreferencesRepository.toggleStatusFilter(uiActions.status)
            }
            is HomeUiActions.OnClearStatusFilters -> viewModelScope.launch {
                userPreferencesRepository.clearStatusFilters()
            }
            is HomeUiActions.OnTogglePriorityFilters -> viewModelScope.launch {
                userPreferencesRepository.togglePriorityFilter(uiActions.priority)
            }
            is HomeUiActions.OnClearPriorityFilters -> viewModelScope.launch {
                userPreferencesRepository.clearPriorityFilters()
            }
            is HomeUiActions.OnToggleShowCompletedTasks -> viewModelScope.launch {
                userPreferencesRepository.toggleShowCompletedTasks(uiActions.show)
            }
            is HomeUiActions.OnSortTypeChange -> viewModelScope.launch {
                userPreferencesRepository.updateSortType(uiActions.sortType)
            }
            is HomeUiActions.OnSortDirectionChange -> viewModelScope.launch {
                userPreferencesRepository.updateSortDirection(uiActions.direction)
            }
        }
    }
    private var deletedTask: Task? = null

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskUseCases.getTask(taskId).firstOrNull()?.let {
                deletedTask = it
                taskUseCases.deleteTask(it)
                _uiEvent.send(HomeUiEvent.ShowUndoDeleteSnackbar)
            } ?: run {
                _uiEvent.send(HomeUiEvent.ShowDeleteFailedSnackbar(taskId))
            }
        }
    }

    fun restoreDeletedTask() {
        deletedTask?.let {
            viewModelScope.launch {
                taskUseCases.insertTask(task = it)
                deletedTask = null
            }
        }
    }

    private var recentlyCompletedTask: Task? = null

    private fun toggleCompleteTask(task: Task) {
        viewModelScope.launch {
            recentlyCompletedTask = task
            val targetStatus = if (task.status == Status.COMPLETED) Status.TODO else Status.COMPLETED
            val toggledCompleteTask = task.copy(status = targetStatus)
            taskUseCases.insertTask(toggledCompleteTask)

            _uiEvent.send(
                HomeUiEvent.ShowTaskCompletedSnackbar(
                    toggledStatus = targetStatus
                )
            )
        }
    }

    private fun undoToggleCompleteTask() {
        viewModelScope.launch {
            recentlyCompletedTask?.let { task ->
                taskUseCases.insertTask(task)
                recentlyCompletedTask = null
            }
        }
    }

    val notificationSettings = userPreferencesRepository.notificationSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = NotificationSettings()
        )

    fun updateNotificationSettings(hour: Int, minute: Int, days: Set<DayOfWeek>) {
        viewModelScope.launch {
            userPreferencesRepository.updateNotificationSettings(hour, minute, days)
        }
    }
}