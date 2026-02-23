package com.jel.taskflow.tasks.presentation.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jel.taskflow.tasks.presentation.calendar.CalendarViewMode
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun YearView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onViewModeChanged: (CalendarViewMode) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .padding(8.dp)
            .heightIn(max = 300.dp)
    ) {
        stickyHeader {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { onDateSelected(selectedDate.minusYears(1)) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                        contentDescription = "Previous Year"
                    )
                }
                Text(
                    text = "${selectedDate.year}",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { onDateSelected(selectedDate.plusYears(1)) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                        contentDescription = "Next Year"
                    )
                }
            }
        }
        items(12) { monthIndex ->
            val monthDate = LocalDate.of(selectedDate.year, monthIndex + 1, 1)
            TextButton(onClick = {
                onDateSelected(monthDate)
                onViewModeChanged(CalendarViewMode.MONTH)
            }) {
                Text(monthDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
            }
        }
    }
}