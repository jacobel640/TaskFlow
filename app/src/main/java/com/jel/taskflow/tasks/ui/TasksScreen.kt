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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TasksScreen(viewModel: TasksViewModel = hiltViewModel(), navController: NavController) {

    val tasks = viewModel.tasks.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

//    LaunchedEffect(key1 = viewModel.errorEvent) {
//        viewModel.errorEvent.collect { message ->
//            snackBarHostState.showSnackbar(message)
//        }
//    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                tonalElevation = 5.dp,
                actions = {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        label = { Text(text = "Main List") },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.List,
                                contentDescription = null
                            )
                        }
                    )
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEditTaskScreen.route) },
                modifier = Modifier.padding(10.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add, // Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Add a Task"
                )
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
                            route = Screen.AddEditTaskScreen.route + "?taskId=${taskId}"
                        )
                    }
                },
                deleteTaskClick = { task ->
                    task.id?.let {
                        viewModel.deleteTask(it)
                    }
                }
            )
        }
    }
}

@Composable
fun ItemsList(
    modifier: Modifier = Modifier,
    tasks: State<List<Task>>,
    onItemClick: (Long?) -> Unit,
    deleteTaskClick: (Task) -> Unit,
) {
    var expandedItem by remember { mutableStateOf<Task?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val state = rememberLazyListState()
    val showScrollToTopButton by remember {
        derivedStateOf {
            state.firstVisibleItemIndex > 0
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(items = tasks.value) { _, task ->
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
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        state.animateScrollToItem(0)
                    }
                },
                modifier = Modifier.padding(10.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
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