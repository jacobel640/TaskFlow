package com.jel.taskflow.core.utils

import kotlin.time.Instant

sealed class Screen(val route: String) {
    object HomeScreen : Screen(route = "tasks_list_screen")
    object CalendarScreen : Screen(route = "calendar_screen")
    object SingleTaskScreen : TaskScreen(baseRoute = "single_task_screen")
    object AddEditTaskScreen : TaskScreen(baseRoute = "add_edit_task_screen")
}

abstract class TaskScreen(baseRoute: String): Screen(route = baseRoute) {
    companion object {
        const val TASK_ID_ARG = "taskId"
        const val INSTANT_MILLI_ARG = "instantMilli"
    }
    fun withArgs(taskId: Long? = null, selectedDueDate: Instant? = null): String {
        val idArg = taskId?.let { "$TASK_ID_ARG=$it" } ?: "$TASK_ID_ARG={$TASK_ID_ARG}"

        val calendarArg = selectedDueDate?.let { "$INSTANT_MILLI_ARG=${it.toEpochMilliseconds()}" }
            ?: "$INSTANT_MILLI_ARG={$INSTANT_MILLI_ARG}"

        return "$route?$idArg&$calendarArg"
    }
}