package com.jel.taskflow.tasks.ui.utils

sealed class Screen(val route: String) {
    object TasksScreen: Screen(route = "tasks_list_screen")
    object TaskScreen: Screen(route = "task_screen")
    object AddEditTaskScreen: Screen(route = "add_edit_task_screen")
    object FullScreenContentEdit : Screen(route = "content_edit_screen")
}