package com.jel.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jel.taskflow.tasks.model.AddEditTaskViewModel
import com.jel.taskflow.tasks.ui.AddEditTaskScreen
import com.jel.taskflow.tasks.ui.SingleTaskScreen
import com.jel.taskflow.tasks.ui.utils.Screen
import com.jel.taskflow.tasks.ui.TasksListScreen
import com.jel.taskflow.tasks.ui.componenets.FullScreenContentEdit
import com.jel.taskflow.tasks.ui.utils.TaskScreen
import com.jel.taskflow.ui.theme.TaskFlowTheme
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
                    startDestination = Screen.TasksListScreen.route
                )  {
                    composable(route = Screen.TasksListScreen.route) {
                        TasksListScreen(navController = navController)
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
                        ),
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        }
                    ) {
                        // the SavedStateHandle in the AddEditTaskViewModel is injecting the above taskId argument
                        AddEditTaskScreen(navController = navController)
                    }
                    composable(
                        route = Screen.FullScreenContentEdit.route,
                        enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        val parentEntry = remember(it) {
                            navController.getBackStackEntry(Screen.AddEditTaskScreen.withIdArg())
                        }
                        val sharedViewModel = hiltViewModel<AddEditTaskViewModel>(parentEntry)
                        FullScreenContentEdit(
                            viewModel = sharedViewModel,
                            onNavigationBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}