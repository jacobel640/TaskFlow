package com.jel.taskflow.tasks.ui.componenets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.jel.taskflow.R
import com.jel.taskflow.tasks.model.Status

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatusGroupButtons(
    status: Status, onStatusChanged: (Status) -> Unit
) {
    Text(
        modifier = Modifier
            .padding(horizontal = 15.dp)
            .padding(top = 10.dp),
        text = stringResource(R.string.task_progress),
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