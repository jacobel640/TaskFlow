package com.jel.taskflow.tasks.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.SortDirection
import com.jel.taskflow.tasks.domain.model.enums.SortType
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
import javax.inject.Inject

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

    val uiState: StateFlow<HomeUiState> = combine(
        filteredTasksFlow,
        userPreferencesRepository.taskSettingsFlow,
        _searchQuery,
        totalCountFlow
    ) { (filteredTasks, isLoading), settings, searchQuery, tasksCount ->
        HomeUiState(
            tasks = filteredTasks,
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

    fun onSortTypeChanged(sortType: SortType) {
        viewModelScope.launch {
            userPreferencesRepository.updateSortType(sortType)
        }
    }

    fun onSortDirectionChanged(sortDirection: SortDirection) {
        viewModelScope.launch {
            userPreferencesRepository.updateSortDirection(sortDirection)
        }
    }

    fun onFilterByPriorityChanged(priority: Priority) {
        viewModelScope.launch {
            userPreferencesRepository.togglePriorityFilter(priority)
        }
    }

    fun onFilterByStatusChanged(status: Status) {
        viewModelScope.launch {
            userPreferencesRepository.toggleStatusFilter(status)
        }
    }

    fun onShowCompletedTasksChanged(showCompletedTasks: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.toggleShowCompletedTasks(showCompletedTasks)
        }
    }

    fun onClearPriorityFilters() {
        viewModelScope.launch {
            userPreferencesRepository.clearPriorityFilters()
        }
    }

    fun onClearStatusFilters() {
        viewModelScope.launch {
            userPreferencesRepository.clearStatusFilters()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onClearFilters() {
        viewModelScope.launch {
            _searchQuery.value = ""
            userPreferencesRepository.clearFilters()
        }
    }

    private val _homeUiEvent = Channel<HomeUiEvent>()
    val uiEvent = _homeUiEvent.receiveAsFlow()
    private var deletedTask: Task? = null

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskUseCases.getTask(taskId).firstOrNull()?.let {
                deletedTask = it
                taskUseCases.deleteTask(it)
                _homeUiEvent.send(HomeUiEvent.ShowUndoDeleteSnackbar)
            } ?: run {
                _homeUiEvent.send(HomeUiEvent.ShowDeleteFailedSnackbar("cannot find task with id: $taskId"))
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
}