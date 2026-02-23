package com.jel.taskflow.tasks.presentation.calendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jel.taskflow.R
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.presentation.calendar.CalendarViewMode
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeekView(
    selectedDate: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>,
    onDateSelected: (LocalDate) -> Unit,
    onViewModeChanged: (CalendarViewMode) -> Unit
) {
    val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value % 7).toLong())
    val weekDates = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    Column(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            IconButton(onClick = { onDateSelected(selectedDate.minusDays(7)) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                    contentDescription = "Previous Week"
                )
            }
            Text(
                text = stringResource(R.string.week_view_title,
                    startOfWeek.dayOfMonth,
                    startOfWeek.month.getDisplayName(
                        TextStyle.FULL,
                        Locale.getDefault()
                    )
                ),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { onDateSelected(selectedDate.plusDays(7)) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                    contentDescription = "Next Week"
                )
            }
        }
        DaysOfWeekHeader()
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            weekDates.forEach { date ->
                Box(modifier = Modifier.weight(1f)) {
                    DayCell(
                        date = date,
                        tasks = tasksByDate[date] ?: emptyList(),
                        onClick = {
                            onDateSelected(date)
                            onViewModeChanged(CalendarViewMode.DAY)
                        }
                    )
                }
            }
        }
    }
}