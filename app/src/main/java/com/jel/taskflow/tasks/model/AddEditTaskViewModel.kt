package com.jel.taskflow.tasks.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
    private var uiStateHistoryPosition = -1

    var currentTaskId: Long? = null

    init {
        savedStateHandle.get<Long>("taskId")
            ?.takeIf { it != -1L }
            ?.let { taskId ->
                currentTaskId = taskId
                observeTask(taskId)
            } ?: run {
                uiState = uiState.copy(isLoading = false)
                uiStatesHistory.add(++uiStateHistoryPosition, uiState)
            }
    }

    private fun observeTask(taskId: Long) {
        println("LOG observeTask uiStateHistoryPosition: $uiStateHistoryPosition")
        viewModelScope.launch {
            repository.getTaskById(taskId).collect { task ->
                task?.let {
                    uiState = uiState.copy(
                        title = task.title,
                        content = TextFieldValue(
                            text = task.content,
                            selection = TextRange(task.content.length)
                        ),
                        status = task.status,
                        priority = task.priority,
                        createdDate = task.createdDate,
                        changedDate = task.changedDate,
                        isLoading = false,
                        currentTaskChanged = false,
                        canUndo = canRevertBackwards(),
                        canRedo = canRevertForwards()
                    )
                    if (uiStatesHistory.isEmpty()) {
                        uiStatesHistory.add(++uiStateHistoryPosition, uiState)
                    }
                } ?: run {
                    // TODO handle non existing taskId case
                    uiState = uiState.copy(
                        isLoading = false,
                        canUndo = canRevertBackwards(),
                        canRedo = canRevertForwards()
                    )
                }
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        uiState = uiState.copy(title = newTitle)
        onCurrentTaskPropertyChanged()
        updateUiStateHistory()
    }

    fun onContentChanged(newContent: TextFieldValue) {
        uiState = uiState.copy(content = newContent)
        onCurrentTaskPropertyChanged()
        updateUiStateHistory()
    }

    fun onStatusChanged(newStatus: Status) {
        uiState = uiState.copy(status = newStatus)
        onCurrentTaskPropertyChanged()
    }

    fun onPriorityChanged(newPriority: Priority) {
        uiState = uiState.copy(priority = newPriority)
        onCurrentTaskPropertyChanged()
    }

    fun onCurrentTaskPropertyChanged() {
        uiState = uiState.copy(
            changedDate = Clock.System.now(),
            currentTaskChanged = uiStatesHistory.isNotEmpty()
        )
    }

    fun updateUiStateHistory() {
        uiStatesHistory.add(++uiStateHistoryPosition, uiState)
        println("LOG updateUiStateHistory uiStateHistoryPosition: $uiStateHistoryPosition")
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
        println("LOG revertChanges uiStateHistoryPosition: $uiStateHistoryPosition, forwards: $forwards")
    }

    fun getUiTextStateAt(index: Int): AddEditTaskUiState =
        uiStatesHistory[index].copy(
            status = uiState.status,
            priority = uiState.priority,
            canUndo = canRevertBackwards(),
            canRedo = canRevertForwards()
        )

    fun reverseChanges() {
        uiStatesHistory.clear()
        uiStateHistoryPosition = -1
        currentTaskId?.let {
            observeTask(it)
        } ?: run {
            uiState = AddEditTaskUiState(
                isLoading = false,
                canUndo = canRevertBackwards(),
                canRedo = canRevertForwards()
            )
        }
    }

    fun shouldSaveTask(): Boolean =
        uiState.currentTaskChanged || currentTaskId == null &&
                (uiState.title.isNotBlank() || uiState.content.text.isNotBlank())

    fun saveTask() {
        viewModelScope.launch {
            uiState = uiState.copy(currentTaskChanged = false)
            repository.insertTask(
                Task(
                    id = currentTaskId,
                    title = uiState.title,
                    content = uiState.content.text,
                    status = uiState.status,
                    priority = uiState.priority,
                    createdDate = uiState.createdDate,
                    changedDate = uiState.changedDate,
                )
            )
        }
    }
}