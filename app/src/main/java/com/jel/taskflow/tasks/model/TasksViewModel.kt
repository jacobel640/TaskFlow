package com.jel.taskflow.tasks.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.tasks.repository.TaskRepository
import com.jel.taskflow.tasks.ui.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val tasks = repository.getTasks()
        .stateIn( // Flow to StateFlow converter
            scope = viewModelScope, // the scope the StateFlow lives in (the StateFlow life depends on the viewModel existence
            started = SharingStarted.WhileSubscribed(5_000L), // the offset life after the viewModel destroyed
            initialValue = emptyList() // while Flow is not have to hold content the StateFloe must have initial value
        )

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    private var deletedTask: Task? = null

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            if (taskId == -1L) return@launch
            val task = repository.getTaskById(taskId).firstOrNull()
            task?.let {
                deletedTask = it
                repository.deleteTask(task = it)
                _uiEvent.send(UiEvent.ShowUndoDeleteSnackbar)
            } ?: run {
                _uiEvent.send(UiEvent.ShowDeleteFailedSnackbar("cannot find task with id: $taskId"))
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
