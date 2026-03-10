package com.jel.taskflow.tasks.presentation.add_edit_task

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.core.utils.TaskScreen
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.domain.use_case.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val useCases: TaskUseCases,
    savedStateHandle: SavedStateHandle // this responsible for injecting taskId and other arguments passing by the nav controller
) : ViewModel() {

    private val uiStatesHistory = mutableListOf<AddEditTaskUiState>()
    private var uiStateHistoryPosition = -1

    private var typingJob: Job? = null
    private val MAX_HISTORY_SIZE = 50

    val currentTaskId: Long? = savedStateHandle.get<Long>(TaskScreen.TASK_ID_ARG)
        ?.takeIf { it != -1L }
    private val _uiState = MutableStateFlow(AddEditTaskUiState())

    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    init { initTaskUiState() }

    private fun initTaskUiState() {
        if (currentTaskId != null) {
            viewModelScope.launch {
                useCases.getTask(currentTaskId).filterNotNull().first().let { task ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            title = TextFieldValue(
                                text = task.title,
                                selection = TextRange(task.title.length)
                            ),
                            content = TextFieldValue(
                                text = task.content,
                                selection = TextRange(task.content.length)
                            ),
                            status = task.status,
                            priority = task.priority,
                            dueDate = task.dueDate,
                            createdDate = task.createdDate,
                            changedDate = task.changedDate,
                            isLoading = false,
                            currentTaskChanged = false,
                            canUndo = canRevertBackwards(),
                            canRedo = canRevertForwards()
                        )
                    }
                    uiStatesHistory.add(++uiStateHistoryPosition, _uiState.value)
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
            uiStatesHistory.add(++uiStateHistoryPosition, _uiState.value)
        }
    }

    fun onTitleChanged(newTitle: TextFieldValue) {
        val currentTitleText = _uiState.value.title.text
        onCurrentTaskPropertyChanged(title = newTitle)
        if (currentTitleText != newTitle.text) updateUiStateHistory()
    }

    fun onContentChanged(newContent: TextFieldValue) {
        val currentContentText = _uiState.value.content.text
        onCurrentTaskPropertyChanged(content = newContent)
        if (currentContentText != newContent.text) updateUiStateHistory()
    }

    fun onStatusChanged(newStatus: Status) {
        onCurrentTaskPropertyChanged(
            status = newStatus,
            currentTaskChanged = true
        )
    }

    fun onPriorityChanged(newPriority: Priority) {
        onCurrentTaskPropertyChanged(
            priority = newPriority,
            currentTaskChanged = true
        )
    }

    fun onDueDateChanged(newDate: Instant?) {
        onCurrentTaskPropertyChanged(
            dueDate = newDate,
            currentTaskChanged = true
        )
    }

    fun onCurrentTaskPropertyChanged(
        title: TextFieldValue = _uiState.value.title,
        content: TextFieldValue = _uiState.value.content,
        status: Status = _uiState.value.status,
        priority: Priority = _uiState.value.priority,
        dueDate: Instant? = _uiState.value.dueDate,
        currentTaskChanged: Boolean = uiState.value.currentTaskChanged
    ) {
        _uiState.update { state ->
            state.copy(
                title = title,
                content = content,
                status = status,
                priority = priority,
                dueDate = dueDate,
                changedDate = Clock.System.now(),
                currentTaskChanged = currentTaskChanged || uiStateHistoryPosition > 0
            )
        }
    }

    fun updateUiStateHistory() {

        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(500)

            uiStatesHistory.add(++uiStateHistoryPosition, _uiState.value)

            if (uiStatesHistory.size > MAX_HISTORY_SIZE) {
                uiStatesHistory.removeAt(0)
                uiStateHistoryPosition--
            }

            _uiState.update { state ->
                state.copy(
                    canUndo = canRevertBackwards(),
                    canRedo = canRevertForwards()
                )
            }
        }
    }

    fun canRevertBackwards(): Boolean = uiStateHistoryPosition > 0
    fun canRevertForwards(): Boolean = uiStateHistoryPosition < uiStatesHistory.size - 1

    fun revertChanges(forwards: Boolean = false) {
        _uiState.update {
            if (forwards) getUiTextStateAt(++uiStateHistoryPosition)
            else getUiTextStateAt(--uiStateHistoryPosition)
        }
        println("LOG revertChanges uiStateHistoryPosition: $uiStateHistoryPosition, forwards: $forwards")
    }

    fun getUiTextStateAt(index: Int): AddEditTaskUiState =
        uiStatesHistory[index].copy(
            status = _uiState.value.status,
            priority = _uiState.value.priority,
            canUndo = canRevertBackwards(),
            canRedo = canRevertForwards()
        )

    fun reverseChanges() {
        uiStatesHistory.clear()
        uiStateHistoryPosition = -1
        initTaskUiState()
    }

    fun shouldSaveTask(): Boolean =
        (_uiState.value.currentTaskChanged || currentTaskId == null) &&
                (_uiState.value.title.text.isNotBlank() || _uiState.value.content.text.isNotBlank())

    fun saveTask() {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(currentTaskChanged = false) }
            useCases.insertTask(_uiState.value.toTask(currentTaskId))
        }
    }
}