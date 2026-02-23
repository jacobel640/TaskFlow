package com.jel.taskflow.tasks.presentation.calendar.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.presentation.calendar.CalendarViewMode
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthView(
    selectedDate: LocalDate,
    tasksByDate: Map<LocalDate, List<Task>>,
    onDateSelected: (LocalDate) -> Unit,
    onViewModeChanged: (CalendarViewMode) -> Unit
) {
    val yearMonth = YearMonth.from(selectedDate)
    val daysInMonth = yearMonth.lengthOfMonth()

    val firstDayOfMonth = yearMonth.atDay(1)
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7

    Column(modifier = Modifier.padding(8.dp)) {
        MonthTitle(
            onPreviousMonth = {
                onDateSelected(selectedDate.minusMonths(1))
            },
            onNextMonth = {
                onDateSelected(selectedDate.plusMonths(1))
            },
            yearMonth = yearMonth
        )
        DaysOfWeekHeader()

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(startOffset) { Spacer(modifier = Modifier.aspectRatio(1f)) }

            items(daysInMonth) { dayIndex ->
                val date = yearMonth.atDay(dayIndex + 1)
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