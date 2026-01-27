package com.jel.taskflow.tasks.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.tasks.repository.TaskRepository
import com.jel.taskflow.tasks.ui.AddEditTaskUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle // this responsible for injecting taskId and other arguments passing by the nav controller
) : ViewModel() {

    var uiState by mutableStateOf(AddEditTaskUiState())
        private set

    private val uiStatesHistory = mutableListOf<AddEditTaskUiState>()
    private var uiStateHistoryPosition = 0

    var currentTaskId: Long? = null

    init {
        savedStateHandle.get<Long>("taskId")?.let { taskId ->
            println("taskId=$taskId")
            if (taskId != -1L) {
                currentTaskId = taskId
                loadTask(taskId)
            } else uiStatesHistory.add(uiState)
        }
    }

    private fun loadTask(taskId: Long) {
        viewModelScope.launch {
            repository.getTaskById(taskId)?.let { task ->
                uiState = uiState.copy(
                    title = task.title,
                    content = task.content,
                    status = task.status,
                    priority = task.priority,
                    createdDate = task.createdDate,
                    changedDate = task.changedDate,
                    isLoading = false,
                    currentTaskChanged = false
                )
                uiStatesHistory.add(uiState)
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        viewModelScope.launch {
            uiState = uiState.copy(title = newTitle)
            onCurrentTaskPropertyChanged()
        }
    }

    fun onContentChanged(newContent: String) {
        viewModelScope.launch {
            uiState = uiState.copy(content = newContent)
            onCurrentTaskPropertyChanged()
        }
    }

    fun onStatusChanged(newStatus: Status) {
        viewModelScope.launch {
            uiState = uiState.copy(status = newStatus)
            onCurrentTaskPropertyChanged()
        }
    }

    fun onPriorityChanged(newPriority: Priority) {
        viewModelScope.launch {
            uiState = uiState.copy(priority = newPriority)
            onCurrentTaskPropertyChanged()
        }
    }

    fun onCurrentTaskPropertyChanged() {
        uiState = uiState.copy(
            changedDate = Date(Clock.System.now().toEpochMilliseconds()),
            currentTaskChanged = uiState.title.isNotBlank() && uiState.content.isNotBlank()
        )
        uiStatesHistory.add(uiState)
        uiStateHistoryPosition = uiStatesHistory.size-1
        println(uiState.toString())
    }

    fun canRevertBackwards(): Boolean = uiStateHistoryPosition > 0
    fun canRevertForwards(): Boolean = uiStateHistoryPosition < uiStatesHistory.size

    fun revertChangesBackwards() {
        uiState = uiStatesHistory[--uiStateHistoryPosition].copy(
            status = uiState.status,
            priority = uiState.priority
        )
    }

    fun revertChangesForwards() {
        uiState = uiStatesHistory[++uiStateHistoryPosition].copy(
            status = uiState.status,
            priority = uiState.priority
        )
    }

    fun reverseChanges() {
        currentTaskId?.let {
            loadTask(it)
        }
    }

    fun saveTask() {
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    id = currentTaskId,
                    title = uiState.title,
                    content = uiState.content,
                    status = uiState.status,
                    priority = uiState.priority,
                    createdDate = uiState.createdDate,
                    changedDate = uiState.changedDate,
                )
            )
            uiState = uiState.copy()
        }
    }
}