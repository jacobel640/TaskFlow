package com.jel.taskflow.tasks.data.repository

import com.jel.taskflow.tasks.domain.model.TaskSettings
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilteredTasksQueryTest {

    private fun placeholderCount(sql: String) = sql.count { it == '?' }

    @Test
    fun `deleted tasks are always excluded`() {
        val query = buildFilteredTasksQuery(TaskSettings(), searchQuery = "")

        assertTrue(query.sql.contains("isDeleted = 0"))
    }

    @Test
    fun `search query is bound as an argument and not concatenated`() {
        val query = buildFilteredTasksQuery(TaskSettings(), searchQuery = "it's'); DROP TABLE tasks; --")

        assertFalse(query.sql.contains("DROP TABLE"))
        assertEquals(placeholderCount(query.sql), query.argCount)
    }

    @Test
    fun `only no-priority filter does not leave unbound placeholders`() {
        val settings = TaskSettings(filterByPriority = setOf(null))

        val query = buildFilteredTasksQuery(settings, searchQuery = "")

        assertTrue(query.sql.contains("AND (priority IS NULL)"))
        assertEquals(placeholderCount(query.sql), query.argCount)
    }

    @Test
    fun `no-priority filter is grouped with the other priorities`() {
        val settings = TaskSettings(
            filterByPriority = setOf(Priority.HIGH, null),
            filterByStatus = setOf(Status.TODO)
        )

        val query = buildFilteredTasksQuery(settings, searchQuery = "milk")

        assertTrue(query.sql.contains("AND (priority IN (?) OR priority IS NULL)"))
        assertEquals(placeholderCount(query.sql), query.argCount)
    }

    @Test
    fun `due date range is bound as arguments`() {
        val query = buildFilteredTasksQuery(
            TaskSettings(),
            searchQuery = "",
            requireDueDate = true,
            dueDateStart = 1L,
            dueDateEnd = 2L
        )

        assertTrue(query.sql.contains("dueDate BETWEEN ? AND ?"))
        assertEquals(placeholderCount(query.sql), query.argCount)
    }
}
