package com.jel.taskflow.tasks.ui.utils

sealed class Screen(val route: String) {
    object TasksScreen: Screen(route = "tasks_screen")
    object AddEditTaskScreen: Screen(route = "add_edit_task_screen")
}