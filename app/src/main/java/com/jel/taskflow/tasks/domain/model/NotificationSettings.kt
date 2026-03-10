package com.jel.taskflow.tasks.domain.model

import com.jel.taskflow.tasks.presentation.calendar.components.daysStartingSunday
import java.time.DayOfWeek

data class NotificationSettings(
    val hour: Int = 9,
    val minute: Int = 0,
    val days: Set<DayOfWeek> = daysStartingSunday.toSet()
)