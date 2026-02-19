package com.jel.taskflow.tasks.presentation.add_edit_task.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jel.taskflow.R
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.presentation.extensions.color
import com.jel.taskflow.tasks.presentation.extensions.containerColor
import com.jel.taskflow.tasks.presentation.extensions.labelRes

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
                        text = stringResource(priority.labelRes),
                        color = priority.color
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = priority.containerColor,
                    labelColor = priority.color,
                    trailingIconContentColor = priority.color
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
                                text = stringResource(entry.labelRes),
                                color = entry.color
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