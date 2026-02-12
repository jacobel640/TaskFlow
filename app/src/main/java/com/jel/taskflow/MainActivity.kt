package com.jel.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jel.taskflow.tasks.presentation.task.AddEditTaskScreen
import com.jel.taskflow.tasks.presentation.task.SingleTaskScreen
import com.jel.taskflow.tasks.presentation.home.HomeScreen
import com.jel.taskflow.core.utils.Screen
import com.jel.taskflow.core.utils.TaskScreen
import com.jel.taskflow.core.theme.TaskFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            TaskFlowTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.HomeScreen.route
                )  {
                    composable(route = Screen.HomeScreen.route) {
                        HomeScreen(navController = navController)
                    }
                    composable(
                        route = Screen.SingleTaskScreen.withIdArg(),
                        arguments = listOf(
                            navArgument(name = TaskScreen.TASK_ID_ARG) {
                                type = NavType.LongType
                            }
                        )
                    ) {
                        SingleTaskScreen(navController = navController)
                    }
                    composable(
                        route = Screen.AddEditTaskScreen.withIdArg(),
                        arguments = listOf(
                            navArgument(name = TaskScreen.TASK_ID_ARG) {
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