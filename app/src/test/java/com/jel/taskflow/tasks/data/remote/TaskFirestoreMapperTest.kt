package com.jel.taskflow.tasks.data.remote

import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class TaskFirestoreMapperTest {

    private val task = Task(
        id = 7L,
        title = "Buy milk",
        content = "2 liters",
        status = Status.IN_PROGRESS,
        priority = Priority.HIGH,
        dueDate = Instant.fromEpochMilliseconds(1_700_000_000_000),
        createdDate = Instant.fromEpochMilliseconds(1_600_000_000_000),
        changedDate = Instant.fromEpochMilliseconds(1_650_000_000_000),
        remoteId = "remote-1",
        isDirty = true,
        lastModified = 1_650_000_000_123
    )

    @Test
    fun `round trip keeps all task fields`() {
        val restored = TaskFirestoreMapper.fromMap(task.remoteId, TaskFirestoreMapper.toMap(task))

        // the local id is never stored in the cloud, and a pulled task is never dirty
        assertEquals(task.copy(id = null, isDirty = false), restored)
    }

    @Test
    fun `round trip keeps null priority and null due date`() {
        val noPriorityTask = task.copy(priority = null, dueDate = null)

        val restored = TaskFirestoreMapper.fromMap(noPriorityTask.remoteId, TaskFirestoreMapper.toMap(noPriorityTask))

        assertNull(restored?.priority)
        assertNull(restored?.dueDate)
    }

    @Test
    fun `unknown enum values fall back to defaults`() {
        val data = TaskFirestoreMapper.toMap(task) +
                mapOf(TaskFirestoreMapper.STATUS to "ARCHIVED", TaskFirestoreMapper.PRIORITY to "URGENT")

        val restored = TaskFirestoreMapper.fromMap(task.remoteId, data)

        assertEquals(Status.TODO, restored?.status)
        assertNull(restored?.priority)
    }

    @Test
    fun `document without created date is ignored`() {
        val data = TaskFirestoreMapper.toMap(task) - TaskFirestoreMapper.CREATED_DATE

        assertNull(TaskFirestoreMapper.fromMap(task.remoteId, data))
    }

    @Test
    fun `missing deleted flag means not deleted`() {
        val data = TaskFirestoreMapper.toMap(task) - TaskFirestoreMapper.IS_DELETED

        assertFalse(TaskFirestoreMapper.fromMap(task.remoteId, data)!!.isDeleted)
    }
}
