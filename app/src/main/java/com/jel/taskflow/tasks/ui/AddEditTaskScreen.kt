package com.jel.taskflow.tasks.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jel.taskflow.R
import com.jel.taskflow.tasks.model.AddEditTaskViewModel
import com.jel.taskflow.tasks.ui.componenets.TaskTextFieldsAndStatus
import com.jel.taskflow.ui.theme.TaskFlowTheme
import com.jel.taskflow.utils.flatColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(viewModel: AddEditTaskViewModel = hiltViewModel()) {
    val state = viewModel.uiState

    val snackBarHostState = remember { SnackbarHostState() }

    DisposableEffect(key1 = state.currentTaskChanged) {
        onDispose {
            if (state.currentTaskChanged) viewModel.saveTask()
        }
    }

    TaskFlowTheme {
        Scaffold(
            modifier = Modifier,
            snackbarHost = { SnackbarHost(snackBarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text =
                            if (state.editMode) stringResource(R.string.edit_task)
                            else stringResource(R.string.add_task)
                        )
                    }, colors = TopAppBarDefaults.flatColors()
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                if (state.currentTaskChanged) {
                    FloatingActionButton(
                        modifier = Modifier.padding(bottom = 20.dp),
                        onClick = { viewModel.reverseChanges() },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Restore,
                            tint = MaterialTheme.colorScheme.tertiary,
                            contentDescription = "restore task changes"
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding(),
                color = MaterialTheme.colorScheme.background
            ) {
                TaskTextFieldsAndStatus(
                    modifier = Modifier.padding(10.dp),
                    state = state,
                    onTitleChanged = { newTitle ->
                        viewModel.onTitleChanged(newTitle)
                    },
                    onContentChanged = { newContent ->
                        viewModel.onContentChanged(newContent)
                    },
                    onStatusChanged = { newStatus ->
                        viewModel.onStatusChanged(newStatus)
                    },
                    onPriorityChanged = { newPriority ->
                        viewModel.onPriorityChanged(newPriority)
                    },
                    onUndo = { viewModel.revertChanges() },
                    onRedo = { viewModel.revertChanges(forwards = true) })
            }
        }
    }
}


//@Preview
//@Composable
//fun AddTaskScreenPreview() {
//    TaskFlowTheme {
//        AddTaskScreen()
//    }
//}