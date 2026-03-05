package com.jel.taskflow.core.utils

import com.jel.taskflow.tasks.domain.model.Task
import kotlinx.serialization.Serializable

sealed class Screen(val route: String) {
    object HomeScreen : Screen(route = "tasks_list_screen")
    object CalendarScreen : Screen(route = "calendar_screen")
    object AddEditTaskScreen : TaskScreen(baseRoute = "add_edit_task_screen")
    @Serializable
    class SingleTaskScreen(val task: Task)
}

abstract class TaskScreen(baseRoute: String): Screen(route = baseRoute) {
    companion object {
        const val TASK_ID_ARG = "taskId"
    }
    fun withIdArg(taskId: Long? = null): String {
        return taskId?.let { "$route?$TASK_ID_ARG=$it" } ?: "$route?$TASK_ID_ARG={$TASK_ID_ARG}"
    }
}