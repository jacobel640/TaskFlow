package com.jel.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jel.taskflow.auth.presentation.account.AccountScreen
import com.jel.taskflow.auth.presentation.welcome.WelcomeScreen
import com.jel.taskflow.tasks.domain.repository.UserPreferencesRepository
import com.jel.taskflow.tasks.presentation.add_edit_task.AddEditTaskScreen
import com.jel.taskflow.tasks.presentation.single_task.SingleTaskScreen
import com.jel.taskflow.tasks.presentation.home.HomeScreen
import com.jel.taskflow.core.utils.Screen
import com.jel.taskflow.core.utils.TaskScreen
import com.jel.taskflow.core.theme.TaskFlowTheme
import com.jel.taskflow.tasks.presentation.calendar.CalendarScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            TaskFlowTheme {
                // the welcome screen is shown only until the user picks Google sign in or guest mode
                var startDestination by remember { mutableStateOf<String?>(null) }
                LaunchedEffect(Unit) {
                    startDestination =
                        if (userPreferencesRepository.onboardingDoneFlow.first()) Screen.HomeScreen.route
                        else Screen.WelcomeScreen.route
                }

                val currentStartDestination = startDestination
                if (currentStartDestination == null) {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
                } else {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = currentStartDestination
                    )  {
                        composable(route = Screen.WelcomeScreen.route) {
                            WelcomeScreen(navController = navController)
                        }
                        composable(route = Screen.AccountScreen.route) {
                            AccountScreen(navController = navController)
                        }
                        composable(route = Screen.HomeScreen.route) {
                            HomeScreen(navController = navController)
                        }
                        composable(route = Screen.CalendarScreen.route) {
                            CalendarScreen(navController = navController)
                        }
                        composable(
                            route = Screen.SingleTaskScreen.withArgs(),
                            arguments = listOf(
                                navArgument(name = TaskScreen.TASK_ID_ARG) {
                                    type = NavType.LongType
                                }
                            )
                        ) {
                            SingleTaskScreen(navController = navController)
                        }
                        composable(
                            route = Screen.AddEditTaskScreen.withArgs(),
                            arguments = listOf(
                                navArgument(name = TaskScreen.TASK_ID_ARG) {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                },
                                navArgument(name = TaskScreen.INSTANT_MILLI_ARG) {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) {
                            // the SavedStateHandle in the AddEditTaskViewModel is injecting the above taskId argument
                            AddEditTaskScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
