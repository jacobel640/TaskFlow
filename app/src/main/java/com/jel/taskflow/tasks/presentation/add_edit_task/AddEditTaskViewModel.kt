package com.jel.taskflow.tasks.presentation.add_edit_task

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jel.taskflow.core.utils.TaskScreen
import com.jel.taskflow.core.utils.toRelativeDay
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

enum class FocusedTextField { TITLE, CONTENT }
@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val useCases: TaskUseCases,
    savedStateHandle: SavedStateHandle // this responsible for injecting taskId and other arguments passing by the nav controller
) : ViewModel() {

    private val titleStatesHistory = mutableListOf<TextFieldValue>()
    private var titleHistoryPosition = -1
    private val contentStatesHistory = mutableListOf<TextFieldValue>()
    private var contentHistoryPosition = -1
    private var lastFocusedField = FocusedTextField.CONTENT
    private var typingJob: Job? = null
    private val MAX_HISTORY_SIZE = 50

    var currentTaskId: Long? = savedStateHandle.get<Long>(TaskScreen.TASK_ID_ARG)
        ?.takeIf { it != -1L }
    val calendarSelectedDueDate: Long? = savedStateHandle.get<Long>(TaskScreen.INSTANT_MILLI_ARG)
        ?.takeIf { it != -1L }
    private val _uiState = MutableStateFlow(AddEditTaskUiState())
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    init { initTaskUiState() }

    private fun initTaskUiState() {
        currentTaskId?.let {
            viewModelScope.launch {
                useCases.getTask(it).filterNotNull().first().let { task ->
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
                            canUndo = false,
                            canRedo = false
                        )
                    }
                    initTextFieldStatesHistory()
                }
            }
        } ?: run {
            _uiState.update { state ->
                val dueDate = calendarSelectedDueDate?.let { Instant.fromEpochMilliseconds(it) }
                Log.d("### dueDate ###", "${dueDate?.toRelativeDay()}")
                Log.d("### dueDateMilli ###", "$calendarSelectedDueDate")
                state.copy(
                    isLoading = false,
                    dueDate = dueDate
                )
            }
            initTextFieldStatesHistory()
        }
    }

    private fun initTextFieldStatesHistory() {
        titleStatesHistory.add(++titleHistoryPosition, _uiState.value.title)
        contentStatesHistory.add(++contentHistoryPosition, _uiState.value.content)
    }

    fun onTitleChanged(newTitle: TextFieldValue) {
        val currentTitleText = _uiState.value.title.text
        onCurrentTaskPropertyChanged(title = newTitle)
        if (currentTitleText != newTitle.text)
            updateUiStateHistory(FocusedTextField.TITLE)
    }

    fun onContentChanged(newContent: TextFieldValue) {
        val currentContentText = _uiState.value.content.text
        onCurrentTaskPropertyChanged(content = newContent)
        if (currentContentText != newContent.text)
            updateUiStateHistory(FocusedTextField.CONTENT)
    }

    fun onStatusChanged(newStatus: Status) {
        onCurrentTaskPropertyChanged(
            status = newStatus,
            currentTaskChanged = true
        )
    }

    fun onPriorityChanged(newPriority: Priority?) {
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
        priority: Priority? = _uiState.value.priority,
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
                currentTaskChanged = currentTaskChanged ||
                        (titleHistoryPosition > 0 || contentHistoryPosition > 0)
            )
        }
    }

    fun updateUiStateHistory(field: FocusedTextField) {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(500)

            when(field) {
                FocusedTextField.TITLE -> {
                    titleStatesHistory.add(++titleHistoryPosition, _uiState.value.title)

                    if (titleStatesHistory.size > MAX_HISTORY_SIZE) {
                        titleStatesHistory.removeAt(0)
                        titleHistoryPosition--
                    }
                }
                FocusedTextField.CONTENT -> {
                    contentStatesHistory.add(++contentHistoryPosition, _uiState.value.content)

                    if (contentStatesHistory.size > MAX_HISTORY_SIZE) {
                        contentStatesHistory.removeAt(0)
                        contentHistoryPosition--
                    }
                }
            }
            updateUndoRedoButtonsState()
        }
    }

    fun onTextFieldFocusChanged(focusedTextField: FocusedTextField, isFocused: Boolean = true) {
        if (isFocused) {
            lastFocusedField = focusedTextField
            updateUndoRedoButtonsState()
        }
    }

    fun onFocusConsumed() {
        _uiState.update { state -> state.copy(fieldToFocus = null) }
    }

    fun updateUndoRedoButtonsState() {
        val canUndo = when (lastFocusedField) {
            FocusedTextField.TITLE -> titleHistoryPosition > 0
            FocusedTextField.CONTENT -> contentHistoryPosition > 0
        }

        val canRedo = when (lastFocusedField) {
            FocusedTextField.TITLE -> titleHistoryPosition < titleStatesHistory.size - 1
            FocusedTextField.CONTENT -> contentHistoryPosition < contentStatesHistory.size - 1
        }

        _uiState.update { state ->
            state.copy(
                canUndo = canUndo,
                canRedo = canRedo,
                fieldToFocus = lastFocusedField
            )
        }
    }
    fun revertChanges(forwards: Boolean = false) {
        when (lastFocusedField) {
            FocusedTextField.TITLE -> {
                if (forwards && _uiState.value.canRedo) titleHistoryPosition++
                else if (_uiState.value.canUndo) titleHistoryPosition--
                onCurrentTaskPropertyChanged(title = titleStatesHistory[titleHistoryPosition])
            }
            FocusedTextField.CONTENT -> {
                if (forwards && _uiState.value.canRedo) contentHistoryPosition++
                else if (_uiState.value.canUndo) contentHistoryPosition--
                onCurrentTaskPropertyChanged(content = contentStatesHistory[contentHistoryPosition])
            }
        }

        updateUndoRedoButtonsState()
    }

    fun reverseChanges() {
        titleStatesHistory.clear()
        contentStatesHistory.clear()
        titleHistoryPosition = -1
        contentHistoryPosition = -1
        initTaskUiState()
    }

    fun shouldSaveTask(): Boolean =
        (_uiState.value.currentTaskChanged || currentTaskId == null) &&
                (_uiState.value.title.text.isNotBlank() || _uiState.value.content.text.isNotBlank())

    fun saveTask() {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(currentTaskChanged = false) }
            currentTaskId = useCases.insertTask(_uiState.value.toTask(currentTaskId))
        }
    }
}