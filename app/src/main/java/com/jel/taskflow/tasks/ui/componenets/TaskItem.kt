package com.jel.taskflow.tasks.ui.componenets

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jel.taskflow.tasks.model.Task
import com.jel.taskflow.tasks.model.enums.Priority
import com.jel.taskflow.tasks.model.enums.extensions.color
import com.jel.taskflow.tasks.model.enums.extensions.containerColor
import com.jel.taskflow.tasks.model.enums.extensions.imageVector
import com.jel.taskflow.ui.theme.TaskFlowTheme

@Composable
fun TaskItem(
    modifier: Modifier = Modifier,
    task: Task,
    expanded: Boolean = false,
    onExpandedClicked: () -> Unit,
    onDelete: () -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (expanded) 4.dp else 0.dp
    )

    Surface(
        modifier = modifier,
        color =
            if (expanded) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Row(Modifier.padding(start = 15.dp, top = 10.dp)) {
            IndicatorChip(
                label = task.status.getLabel(LocalContext.current),
                imageVector = task.status.imageVector,
                color = task.status.color,
                containerColor = task.status.containerColor
            )
            Spacer(Modifier.padding(horizontal = 2.dp))
            IndicatorChip(
                label = task.priority.getLabel(LocalContext.current),
                color = task.priority.color,
                containerColor = task.priority.containerColor
            )
        }
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onExpandedClicked) {
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
                Surface(
                    modifier = Modifier.padding(top = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            text = task.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 10,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true
                        )

                    }
                }
                Spacer(Modifier.padding(vertical = 5.dp))
                Row(Modifier.fillMaxWidth()) {
                    IconButton(
                        modifier = Modifier
                            .clip(shape = MaterialTheme.shapes.large)
                            .background(color = MaterialTheme.colorScheme.primaryContainer)
                            .size(30.dp),
                        onClick = onDelete,
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

@Preview
@Composable
fun TaskItemPreview() {
    TaskFlowTheme {
        Column {
            Text(
                text = "expended = true",
                color = Color.Gray,
                modifier = Modifier.padding(top = 10.dp, start = 10.dp)
            )
            TaskItem(
                task = Task(title = "title", content = "content", priority = Priority.HIGH),
                expanded = true,
                onExpandedClicked = {},
                onDelete = {}
            )
            Text(
                text = "expended = false",
                color = Color.Gray,
                modifier = Modifier.padding(top = 10.dp, start = 10.dp)
            )
            TaskItem(
                task = Task(title = "title", content = "content"),
                expanded = false,
                onExpandedClicked = {},
                onDelete = {}
            )
            Spacer(modifier = Modifier.padding(top = 10.dp))
        }
    }
}