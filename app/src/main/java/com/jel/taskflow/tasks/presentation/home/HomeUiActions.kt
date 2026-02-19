package com.jel.taskflow.tasks.presentation.home

import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.SortDirection
import com.jel.taskflow.tasks.domain.model.enums.SortType
import com.jel.taskflow.tasks.domain.model.enums.Status

sealed class HomeUiActions {
    data class OnSearchQueryChange(val query: String) : HomeUiActions()
    data class OnSortTypeChange(val sortType: SortType) : HomeUiActions()
    data class OnSortDirectionChange(val direction: SortDirection) : HomeUiActions()
    data class OnTogglePriorityFilters(val priority: Priority) : HomeUiActions()
    data class OnToggleStatusFilters(val status: Status) : HomeUiActions()
    data class OnToggleShowCompletedTasks(val show: Boolean) : HomeUiActions()
    object OnClearStatusFilters : HomeUiActions()
    object OnClearPriorityFilters : HomeUiActions()
    object OnClearFilters : HomeUiActions()
}