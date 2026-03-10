package com.jel.taskflow.tasks.presentation.single_task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.core.utils.TaskScreen
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.domain.use_case.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SingleTaskViewModel @Inject constructor(
    private val useCases: TaskUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val taskId: Long = requireNotNull(savedStateHandle.get<Long>(TaskScreen.TASK_ID_ARG)) {
        "SingleTaskViewModel requires a non-null taskId argument" // exception log message...
    }

    val uiState = useCases.getTask(taskId)
        .filterNotNull()
        .map { task -> SingleTaskUiState.Success(task) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SingleTaskUiState.Loading
        )

    fun onStatusChanged(newStatus: Status) {
        val uiState = uiState.value

        if (uiState is SingleTaskUiState.Success) {
            viewModelScope.launch {
                val taskToUpdate = uiState.task.copy(status = newStatus)
                useCases.insertTask(taskToUpdate)
            }
        }
    }
}