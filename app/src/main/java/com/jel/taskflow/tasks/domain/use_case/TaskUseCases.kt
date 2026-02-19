package com.jel.taskflow.tasks.domain.use_case

data class TaskUseCases(
    val getFilteredTasks: GetFilteredTasks,
    val getTask: GetTask,
    val insertTask: InsertTask,
    val deleteTask: DeleteTask,
    val getTasksCount: GetTasksCount
)