package com.jel.taskflow.tasks.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jel.taskflow.R
import com.jel.taskflow.core.theme.TaskFlowTheme
import com.jel.taskflow.core.utils.Screen
import com.jel.taskflow.core.utils.flatColors
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.presentation.home.components.NotificationTimeDialog
import com.jel.taskflow.tasks.presentation.home.components.SearchAndFilterSection
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel(), navController: NavController) {

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val firstListItemShown by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }
    val resources = LocalResources.current

    var showTimePickerDialog by remember { mutableStateOf(false) }

    if (showTimePickerDialog) {
        NotificationTimeDialog(
            onDismiss = { showTimePickerDialog = false },
            onConfirm = { hour, minute ->
                coroutineScope.launch {
                    snackBarHostState.showSnackbar(
                        message = resources.getString(
                            R.string.notification_saved_message,
                            hour,
                            minute
                        ),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

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
            when (event) {
                is HomeUiEvent.ShowUndoDeleteSnackbar -> {
                    val result = snackBarHostState.showSnackbar(
                        message = deleteSuccessMessage,
                        actionLabel = undoActionText,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreDeletedTask()
                    }
                }

                is HomeUiEvent.ShowDeleteFailedSnackbar -> {
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
                actions = {
                    IconButton(onClick = { showTimePickerDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = stringResource(R.string.set_daily_notification),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.CalendarScreen.route) }) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = stringResource(R.string.tasks_calendar),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.flatColors()
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            Column(
                modifier = Modifier.padding(bottom = 10.dp, end = 10.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                AnimatedVisibility(
                    visible = firstListItemShown,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    OutlinedIconButton(
                        modifier = Modifier.padding(end = 4.dp),
                        shape = CircleShape,
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        ),
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.return_to_top)
                        )
                    }
                }
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddEditTaskScreen.route) },
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
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val isTasksEmpty by remember(uiState.tasksCount) {
            derivedStateOf { uiState.tasksCount <= 0 }
        }

        Surface(
            modifier = Modifier.padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (isTasksEmpty) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.no_tasks_greeting))
                    Button(onClick = { navController.navigate(Screen.AddEditTaskScreen.route) }) {
                        Icon(
                            imageVector = Icons.Rounded.AddTask,
                            contentDescription = stringResource(R.string.create_new_task)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                        Text(text = stringResource(R.string.create_new_task))
                    }
                }
            } else {
                TasksList(
                    modifier = Modifier.fillMaxSize(),
                    listState = listState,
                    uiState = uiState,
                    onUiAction = viewModel::onUiAction,
                    onNavigateToTaskDetails = { task ->
                        navController.navigate(
                            route = Screen.SingleTaskScreen(task)
                        )
                    },
                    deleteTaskClick = { taskId ->
                        taskId?.let {
                            viewModel.deleteTask(it)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TasksList(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    uiState: HomeUiState,
    onUiAction: (HomeUiActions) -> Unit,
    onNavigateToTaskDetails: (Task) -> Unit,
    deleteTaskClick: (Long?) -> Unit
) {
    var expandedItem by remember { mutableStateOf<Task?>(null) }
    var todayExpanded by remember { mutableStateOf(true) }
    var otherExpanded by remember { mutableStateOf(true) }
    val showClearFiltersMessage by remember(
        uiState.settings,
        uiState.searchQuery,
        uiState.allTasks
    ) {
        derivedStateOf { uiState.isFilterApplied() && uiState.allTasks.isEmpty() }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .padding(horizontal = 8.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "header_section") {
            Column(modifier = Modifier.fillMaxWidth()) {
                SearchAndFilterSection(
                    uiState = uiState,
                    onUiAction = onUiAction
                )
                AnimatedVisibility(uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillParentMaxHeight()
                            .fillParentMaxWidth()
                            .background(color = MaterialTheme.colorScheme.background),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LinearProgressIndicator()
                    }
                }
                AnimatedVisibility(showClearFiltersMessage) {
                    Column(
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.background)
                            .fillParentMaxHeight()
                            .fillParentMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = stringResource(R.string.empty_tasks_filters_results))
                        Button(
                            onClick = { onUiAction(HomeUiActions.OnClearFilters) }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = stringResource(R.string.clear_filters)
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
                            Text(text = stringResource(R.string.clear_filters))
                        }
                    }
                }
            }
        }

        if (uiState.todayTasks.isNotEmpty()) {
            stickyHeader(key = "today_header") {
                SectionHeader(
                    text = stringResource(R.string.today),
                    isExpanded = todayExpanded,
                    onExpandClick = { todayExpanded = !todayExpanded}
                )
            }
            if (todayExpanded) {
                items(
                    items = uiState.todayTasks,
                    key = { task -> "today_${task.id}" },
                    contentType = { "task_item" }
                ) { task ->
                    TaskItem(
                        modifier = Modifier.animateItem(),
                        task = task,
                        expanded = (task == expandedItem),
                        onExpandedClicked = {
                            expandedItem = if (expandedItem == task) null else task
                        },
                        onClick = { onNavigateToTaskDetails(task) },
                        onDelete = { deleteTaskClick(task.id) }
                    )
                }
            }
        }

        if (uiState.otherTasks.isNotEmpty()) {
            if (uiState.todayTasks.isNotEmpty()) {
                stickyHeader(key = "other_header") {
                    SectionHeader(
                        text = stringResource(R.string.other_tasks),
                        isExpanded = otherExpanded,
                        onExpandClick = { otherExpanded = !otherExpanded}
                    )
                }
            }
            if (otherExpanded) {
                items(
                    items = uiState.otherTasks,
                    key = { task -> "other_${task.id}" },
                    contentType = { "task_item" }
                ) { task ->
                    TaskItem(
                        modifier = Modifier.animateItem(),
                        task = task,
                        expanded = (task == expandedItem),
                        onExpandedClicked = {
                            expandedItem = if (expandedItem == task) null else task
                        },
                        onClick = { onNavigateToTaskDetails(task) },
                        onDelete = { deleteTaskClick(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String, isExpanded: Boolean, onExpandClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
            )
            IconButton(onClick = onExpandClick) {
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = stringResource(R.string.toggle_expanded)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TasksListPreview() {
    TaskFlowTheme {
//        ItemsList(
//            tasks =
//        )
    }
}