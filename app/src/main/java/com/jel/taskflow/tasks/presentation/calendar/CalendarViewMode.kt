package com.jel.taskflow.tasks.presentation.calendar

import com.jel.taskflow.R

enum class CalendarViewMode { DAY, WEEK, MONTH, YEAR }

val CalendarViewMode.titleRes: Int
    get() = when(this) {
        CalendarViewMode.DAY -> R.string.day
        CalendarViewMode.WEEK -> R.string.week
        CalendarViewMode.MONTH -> R.string.month
        CalendarViewMode.YEAR -> R.string.year
    }