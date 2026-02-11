package com.jel.taskflow.tasks.ui.utils

sealed class Screen(val route: String) {
    object TasksListScreen : Screen(route = "tasks_list_screen")
    object SingleTaskScreen : TaskScreen(baseRoute = "single_task_screen")
    object AddEditTaskScreen : TaskScreen(baseRoute = "add_edit_task_screen")
}

abstract class TaskScreen(baseRoute: String): Screen(route = baseRoute) {
    companion object {
        const val TASK_ID_ARG = "taskId"
    }
    fun withIdArg(taskId: Long? = null): String {
        return taskId?.let { "$route?$TASK_ID_ARG=$it" } ?: "$route?$TASK_ID_ARG={$TASK_ID_ARG}"
    }
}