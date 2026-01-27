package com.jel.taskflow.tasks.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonGroup
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.jel.taskflow.tasks.model.AddEditTaskViewModel
import com.jel.taskflow.tasks.model.Priority
import com.jel.taskflow.tasks.model.Status
import com.jel.taskflow.tasks.ui.componenets.priorityColor
import com.jel.taskflow.tasks.ui.componenets.priorityContainerColor
import com.jel.taskflow.ui.theme.TaskFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: AddEditTaskViewModel = hiltViewModel(),
    navController: NavController,
) {
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
                        Text(text = "Add Task")
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                if (state.currentTaskChanged) {
                    FloatingActionButton(
                        modifier = Modifier.padding(bottom = 10.dp),
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
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                TaskTextFieldsAndStatus(
                    modifier = Modifier.padding(10.dp),
                    title = state.title,
                    content = state.content,
                    status = state.status,
                    priority = state.priority,
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
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaskTextFieldsAndStatus(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    status: Status,
    priority: Priority,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onStatusChanged: (Status) -> Unit,
    onPriorityChanged: (Priority) -> Unit
) {

    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text(text = "Title") },
                value = title,
                onValueChange = onTitleChanged
            )
            PriorityDropDown(
                modifier = Modifier.padding(horizontal = 15.dp),
                priority = priority,
                onPriorityChanged = onPriorityChanged
            )
        }
        StatusGroupButtons(
            status = status,
            onStatusChanged = onStatusChanged
        )
        Spacer(modifier = Modifier.padding(vertical = 5.dp))
        Box(
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                modifier = Modifier.fillMaxSize(),
                label = { Text(text = "Task content") },
                value = content,
                onValueChange = onContentChanged
            )
            ButtonGroup(

            ) {

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PriorityDropDown(
    modifier: Modifier = Modifier,
    priority: Priority,
    onPriorityChanged: (Priority) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.width(IntrinsicSize.Max)) {
        ExposedDropdownMenuBox(
            modifier = modifier.padding(top = 10.dp),
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Text(
                text = "Priority",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.offset(x = 10.dp, y = (-10).dp)
            )
            AssistChip(
                label = { Text(priority.name, color = priorityColor(priority)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = priorityContainerColor(priority),
                    labelColor = priorityColor(priority),
                    trailingIconContentColor = priorityColor(priority)
                ),
                modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                onClick = {}
            )

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
                        val priorityName = when (entry) {
                            Priority.LOW -> "Low"
                            Priority.MEDIUM -> "Medium"
                            Priority.HIGH -> "High"
                        }
                        DropdownMenuItem(
                            text = {
                                Text(text = priorityName, color = priorityColor(entry))
                            },
                            onClick = {
                                onPriorityChanged(entry)
                                expanded = false
                            },
                            trailingIcon = {
                                if (entry == priority) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected Priority",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatusGroupButtons(
    status: Status,
    onStatusChanged: (Status) -> Unit
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
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        Status.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    }
            ) {
                AnimatedVisibility(selected) {
                    Row {
                        Icon(Icons.Rounded.Check, "${entry.getLabel()} selected.")
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    }
                }
                Text(text = entry.getLabel())
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
            title = "Title",
            content = "Text written in the Content section saved along the title in the database.",
            status = Status.TODO,
            priority = Priority.MEDIUM,
            onTitleChanged = {},
            onContentChanged = {},
            onStatusChanged = {},
            onPriorityChanged = {}
        )
    }
}