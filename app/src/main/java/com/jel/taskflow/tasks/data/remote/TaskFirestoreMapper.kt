package com.jel.taskflow.tasks.data.remote

import com.jel.taskflow.tasks.domain.model.Task
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.Status
import kotlin.time.Instant

/**
 * Manual mapping between [Task] and a Firestore document, so no reflection is involved
 * (safe under R8 without keep rules). The document id is the task's remoteId.
 */
object TaskFirestoreMapper {

    const val TITLE = "title"
    const val CONTENT = "content"
    const val STATUS = "status"
    const val PRIORITY = "priority"
    const val DUE_DATE = "dueDate"
    const val CREATED_DATE = "createdDate"
    const val CHANGED_DATE = "changedDate"
    const val LAST_MODIFIED = "lastModified"
    const val IS_DELETED = "isDeleted"
    const val SERVER_UPDATED_AT = "serverUpdatedAt"

    fun toMap(task: Task): Map<String, Any?> = mapOf(
        TITLE to task.title,
        CONTENT to task.content,
        STATUS to task.status.name,
        PRIORITY to task.priority?.name,
        DUE_DATE to task.dueDate?.toEpochMilliseconds(),
        CREATED_DATE to task.createdDate.toEpochMilliseconds(),
        CHANGED_DATE to task.changedDate.toEpochMilliseconds(),
        LAST_MODIFIED to task.lastModified,
        IS_DELETED to task.isDeleted
    )

    /** Returns null for documents that are missing required fields. */
    fun fromMap(remoteId: String, data: Map<String, Any?>): Task? {
        val createdDate = (data[CREATED_DATE] as? Number)?.toLong() ?: return null
        return Task(
            title = data[TITLE] as? String ?: "",
            content = data[CONTENT] as? String ?: "",
            status = (data[STATUS] as? String)
                ?.let { runCatching { Status.valueOf(it) }.getOrNull() }
                ?: Status.TODO,
            priority = (data[PRIORITY] as? String)
                ?.let { runCatching { Priority.valueOf(it) }.getOrNull() },
            dueDate = (data[DUE_DATE] as? Number)?.let { Instant.fromEpochMilliseconds(it.toLong()) },
            createdDate = Instant.fromEpochMilliseconds(createdDate),
            changedDate = Instant.fromEpochMilliseconds(
                (data[CHANGED_DATE] as? Number)?.toLong() ?: createdDate
            ),
            remoteId = remoteId,
            isDeleted = data[IS_DELETED] as? Boolean ?: false,
            isDirty = false,
            lastModified = (data[LAST_MODIFIED] as? Number)?.toLong() ?: 0L
        )
    }
}
