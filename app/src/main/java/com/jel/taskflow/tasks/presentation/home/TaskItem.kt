package com.jel.taskflow.tasks.presentation.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jel.taskflow.core.components.IndicatorChip
import com.jel.taskflow.core.theme.TaskFlowTheme
import com.jel.taskflow.core.utils.toRelativeDay
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.presentation.components.DeleteConfirmDialog
import com.jel.taskflow.tasks.presentation.extensions.color
import com.jel.taskflow.tasks.presentation.extensions.containerColor
import com.jel.taskflow.tasks.presentation.extensions.imageVector
import com.jel.taskflow.tasks.presentation.extensions.labelRes
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant

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
    onEditClick: () -> Unit,
    onComplete: () -> Unit,
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

    SwipeToToggleCompleteTaskWrapper(
        modifier = modifier,
        status = task.status,
        onComplete = onComplete
    ) {
        Column(
            modifier = Modifier
                .shadow(
                    elevation = elevation,
                    shape = itemShape,
                    clip = false
                )
                .clip(shape = itemShape)
                .background(color = animatedColor)
                .clickable(
                    onClick = onClick
                )
                .animateContentSize(
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
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
                        priority = task.priority,
                        dueDate = task.dueDate
                    )
                    Spacer(Modifier.padding(vertical = 5.dp))
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
                    if (task.content.isNotBlank()) {
                        ContentText(content = task.content)
                    }
                    Spacer(Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Rounded.EditNote,
                                contentDescription = "Edit Task"
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
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
    priority: Priority?,
    dueDate: Instant?
    ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        IndicatorChip(
            label = stringResource(status.labelRes),
            imageVector = status.imageVector,
            color = status.color,
            containerColor = status.containerColor
        )
        priority?.let {
            IndicatorChip(
                label = stringResource(priority.labelRes),
                color = priority.color,
                containerColor = priority.containerColor
            )
        }
        if (dueDate != null) {
            IndicatorChip(
                label = dueDate.toRelativeDay(),
                imageVector = Icons.Rounded.Event,
                color = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

@Composable
fun ContentText(content: String) {
    Column {
        Surface(
            modifier = Modifier.padding(top = 16.dp),
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
    }
}

@Composable
fun SwipeToToggleCompleteTaskWrapper(
    modifier: Modifier = Modifier,
    status: Status,
    onComplete: () -> Unit,
    content: @Composable () -> Unit
) {
    val currentOnComplete by rememberUpdatedState(onComplete)

    val haptic = LocalHapticFeedback.current

    val maxSwipeDp = 80.dp // reveal the target status icon box
    val maxSwipePx = with(LocalDensity.current) { maxSwipeDp.toPx() }

    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val isThresholdCrossed by remember { // toggle when crossing the 90% swipe bounds
        derivedStateOf { offsetX.value >= maxSwipePx * 0.9f }
    }

    LaunchedEffect(isThresholdCrossed) { // trigger vibration
        if (isThresholdCrossed && !hasTriggeredHaptic) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            hasTriggeredHaptic = true
        } else if (!isThresholdCrossed) {
            hasTriggeredHaptic = false
        }
    }

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = { // detect position when leaving the finger
                    coroutineScope.launch {
                        // if crossed the swipe bounds (90%) toggle the status
                        if (isThresholdCrossed) currentOnComplete()
                        // finally reset the item position (animated...)
                        offsetX.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                    }
                },
                onDragCancel = {
                    coroutineScope.launch {
                        offsetX.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                    }
                },
                // calculate the drag position (with limitations...)
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                        val startToEndDragAmount = if (isRtl) -dragAmount else dragAmount
                        // coerceIn(0f, maxSwipePx) - limits the dragging with the revealing box width
                        val newOffset = (offsetX.value + startToEndDragAmount).coerceIn(0f, maxSwipePx)
                        offsetX.snapTo(newOffset)
                    }
                }
            )
        }
    ) {
        SwipeToCompleteBackground(
            modifier = Modifier.matchParentSize(),
            status = status,
            offsetX = offsetX.value,
            maxSwipePx = maxSwipePx,
            isThresholdCrossed = isThresholdCrossed
        )

        // the draggable content (TaskItem)
        Box(
            modifier = Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) }
        ) { content() }
    }
}

@Composable
fun SwipeToCompleteBackground(
    modifier: Modifier = Modifier,
    status: Status,
    offsetX: Float,
    maxSwipePx: Float,
    isThresholdCrossed: Boolean
) {
    val targetStatus = if (status == Status.COMPLETED) Status.TODO else Status.COMPLETED

    val backgroundColor by animateColorAsState(
        targetValue =
            if (isThresholdCrossed) targetStatus.containerColor
            else status.containerColor,
        label = "swipeBackgroundColor",
        animationSpec = tween(150)
    )

    // calculate drag progress (animates the icon size)
    val progress = (offsetX / maxSwipePx).coerceIn(0f, 1f)

    val targetScale = when {
        isThresholdCrossed -> 1f
        else -> 0.5f + (progress * 0.5f)
    }

    val iconScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = if (isThresholdCrossed) Spring.DampingRatioMediumBouncy else Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "swipeIconScale"
    )

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(end = 5.dp)
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(with(LocalDensity.current) { offsetX.toDp() - 5.dp }) // expanding with the drag
                .background(
                    color = backgroundColor,
                    shape = MaterialTheme.shapes.medium
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (progress > 0.1f) {
                Icon(
                    imageVector = targetStatus.imageVector,
                    tint = targetStatus.color,
                    contentDescription = "Toggle Complete Task",
                    modifier = Modifier.scale(iconScale)
                )
            }
        }
    }
}

@Preview(name = "expended = true")
@Composable
fun TaskItemPreview() {
    TaskFlowTheme {
        TaskItem(
            task = Task(title = "title", content = "content", priority = Priority.HIGH, dueDate = Clock.System.now()),
            expanded = true,
            onExpandedClicked = {},
            onClick = {},
            onEditClick = {},
            onComplete = {},
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
            onEditClick = {},
            onComplete = {},
            onDelete = {}
        )
    }
}