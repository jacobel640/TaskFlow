package com.jel.taskflow.tasks.presentation.add_edit_task.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jel.taskflow.R
import com.jel.taskflow.core.components.AnimatedSelector
import com.jel.taskflow.core.components.rememberDropdownSelectionState
import com.jel.taskflow.core.theme.TaskFlowTheme
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.presentation.extensions.color
import com.jel.taskflow.tasks.presentation.extensions.containerColor
import com.jel.taskflow.tasks.presentation.extensions.labelRes
import com.jel.taskflow.tasks.presentation.extensions.options
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PriorityDropDown(
    modifier: Modifier = Modifier,
    priority: Priority?,
    onPriorityChanged: (Priority?) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val animatedSelectedItemContainerColor by animateColorAsState(
        targetValue = priority.containerColor
    )

    Box(modifier = modifier.width(IntrinsicSize.Max).animateContentSize()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }) {
            Column {
                Text(
                    text = stringResource(R.string.priority),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.offset(x = 10.dp, y = (5).dp)
                )
                AssistChip(
                    modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    label = {
                        Text(
                            text = stringResource(priority.labelRes),
                            color = priority?.color ?: MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        TrailingIcon(
                            expanded = expanded,
                            priority = priority
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = animatedSelectedItemContainerColor,
                        labelColor = priority.color,
                        trailingIconContentColor = priority.color
                    ),
                    onClick = {}
                )
            }

            val coroutineScope = rememberCoroutineScope()
            val priorityOptions = Priority.options()

            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = MaterialTheme.shapes.medium)) {
                val selectionState = rememberDropdownSelectionState(priority, priorityOptions)

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                        Box(modifier = Modifier.width(IntrinsicSize.Max)) {
                            AnimatedSelector(
                                state = selectionState,
                                color = animatedSelectedItemContainerColor
                            )

                            Column {
                                priorityOptions.forEachIndexed { index, item ->
                                    PriorityDropDownItem(
                                        modifier = Modifier
                                            .padding(horizontal = 5.dp)
                                            .onGloballyPositioned {
                                                selectionState.onItemLayout(
                                                    index,
                                                    it
                                                )
                                            },
                                        priority = item,
                                        onClick = {
                                            onPriorityChanged(item)
                                            coroutineScope.launch {
                                                delay(700)
                                                expanded = false
                                            }
                                        },
                                        isSelected = item == priority
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityDropDownItem(
    modifier: Modifier = Modifier,
    priority: Priority? = null,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    DropdownMenuItem(
        modifier = modifier,
        text = {
            Text(
                text = stringResource(priority.labelRes),
                color = priority.color
            )
        },
        onClick = onClick,
        trailingIcon = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selected Priority",
                    tint = priority.color
                )
            }
        }
    )
}

@Composable
fun TrailingIcon(
    modifier: Modifier = Modifier,
    priority: Priority?,
    expanded: Boolean
) {
    Icon(
        imageVector = Icons.Rounded.ArrowDropDown,
        contentDescription = null,
        tint = priority?.color ?: MaterialTheme.colorScheme.primary,
        modifier = modifier.rotate(if (expanded) 180f else 0f)
    )
}

@Preview
@Composable
fun PriorityDropDownPreview() {
    TaskFlowTheme {
        Column {
            Priority.options().forEach { priority ->
                PriorityDropDown(
                    priority = priority,
                    onPriorityChanged = { }
                )
            }
        }
    }
}

@Preview
@Composable
fun PriorityDropDownItemPreview() {
    TaskFlowTheme {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Priority.options().forEach { priority ->
                PriorityDropDownItem(
                    priority = priority,
                    onClick = { },
                    isSelected = true
                )
            }
        }
    }
}