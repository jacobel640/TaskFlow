package com.jel.taskflow.tasks.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jel.taskflow.core.components.IndicatorChip
import com.jel.taskflow.core.theme.TaskFlowTheme
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.presentation.components.DeleteConfirmDialog
import com.jel.taskflow.tasks.presentation.extensions.color
import com.jel.taskflow.tasks.presentation.extensions.containerColor
import com.jel.taskflow.tasks.presentation.extensions.imageVector
import com.jel.taskflow.tasks.presentation.extensions.labelRes

val ColorScheme.collapsedColor: Color
    @Composable
    get() = primaryContainer.copy(alpha = 0.5f).compositeOver(background)

@Composable
fun TaskItem(
    modifier: Modifier = Modifier,
    task: Task,
    expanded: Boolean = false,
    onExpandedClicked: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    itemShape: CornerBasedShape = MaterialTheme.shapes.medium
) {
    val elevation by animateDpAsState(
        targetValue = if (expanded) 4.dp else 0.dp
    )

    val collapsedColor = MaterialTheme.colorScheme.collapsedColor
    val expendedColor = MaterialTheme.colorScheme.primaryContainer

    val animatedColor by animateColorAsState(
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        targetValue = if (expanded) expendedColor else collapsedColor,
        label = "cardColor"
    )
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            taskTitle = task.title,
            onDismiss = { showDeleteConfirmDialog = false },
            onDeleteConfirm = onDelete
        )
    }

    Surface(
        modifier = modifier
            .clip(shape = itemShape)
            .clickable(
                onClick = onClick
            )
            .animateContentSize(
                spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        color = animatedColor,
        shape = itemShape,
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                ) {
                    TopIndicationChips(
                        status = task.status,
                        priority = task.priority
                    )
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = onExpandedClicked
                ) {
                    if (expanded) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = "Collapse"
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Expand"
                        )
                    }
                }
            }
            if (expanded) {
                Column {
                    ContentText(content = task.content)
                    Row(Modifier.fillMaxWidth()) {
                        IconButton(
                            modifier = Modifier
                                .clip(shape = MaterialTheme.shapes.large)
                                .background(color = MaterialTheme.colorScheme.primaryContainer)
                                .size(30.dp),
                            onClick = { showDeleteConfirmDialog = true },
                            shape = MaterialTheme.shapes.large,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = "Delete Task"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopIndicationChips(
    status: Status,
    priority: Priority) {
    Row {
        IndicatorChip(
            label = stringResource(status.labelRes),
            imageVector = status.imageVector,
            color = status.color,
            containerColor = status.containerColor
        )
        Spacer(Modifier.padding(horizontal = 2.dp))
        IndicatorChip(
            label = stringResource(priority.labelRes),
            color = priority.color,
            containerColor = priority.containerColor
        )
    }
}

@Composable
fun ContentText(content: String) {
    Column {
        Surface(
            modifier = Modifier.padding(top = 8.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )
        }
        Spacer(Modifier.padding(vertical = 5.dp))
    }
}

@Preview(name = "expended = true")
@Composable
fun TaskItemPreview() {
    TaskFlowTheme {
        TaskItem(
            task = Task(title = "title", content = "content", priority = Priority.HIGH),
            expanded = true,
            onExpandedClicked = {},
            onClick = {},
            onDelete = {})
    }
}

@Preview(name = "expended = false")
@Composable
fun TaskItemNotExpendedPreview() {
    TaskFlowTheme {
        TaskItem(
            task = Task(title = "title", content = "content"),
            expanded = false,
            onExpandedClicked = {},
            onClick = {},
            onDelete = {}
        )
    }
}