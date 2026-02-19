package com.jel.taskflow.tasks.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jel.taskflow.R
import com.jel.taskflow.core.components.AnimatedSelector
import com.jel.taskflow.core.components.rememberDropdownSelectionState
import com.jel.taskflow.core.theme.TaskFlowTheme
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.SortDirection
import com.jel.taskflow.tasks.domain.model.enums.SortType
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.presentation.extensions.labelRes
import com.jel.taskflow.tasks.presentation.home.HomeUiActions
import com.jel.taskflow.tasks.presentation.home.HomeUiState
import com.jel.taskflow.tasks.presentation.home.collapsedColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val SortType.labelRes: Int
    get() = when (this) {
        SortType.TITLE -> R.string.title
        SortType.STATUS -> R.string.status
        SortType.PRIORITY -> R.string.priority
        SortType.CREATED -> R.string.date_created
        SortType.UPDATED -> R.string.date_updated
    }

@Composable
fun SearchAndFilterSection(
    uiState: HomeUiState,
    onUiAction: (HomeUiActions) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val filtersApplied by remember(
        uiState.settings.filterByStatus,
        uiState.settings.filterByPriority,
        uiState.searchQuery
    ) {
        derivedStateOf { uiState.isFilterApplied() }
    }

    LaunchedEffect(uiState.settings.filterByStatus, uiState.settings.filterByPriority) {
        delay(300)
        isExpanded = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(filtersApplied) {
                IconButton(
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.collapsedColor
                    ),
                    onClick = {
                        isExpanded = false
                        onUiAction(HomeUiActions.OnClearFilters)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Clear Filters"
                    )
                }
                Spacer(Modifier.padding(start = 5.dp))
            }
            TextButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (filtersApplied) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.collapsedColor,
                    contentColor =
                        if (filtersApplied) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                onClick = { isExpanded = !isExpanded }
            ) {
                Text(
                    text = if (isExpanded) stringResource(R.string.hide_filters)
                    else stringResource(R.string.show_filters)
                )
                Spacer(Modifier.padding(start = 5.dp))
                Icon(
                    imageVector =
                        if (isExpanded) Icons.Rounded.KeyboardArrowUp
                        else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Toggle Filters",
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            SortOptions(
                selectedSortType = uiState.settings.sortType,
                onSortTypeSelected = { onUiAction(HomeUiActions.OnSortTypeChange(it)) },
                selectedSortDirection = uiState.settings.sortDirection,
                onSortDirectionSelected = { onUiAction(HomeUiActions.OnSortDirectionChange(it)) }
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            Spacer(Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.collapsedColor,
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { onUiAction(HomeUiActions.OnSearchQueryChange(it)) },
                        label = { Text(stringResource(R.string.task_search)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                isExpanded = false
                            }
                        ),
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onUiAction(HomeUiActions.OnSearchQueryChange("")) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Clear,
                                        contentDescription = "Clear Text"
                                    )
                                }
                            }
                        }
                    )
                    FilterOptions(
                        selectedPriorities = uiState.settings.filterByPriority,
                        onPrioritySelected = { onUiAction(HomeUiActions.OnTogglePriorityFilters(it)) },
                        onClearPriorities = { onUiAction(HomeUiActions.OnClearPriorityFilters) },
                        selectedStatuses = uiState.settings.filterByStatus,
                        onStatusSelected = { onUiAction(HomeUiActions.OnToggleStatusFilters(it)) },
                        onClearStatuses = { onUiAction(HomeUiActions.OnClearStatusFilters) }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick = {
                                    onUiAction(
                                        HomeUiActions.OnToggleShowCompletedTasks(
                                            show = !uiState.settings.showCompletedTasks
                                        )
                                    )
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text =
                                stringResource(
                                    id =
                                        if (uiState.settings.showCompletedTasks) R.string.show_completed_tasks
                                        else R.string.hiding_completed_tasks
                                )
                        )
                        Switch(
                            checked = uiState.settings.showCompletedTasks,
                            onCheckedChange = {
                                onUiAction(
                                    HomeUiActions.OnToggleShowCompletedTasks(
                                        show = !uiState.settings.showCompletedTasks
                                    )
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SortOptions(
    selectedSortType: SortType,
    onSortTypeSelected: (SortType) -> Unit,
    selectedSortDirection: SortDirection,
    onSortDirectionSelected: (SortDirection) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SortDropdown(
            items = SortType.entries,
            selectedValue = selectedSortType,
            onValueSelected = onSortTypeSelected
        )
        VerticalDivider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.height(15.dp)
        )
        ToggleSortDirections(
            selectedSortDirection = selectedSortDirection,
            onSortDirectionSelected = onSortDirectionSelected
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SortDropdown(
    items: List<SortType>,
    selectedValue: SortType,
    onValueSelected: (SortType) -> Unit,
    dropDownWindowShape: CornerBasedShape = MaterialTheme.shapes.medium,
    dropDownItemShape: CornerBasedShape = MaterialTheme.shapes.large
) {
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    Box(contentAlignment = Alignment.BottomEnd) {
        TextButton(
            contentPadding = PaddingValues(
                horizontal = 5.dp
            ),
            onClick = { expanded = true }) {
            Icon(
                modifier = Modifier.graphicsLayer(scaleX = -1f),
                imageVector = Icons.AutoMirrored.Rounded.Sort,
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(horizontal = 3.dp),
                text = stringResource(selectedValue.labelRes),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Box(modifier = Modifier.size(0.dp)) { // workaround to keep the popUp aligned to the end of the button
            MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = dropDownWindowShape)) {
                val selectionState = rememberDropdownSelectionState(selectedValue, items)

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    CompositionLocalProvider(LocalRippleConfiguration provides null) {
                        Box(modifier = Modifier.width(IntrinsicSize.Max)) {
                            AnimatedSelector(selectionState, shape = dropDownItemShape)

                            Column {
                                items.forEachIndexed { index, item ->
                                    val selected = item == selectedValue
                                    DropdownMenuItem(
                                        modifier = Modifier
                                            .padding(horizontal = 5.dp)
                                            .onGloballyPositioned {
                                                selectionState.onItemLayout(
                                                    index,
                                                    it
                                                )
                                            }
                                            .clip(dropDownItemShape),
                                        text = {
                                            Text(
                                                text = stringResource(item.labelRes),
                                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Unspecified
                                            )
                                        },
                                        onClick = {
                                            onValueSelected(item)
                                            coroutineScope.launch {
                                                delay(700)
                                                expanded = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleSortDirections(
    selectedSortDirection: SortDirection,
    onSortDirectionSelected: (SortDirection) -> Unit
) {
    Icon(
        modifier = Modifier
            .clip(shape = CircleShape)
            .clickable(
                onClick = {
                    onSortDirectionSelected(
                        when (selectedSortDirection) {
                            SortDirection.ASC -> SortDirection.DESC
                            SortDirection.DESC -> SortDirection.ASC
                        }
                    )
                }
            )
            .padding(5.dp),
        imageVector =
            when (selectedSortDirection) {
                SortDirection.ASC -> Icons.Rounded.KeyboardArrowUp
                SortDirection.DESC -> Icons.Rounded.KeyboardArrowDown
            },
        tint = MaterialTheme.colorScheme.primary,
        contentDescription = "Toggle Sort Direction"
    )
}

@Composable
fun FilterOptions(
    selectedPriorities: Set<Priority>,
    onPrioritySelected: (Priority) -> Unit,
    onClearPriorities: () -> Unit,
    selectedStatuses: Set<Status>,
    onStatusSelected: (Status) -> Unit,
    onClearStatuses: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.width(5.dp))
        FilterChips(
            labelRes = R.string.priorities,
            items = Priority.entries,
            selectedValues = selectedPriorities,
            onValueSelected = onPrioritySelected,
            onClear = onClearPriorities
        )
        FilterChips(
            labelRes = R.string.statuses,
            items = Status.entries,
            selectedValues = selectedStatuses,
            onValueSelected = onStatusSelected,
            onClear = onClearStatuses
        )
    }
}

@Composable
fun <T> FilterChips(
    labelRes: Int,
    items: List<T>,
    selectedValues: Set<T>,
    onValueSelected: (T) -> Unit,
    onClear: () -> Unit
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
        ) {
            item {
                FilterChip(
                    label = when (items[0]) {
                        is Status -> stringResource(R.string.all_statuses)
                        is Priority -> stringResource(R.string.all_priorities)
                        else -> "All"
                    },
                    onSelect = onClear,
                    isSelected = selectedValues.isEmpty() || items.size == selectedValues.size
                )
            }
            items(items = items) {
                FilterChip(
                    label = when (it) {
                        is Status -> stringResource(it.labelRes)
                        is Priority -> stringResource(it.labelRes)
                        else -> it.toString()
                    },
                    onSelect = { onValueSelected(it) },
                    isSelected = it in selectedValues
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    onSelect: () -> Unit,
    isSelected: Boolean
) {
    ElevatedFilterChip(
        label = { Text(label) },
        onClick = onSelect,
        selected = isSelected,
        leadingIcon = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SearchAndFilterSectionPreview() {
    TaskFlowTheme {
        SearchAndFilterSection(
            uiState = HomeUiState(),
            onUiAction = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SortOptionsPreview() {
    TaskFlowTheme {
        SortOptions(
            selectedSortType = SortType.UPDATED,
            onSortTypeSelected = { },
            selectedSortDirection = SortDirection.ASC,
            onSortDirectionSelected = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FilterOptionsPreview() {
    TaskFlowTheme {
        FilterOptions(
            selectedPriorities = setOf(Priority.MEDIUM),
            onPrioritySelected = { },
            onClearPriorities = {},
            selectedStatuses = setOf(Status.IN_PROGRESS),
            onStatusSelected = { },
            onClearStatuses = {}
        )
    }
}