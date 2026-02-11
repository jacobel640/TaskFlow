package com.jel.taskflow.tasks.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jel.taskflow.R
import com.jel.taskflow.tasks.model.Task
import com.jel.taskflow.tasks.model.TasksViewModel
import com.jel.taskflow.tasks.ui.componenets.TaskItem
import com.jel.taskflow.tasks.ui.utils.Screen
import com.jel.taskflow.ui.theme.TaskFlowTheme
import com.jel.taskflow.utils.flatColors
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TasksListScreen(viewModel: TasksViewModel = hiltViewModel(), navController: NavController) {

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    var firstListItemShown by rememberSaveable { mutableStateOf(false) }

    val currentBackStackEntry = navController.currentBackStackEntry
    LaunchedEffect(currentBackStackEntry) {
        val savedStateHandle = currentBackStackEntry?.savedStateHandle
        savedStateHandle?.get<Long>("deletedTaskId")?.let { taskId ->
            viewModel.deleteTask(taskId)
            savedStateHandle.remove<Long>("deletedTaskId")
        }
    }

    val deleteSuccessMessage = stringResource(R.string.task_deleted_successfully_message)
    val undoActionText = stringResource(R.string.undo_action)
    LaunchedEffect(key1 = viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is UiEvent.ShowUndoDeleteSnackbar -> {
                    val result = snackBarHostState.showSnackbar(
                        message = deleteSuccessMessage,
                        actionLabel = undoActionText,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreDeletedTask()
                    }
                }
                is UiEvent.ShowDeleteFailedSnackbar -> {
                    snackBarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                colors = TopAppBarDefaults.flatColors()
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditTaskScreen.route) },
                modifier = Modifier.padding(10.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal =
                            if (firstListItemShown) 0.dp
                            else 10.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add, // Icons.Rounded.KeyboardArrowUp,
                        contentDescription = stringResource(R.string.add_task)
                    )
                    AnimatedVisibility(visible = !firstListItemShown) {
                        Text(
                            modifier = Modifier.padding(start = 10.dp, end = 5.dp),
                            text = stringResource(R.string.add_task)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            ItemsList(
                modifier = Modifier,
                tasks = tasks,
                onItemClick = { taskId ->
                    taskId?.let {
                        navController.navigate(
                            route = Screen.SingleTaskScreen.withIdArg(taskId)
                        )
                    }
                },
                deleteTaskClick = { task ->
                    task.id?.let {
                        viewModel.deleteTask(it)
                    }
                },
                onListScroll = {
                    firstListItemShown = it
                }
            )
        }
    }
}

@Composable
fun ItemsList(
    modifier: Modifier = Modifier,
    tasks: List<Task>,
    onItemClick: (Long?) -> Unit,
    deleteTaskClick: (Task) -> Unit,
    onListScroll: (Boolean) -> Unit
) {
    var expandedItem by remember { mutableStateOf<Task?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val state = rememberLazyListState()
    val showScrollToTopButton by remember {
        derivedStateOf {
            state.firstVisibleItemIndex > 0
        }
    }

    LaunchedEffect(showScrollToTopButton) {
        onListScroll(showScrollToTopButton)
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = tasks,
                key = { task -> task.id ?: 0L },
                contentType = { "task_item" }
            ) { task ->
                TaskItem(
                    modifier = Modifier.clickable { onItemClick(task.id) },
                    task = task,
                    expanded = (task == expandedItem),
                    onExpandedClicked = {
                        println("expanded toggled at task: ${task.id}, ${task.title}")
                        expandedItem = if (expandedItem == task) null else task
                    },
                    onDelete = { deleteTaskClick(task) }
                )
            }
        }
        AnimatedVisibility(
            visible = showScrollToTopButton,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            IconButton(
                modifier = Modifier.padding(10.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                onClick = {
                    coroutineScope.launch {
                        state.animateScrollToItem(0)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Return to top"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemsListPreview() {
    TaskFlowTheme {
//        ItemsList(
//            tasks =
//        )
    }
}