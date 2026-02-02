package com.jel.taskflow.tasks.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.navigation.NavController
import com.jel.taskflow.R
import com.jel.taskflow.tasks.model.AddEditTaskViewModel
import com.jel.taskflow.tasks.model.enums.extensions.color
import com.jel.taskflow.tasks.model.enums.extensions.containerColor
import com.jel.taskflow.tasks.model.enums.extensions.labelRes
import com.jel.taskflow.tasks.ui.componenets.StatusGroupButtons
import com.jel.taskflow.tasks.ui.utils.Screen
import com.jel.taskflow.utils.toRelativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: AddEditTaskViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState = viewModel.uiState
    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        DeleteDialog(
            taskTitle = uiState.title,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                viewModel.currentTaskId.let { currentTaskId ->
                    showDeleteConfirmDialog = false
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("deletedTaskId", currentTaskId)
                    navController.popBackStack()
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AssistChip(
                                onClick = {},
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = uiState.priority.containerColor
                                ),
                                label = {
                                    Text(
                                        text = stringResource(uiState.priority.labelRes),
                                        color = uiState.priority.color
                                    )
                                },
                            )
                            IconButton(
                                onClick = { showDeleteConfirmDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = "Delete Task"
                                )
                            }
                        }
                        Text(
                            text = uiState.title,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge
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
                        Screen.AddEditTaskScreen.route + "?taskId=${viewModel.currentTaskId}"
                    )
                },

                ) {
                Icon(
                    imageVector = Icons.Rounded.EditNote,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Edit Task"
                )
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            StatusGroupButtons(
                status = uiState.status,
                onStatusChanged = { newStatus ->
                    viewModel.onStatusChanged(newStatus)
                    viewModel.saveTask()
                }
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
                            text = uiState.content,
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
                                uiState.createdDate.toRelativeTime()
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = stringResource(
                                R.string.updated_at,
                                uiState.changedDate.toRelativeTime()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteDialog(
    taskTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Task confirmation") },
        icon = {
            Icon(
                imageVector = Icons.Rounded.Delete,
                tint = MaterialTheme.colorScheme.error,
                contentDescription = "Delete Task Dialog"
            )
        },
        text = {
            Text(
                text = "You sure you want to delete $taskTitle?"
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Cancel")
            }
        }
    )
}