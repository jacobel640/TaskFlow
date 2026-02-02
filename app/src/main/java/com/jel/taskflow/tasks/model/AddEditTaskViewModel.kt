package com.jel.taskflow.tasks.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.tasks.model.enums.Priority
import com.jel.taskflow.tasks.model.enums.Status
import com.jel.taskflow.tasks.repository.TaskRepository
import com.jel.taskflow.tasks.ui.AddEditTaskUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
                observeTask(taskId)
            } else uiStatesHistory.add(uiState)
        }
    }

    private fun observeTask(taskId: Long) {
        viewModelScope.launch {
            repository.getTaskById(taskId).collect { task ->
                task?.let {
                    uiStateHistoryPosition = 0
                    uiState = uiState.copy(
                        title = task.title,
                        content = task.content,
                        status = task.status,
                        priority = task.priority,
                        createdDate = task.createdDate,
                        changedDate = task.changedDate,
                        editMode = true,
                        isLoading = false,
                        currentTaskChanged = false,
                        canUndo = canRevertBackwards(),
                        canRedo = canRevertForwards()
                    )
                    uiStatesHistory.add(uiState)
                }
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        viewModelScope.launch {
            uiState = uiState.copy(title = newTitle)
            onCurrentTaskPropertyChanged()
            updateUiStateHistory()
        }
    }

    fun onContentChanged(newContent: String) {
        viewModelScope.launch {
            uiState = uiState.copy(content = newContent)
            onCurrentTaskPropertyChanged()
            updateUiStateHistory()
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
            changedDate = Clock.System.now(),
            currentTaskChanged = uiState.title.isNotBlank() || uiState.content.isNotBlank()
        )
    }

    fun updateUiStateHistory() {
        uiStatesHistory.add(++uiStateHistoryPosition, uiState)

        uiState = uiState.copy(
            canUndo = canRevertBackwards(),
            canRedo = canRevertForwards()
        )
    }

    fun canRevertBackwards(): Boolean = uiStateHistoryPosition > 0
    fun canRevertForwards(): Boolean = uiStateHistoryPosition < uiStatesHistory.size - 1

    fun revertChanges(forwards: Boolean = false) {
        uiState = if (forwards) getUiTextStateAt(++uiStateHistoryPosition)
        else getUiTextStateAt(--uiStateHistoryPosition)
    }

    fun getUiTextStateAt(index: Int): AddEditTaskUiState = uiStatesHistory[index].copy(
        status = uiState.status,
        priority = uiState.priority,
        canUndo = canRevertBackwards(),
        canRedo = canRevertForwards()
    )

    fun reverseChanges() {
        currentTaskId?.let {
            observeTask(it)
        }
    }

    fun saveTask() {
        viewModelScope.launch {
            uiState = uiState.copy(currentTaskChanged = false)
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
        }
    }
}