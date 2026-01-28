package com.jel.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jel.taskflow.tasks.ui.AddEditTaskScreen
import com.jel.taskflow.tasks.ui.utils.Screen
import com.jel.taskflow.tasks.ui.TasksScreen
import com.jel.taskflow.ui.theme.TaskFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskFlowTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.TasksScreen.route
                )  {
                    composable(route = Screen.TasksScreen.route) {
                        TasksScreen(navController = navController)
                    }
                    composable(
                        route = Screen.AddEditTaskScreen.route + "?taskId={taskId}",
                        arguments = listOf(
                            navArgument(
                                name = "taskId"
                            ) {
                                type = NavType.LongType
                                defaultValue = -1L
                            }
                        )
                    ) {
                        // the SavedStateHandle in the AddEditTaskViewModel is injecting the above taskId argument
                        AddEditTaskScreen()
                    }
                }
            }
        }
    }
}