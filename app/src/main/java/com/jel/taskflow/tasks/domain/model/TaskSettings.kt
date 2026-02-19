package com.jel.taskflow.tasks.domain.model

import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.SortDirection
import com.jel.taskflow.tasks.domain.model.enums.SortType
import com.jel.taskflow.tasks.domain.model.enums.Status

data class TaskSettings(
    val sortType: SortType = SortType.CREATED,
    val sortDirection: SortDirection = SortDirection.DESC,
    val filterByPriority: Set<Priority> = emptySet(),
    val filterByStatus: Set<Status> = emptySet(),
    val showCompletedTasks: Boolean = false
)