package com.jel.taskflow.tasks.presentation.calendar

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jel.taskflow.R
import com.jel.taskflow.core.utils.Screen
import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.presentation.calendar.components.DayHeaderView
import com.jel.taskflow.tasks.presentation.calendar.components.MonthView
import com.jel.taskflow.tasks.presentation.calendar.components.WeekView
import com.jel.taskflow.tasks.presentation.calendar.components.YearView
import com.jel.taskflow.tasks.presentation.home.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    navController: NavController
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val tasksByDate by viewModel.tasksByDate.collectAsStateWithLifecycle()
    val allTasks = remember(tasksByDate) {
        tasksByDate.values.flatten()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.tasks_calendar)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ViewModeSelector(
                currentMode = viewMode,
                onModeSelected = viewModel::onViewModeChanged
            )

            Surface(
                modifier = Modifier.fillMaxWidth().zIndex(1f),
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Crossfade(targetState = viewMode, label = "ViewModeAnimation") { mode ->
                    when (mode) {
                        CalendarViewMode.DAY -> DayHeaderView(
                            selectedDate,
                            viewModel::onDateSelected
                        )

                        CalendarViewMode.WEEK -> WeekView(
                            selectedDate,
                            tasksByDate,
                            viewModel::onDateSelected,
                            viewModel::onViewModeChanged
                        )

                        CalendarViewMode.MONTH -> MonthView(
                            selectedDate,
                            tasksByDate,
                            viewModel::onDateSelected,
                            viewModel::onViewModeChanged
                        )

                        CalendarViewMode.YEAR -> YearView(
                            selectedDate,
                            viewModel::onDateSelected,
                            viewModel::onViewModeChanged
                        )
                    }
                }
            }

            TasksListForDate(allTasks, navController)
        }
    }
}

@Composable
fun ViewModeSelector(currentMode: CalendarViewMode, onModeSelected: (CalendarViewMode) -> Unit) {
    val modes = CalendarViewMode.entries.toTypedArray()
    val selectedTabIndex = modes.indexOf(currentMode)

    SecondaryTabRow(
        selectedTabIndex,
        Modifier,
        TabRowDefaults.primaryContainerColor,
        TabRowDefaults.primaryContentColor,
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(selectedTabIndex = selectedTabIndex)
            )
        },
        @Composable { HorizontalDivider() },
        {
            modes.forEachIndexed { index, mode ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onModeSelected(mode) },
                    text = { Text(text = stringResource(mode.titleRes)) }
                )
            }
        }
    )
}

@Composable
fun TasksListForDate(tasks: List<Task>, navController: NavController) {
    var expandedItem by remember { mutableStateOf<Task?>(null) }

    if (tasks.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.calendar_day_has_no_scheduled_tasks),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks, key = { it.id ?: it.hashCode() }) { task ->
                TaskItem(
                    task = task,
                    expanded = (task == expandedItem),
                    onExpandedClicked = {
                        expandedItem = if (expandedItem == task) null else task
                    },
                    onClick = {
                        task.id?.let {
                            navController.navigate(Screen.SingleTaskScreen.withIdArg(it))
                        }
                    },
                    onDelete = {
                        // TODO
                    }
                )
            }
        }
    }
}