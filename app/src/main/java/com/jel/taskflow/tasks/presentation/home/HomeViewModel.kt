package com.jel.taskflow.tasks.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.tasks.domain.Task
import com.jel.taskflow.tasks.domain.TaskRepository
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val tasks = repository.getTasks()
        .stateIn( // Flow to StateFlow converter
            scope = viewModelScope, // the scope the StateFlow lives in (the StateFlow life depends on the viewModel existence
            started = SharingStarted.WhileSubscribed(5_000L), // the offset life after the viewModel destroyed
            initialValue = emptyList() // while Flow is not have to hold content the StateFloe must have initial value
        )

    private val _homeUiEvent = Channel<HomeUiEvent>()
    val uiEvent = _homeUiEvent.receiveAsFlow()
    private var deletedTask: Task? = null

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.getTaskById(taskId).firstOrNull()?.let {
                deletedTask = it
                repository.deleteTask(it)
                _homeUiEvent.send(HomeUiEvent.ShowUndoDeleteSnackbar)
            } ?: run {
                _homeUiEvent.send(HomeUiEvent.ShowDeleteFailedSnackbar("cannot find task with id: $taskId"))
            }
        }
    }

    fun restoreDeletedTask() {
        deletedTask?.let {
            viewModelScope.launch {
                repository.insertTask(task = it)
                deletedTask = null
            }
        }
    }
}