package com.jel.taskflow.tasks.presentation.add_edit_task

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import com.jel.taskflow.auth.data.repository.LocalOnlyAuthRepository
import com.jel.taskflow.core.utils.TaskScreen
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.TaskSettings
import com.jel.taskflow.tasks.domain.repository.TaskRepository
import com.jel.taskflow.tasks.domain.use_case.DeleteTask
import com.jel.taskflow.tasks.domain.use_case.GetFilteredTasks
import com.jel.taskflow.tasks.domain.use_case.GetSyncState
import com.jel.taskflow.tasks.domain.use_case.GetTask
import com.jel.taskflow.tasks.domain.use_case.GetTasksCount
import com.jel.taskflow.tasks.domain.use_case.InsertTask
import com.jel.taskflow.tasks.domain.use_case.TaskUseCases
import com.jel.taskflow.tasks.domain.use_case.UpdateTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditTaskViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeTaskRepository(Task(id = 1L, title = "Buy milk", content = "2 liters"))

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun editExistingTask() = AddEditTaskViewModel(
        useCases = TaskUseCases(
            getFilteredTasks = GetFilteredTasks(repository),
            getTask = GetTask(repository),
            insertTask = InsertTask(repository),
            updateTask = UpdateTask(repository),
            deleteTask = DeleteTask(repository),
            getTasksCount = GetTasksCount(repository),
            getSyncState = GetSyncState(
                authRepository = LocalOnlyAuthRepository(),
                isSyncRunning = flowOf(false),
                lastSyncFailed = flowOf(false)
            )
        ),
        savedStateHandle = SavedStateHandle(mapOf(TaskScreen.TASK_ID_ARG to 1L))
    )

    private fun String.typed() = TextFieldValue(text = this, selection = TextRange(length))

    @Test
    fun `untouched task is not saved`() = runTest(dispatcher) {
        val viewModel = editExistingTask()

        assertFalse(viewModel.shouldSaveTask())
    }

    @Test
    fun `moving the cursor alone does not count as a change`() = runTest(dispatcher) {
        val viewModel = editExistingTask()

        viewModel.onTitleChanged(TextFieldValue(text = "Buy milk", selection = TextRange(0)))

        assertFalse(viewModel.shouldSaveTask())
    }

    @Test
    fun `edit typed in one burst is saved before the history debounce runs`() = runTest(dispatcher) {
        val viewModel = editExistingTask()

        viewModel.onTitleChanged("Buy milk!".typed())

        assertTrue(viewModel.shouldSaveTask())
        viewModel.saveTask()
        assertEquals("Buy milk!", repository.task.value.title)
        assertFalse(viewModel.shouldSaveTask())
    }

    @Test
    fun `content edit typed in one burst is saved`() = runTest(dispatcher) {
        val viewModel = editExistingTask()

        viewModel.onContentChanged("3 liters".typed())

        assertTrue(viewModel.shouldSaveTask())
    }

    @Test
    fun `undo history still records the burst after the debounce`() = runTest(dispatcher) {
        val viewModel = editExistingTask()
        viewModel.onTextFieldFocusChanged(FocusedTextField.TITLE)

        viewModel.onTitleChanged("Buy milk!".typed())
        assertFalse(viewModel.uiState.value.canUndo)
        advanceTimeBy(501)
        assertTrue(viewModel.uiState.value.canUndo)

        viewModel.revertChanges()
        assertEquals("Buy milk", viewModel.uiState.value.title.text)
        assertTrue(viewModel.uiState.value.canRedo)
    }

    @Test
    fun `restoring task changes discards the pending edit`() = runTest(dispatcher) {
        val viewModel = editExistingTask()
        viewModel.onTitleChanged("Buy milk!".typed())
        advanceTimeBy(501)

        viewModel.reverseChanges()

        assertEquals("Buy milk", viewModel.uiState.value.title.text)
        assertFalse(viewModel.shouldSaveTask())
    }

    private class FakeTaskRepository(initial: Task) : TaskRepository {
        val task = MutableStateFlow(initial)

        override fun getTasks(): Flow<List<Task>> = task.map { listOf(it) }

        override fun getFilteredTasks(
            settings: TaskSettings,
            searchQuery: String,
            requireDueDate: Boolean,
            dueDateStart: Long?,
            dueDateEnd: Long?
        ): Flow<List<Task>> = getTasks()

        override fun getTaskById(id: Long): Flow<Task?> = task

        override suspend fun insertTask(task: Task): Long {
            this.task.value = task
            return task.id ?: 1L
        }

        override suspend fun updateTask(task: Task) {
            this.task.value = task
        }

        override suspend fun deleteTask(task: Task) = Unit

        override fun getTasksCount(): Flow<Int> = flowOf(1)
    }
}
