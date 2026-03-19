package com.jel.taskflow.tasks.presentation.single_task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jel.taskflow.R
import com.jel.taskflow.core.utils.Screen
import com.jel.taskflow.core.utils.toRelativeDay
import com.jel.taskflow.core.utils.toRelativeTime
import com.jel.taskflow.tasks.presentation.add_edit_task.components.StatusGroupButtons
import com.jel.taskflow.tasks.presentation.components.DeleteConfirmDialog
import com.jel.taskflow.tasks.presentation.extensions.color
import com.jel.taskflow.tasks.presentation.extensions.containerColor
import com.jel.taskflow.tasks.presentation.extensions.labelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleTaskScreen(
    viewModel: SingleTaskViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState is SingleTaskUiState.Success) {
        val currentTask = (uiState as SingleTaskUiState.Success).task
        var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }

        if (showDeleteConfirmDialog) {
            DeleteConfirmDialog(
                taskTitle = currentTask.title,
                onDismiss = { showDeleteConfirmDialog = false },
                onDeleteConfirm = {
                    showDeleteConfirmDialog = false
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("deletedTaskId", viewModel.taskId)
                    navController.popBackStack()
                }
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            currentTask.priority?.let {
                                AssistChip(
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    onClick = {},
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = currentTask.priority.containerColor
                                    ),
                                    label = {
                                        Text(
                                            text = stringResource(currentTask.priority.labelRes),
                                            color = currentTask.priority.color
                                        )
                                    },
                                )
                            }
                            IconButton(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                onClick = { showDeleteConfirmDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = stringResource(R.string.delete_task)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = 10.dp),
                    onClick = {
                        navController.navigate(
                            Screen.AddEditTaskScreen.withArgs(viewModel.taskId)
                        )
                    },

                    ) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = stringResource(R.string.edit_task)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = currentTask.title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
                currentTask.dueDate?.let { dueDate ->
                    Text(
                        text = stringResource(R.string.due_date),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.offset(x = 10.dp, y = 5.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Event,
                            contentDescription = stringResource(R.string.due_date),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = dueDate.toRelativeDay(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                StatusGroupButtons(
                    status = currentTask.status,
                    onStatusChanged = viewModel::onStatusChanged
                )

                SelectionContainer {
                    Box {
                        Surface(
                            modifier = Modifier.padding(vertical = 20.dp, horizontal = 5.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 10.dp,
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 5.dp)
                                    .padding(top = 25.dp)
                                    .verticalScroll(rememberScrollState()),
                                text = currentTask.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            modifier = Modifier
                                .padding(
                                    start = 10.dp, top = 25.dp
                                )
                                .align(Alignment.TopStart),
                            text = stringResource(R.string.task_content),
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.created_at,
                                    currentTask.createdDate.toRelativeTime()
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = stringResource(
                                    R.string.updated_at,
                                    currentTask.changedDate.toRelativeTime()
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            }
        }
    }
}