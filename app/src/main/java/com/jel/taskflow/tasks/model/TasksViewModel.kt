package com.jel.taskflow.tasks.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.tasks.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
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

    private val _errorEvents = Channel<String>(capacity = Channel.BUFFERED)
    val errorEvent = _errorEvents.receiveAsFlow()

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            task?.let {
                repository.deleteTask(task)
            }
        }
    }
}