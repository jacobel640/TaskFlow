package com.jel.taskflow.tasks.presentation.single_task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jel.taskflow.core.utils.Screen
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.domain.use_case.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SingleTaskViewModel @Inject constructor(
    private val useCases: TaskUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val route = savedStateHandle.toRoute<Screen.SingleTaskScreen>()
    val taskId = route.task.id

    var currentTask = taskId?.let { taskId ->
        useCases.getTask(taskId)
            .filterNotNull()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = route.task
            )
    } ?: flowOf(route.task).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = route.task
    )

    fun onStatusChanged(newStatus: Status) {
        viewModelScope.launch {
            useCases.insertTask(currentTask.first().copy(status = newStatus))
        }
    }
}