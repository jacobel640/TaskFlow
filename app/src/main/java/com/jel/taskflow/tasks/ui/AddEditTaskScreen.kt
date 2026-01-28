package com.jel.taskflow.tasks.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jel.taskflow.R
import com.jel.taskflow.tasks.model.AddEditTaskViewModel
import com.jel.taskflow.tasks.model.Priority
import com.jel.taskflow.tasks.model.Status
import com.jel.taskflow.tasks.ui.componenets.priorityColor
import com.jel.taskflow.tasks.ui.componenets.priorityContainerColor
import com.jel.taskflow.ui.theme.TaskFlowTheme
import com.jel.taskflow.utils.flatColors
import com.jel.taskflow.utils.toRelativeTime

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
        Scaffold(modifier = Modifier, snackbarHost = { SnackbarHost(snackBarHostState) }, topBar = {
            TopAppBar(
                title = {
                    Text(
                        text =
                            if (state.editMode) stringResource(R.string.edit_task)
                            else stringResource(R.string.add_task)
                    )
                }, colors = TopAppBarDefaults.flatColors()
            )
        }, floatingActionButtonPosition = FabPosition.End, floatingActionButton = {
            if (state.currentTaskChanged) {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = 16.dp),
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
        }) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaskTextFieldsAndStatus(
    modifier: Modifier = Modifier,
    state: AddEditTaskUiState,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onStatusChanged: (Status) -> Unit,
    onPriorityChanged: (Priority) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit
) {

    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text(text = stringResource(R.string.title)) },
                value = state.title,
                onValueChange = onTitleChanged
            )
            PriorityDropDown(
                modifier = Modifier.padding(horizontal = 15.dp),
                priority = state.priority,
                onPriorityChanged = onPriorityChanged
            )
        }
        StatusGroupButtons(
            status = state.status, onStatusChanged = onStatusChanged
        )
        Spacer(modifier = Modifier.padding(vertical = 5.dp))
        Box(
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                modifier = Modifier.fillMaxSize(),
                label = { Text(text = stringResource(R.string.task_content)) },
                value = state.content,
                onValueChange = onContentChanged,
            )
            UndoRedoControl(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 5.dp),
                canUndo = state.canUndo,
                canRedo = state.canRedo,
                onUndo = onUndo,
                onRedo = onRedo
            )
        }
        Row {
            Text(
                text = stringResource(R.string.created_at, state.createdDate.toRelativeTime()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.updated_at, state.changedDate.toRelativeTime()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PriorityDropDown(
    modifier: Modifier = Modifier, priority: Priority, onPriorityChanged: (Priority) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.width(IntrinsicSize.Max)) {
        ExposedDropdownMenuBox(
            modifier = modifier.padding(top = 10.dp),
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }) {
            Text(
                text = stringResource(R.string.priority),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.offset(x = 10.dp, y = (-10).dp)
            )
            AssistChip(
                label = {
                    Text(
                        text = priority.getLabel(LocalContext.current),
                        color = priorityColor(priority)
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = priorityContainerColor(priority),
                    labelColor = priorityColor(priority),
                    trailingIconContentColor = priorityColor(priority)
                ),
                modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                onClick = {})

            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(extraSmall = MaterialTheme.shapes.medium)
            ) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                        .width(IntrinsicSize.Max),
                ) {

                    Priority.entries.forEach { entry ->
                        DropdownMenuItem(text = {
                            Text(
                                text = entry.getLabel(LocalContext.current),
                                color = priorityColor(entry)
                            )
                        }, onClick = {
                            onPriorityChanged(entry)
                            expanded = false
                        }, trailingIcon = {
                            if (entry == priority) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected Priority",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatusGroupButtons(
    status: Status, onStatusChanged: (Status) -> Unit
) {
    Text(
        modifier = Modifier
            .padding(horizontal = 15.dp)
            .padding(top = 10.dp),
        text = "Task progress",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.outline,
    )
    FlowRow(
        Modifier
            .padding(horizontal = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {

        Status.entries.forEachIndexed { index, entry ->
            val selected = entry == status

            ToggleButton(
                modifier = Modifier.semantics { role = Role.RadioButton },
                checked = selected,
                onCheckedChange = { onStatusChanged(entry) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    Status.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
            ) {
                AnimatedVisibility(selected) {
                    Row {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "${entry.getLabel(LocalContext.current)} selected."
                        )
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    }
                }
                Text(text = entry.getLabel(LocalContext.current))
            }
        }
    }
//        SingleChoiceSegmentedButtonRow(
//            modifier = Modifier.fillMaxWidth(),
//            space = (-4).dp
//        ) {
//            Status.entries.forEachIndexed { index, entry ->
//                val checked = entry == status
//                SegmentedButton(
//                    label = { Text(text = entry.name.replace("_", " ")) },
//                    selected = checked,
//                    onClick = { onStatusChanged(entry) },
//                    colors = SegmentedButtonDefaults.colors(
//                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainer,
//                        inactiveBorderColor = MaterialTheme.colorScheme.surfaceContainer,
//                        activeBorderColor = MaterialTheme.colorScheme.surfaceContainer
//                    ),
//                    shape = SegmentedButtonDefaults.itemShape(
//                        index = index,
//                        count = Status.entries.size,
//                        baseShape = RoundedCornerShape(MaterialTheme.shapes.large.topStart)
//                    )
//                )
//            }
//        }
}

@Composable
fun UndoRedoControl(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onUndo, enabled = canUndo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) MaterialTheme.colorScheme.onSurface else Color.Gray.copy(
                        alpha = 0.3f
                    )
                )
            }

            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            IconButton(
                onClick = onRedo, enabled = canRedo
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) MaterialTheme.colorScheme.onSurface else Color.Gray.copy(
                        alpha = 0.3f
                    )
                )
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

@Preview
@Composable
fun TaskTextFieldsAndStatusPreview() {
    TaskFlowTheme {
        TaskTextFieldsAndStatus(
            state = AddEditTaskUiState(),
            onTitleChanged = {},
            onContentChanged = {},
            onStatusChanged = {},
            onPriorityChanged = {},
            onUndo = {},
            onRedo = {})
    }
}