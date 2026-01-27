package com.jel.taskflow.tasks.ui.componenets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jel.taskflow.ui.theme.TaskFlowTheme

@Composable
fun IndicatorChip(
    modifier: Modifier = Modifier,
    label: String = "",
    imageVector: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentDescription: String? = null
) {
    Box(modifier = modifier) {
        Surface(
            color = containerColor,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(
                width = 0.2.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                imageVector?.let {
                    val fontSizeSp = MaterialTheme.typography.labelSmall.fontSize.times(1.5f)
                    val iconSizeDp = with(LocalDensity.current) { fontSizeSp.toDp() }

                    Icon(
                        modifier = Modifier.size(iconSizeDp),
                        imageVector = imageVector,
                        tint = color,
                        contentDescription = contentDescription
                    )
                }
                if (label.isNotBlank()) {
//                    Spacer(
//                        Modifier.padding(
//                            if (imageVector == null) 3.dp
//                            else 1.dp
//                        )
//                    )
                    Text(
                        modifier =
                            if (imageVector == null) Modifier.padding(horizontal = 5.dp)
                            else Modifier.padding(start = 1.5.dp, end = 5.dp),
                        text = label,
                        color = color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = MaterialTheme.typography.bodySmall.fontWeight,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize.times(0.8)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun IndicatorChipPreview() {
    TaskFlowTheme {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.safeContentPadding()) {
                IndicatorChip(
                    imageVector = Icons.Rounded.Pending,
                    color = MaterialTheme.colorScheme.onError,
                    containerColor = MaterialTheme.colorScheme.onErrorContainer
                )
                IndicatorChip(
                    imageVector = Icons.Rounded.Pending,
                    color = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.inverseOnSurface
                )
                IndicatorChip(
                    imageVector = Icons.Rounded.Pending
                )
            }
            IndicatorChip(label = "label")
            IndicatorChip(
                label = "label",
                imageVector = Icons.Rounded.Pending
            )
        }
    }
}