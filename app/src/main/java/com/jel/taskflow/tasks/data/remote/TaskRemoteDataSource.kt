package com.jel.taskflow.tasks.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jel.taskflow.tasks.data.remote.TaskFirestoreMapper.SERVER_UPDATED_AT
import com.jel.taskflow.tasks.domain.model.Task
import kotlinx.coroutines.tasks.await
import java.util.Date

data class RemoteTask(val task: Task, val serverUpdatedAtMillis: Long)

/**
 * Tasks are stored per user under users/{uid}/tasks/{remoteId}.
 * Firestore is created on first use, so a local-only build (no Firebase) never touches it.
 */
class TaskRemoteDataSource(firestoreProvider: () -> FirebaseFirestore) {

    private val firestore by lazy(firestoreProvider)

    private fun tasksCollection(uid: String): CollectionReference =
        firestore.collection(USERS_COLLECTION).document(uid).collection(TASKS_COLLECTION)

    suspend fun getTasksUpdatedSince(uid: String, sinceMillis: Long): List<RemoteTask> =
        tasksCollection(uid)
            .whereGreaterThan(SERVER_UPDATED_AT, Timestamp(Date(sinceMillis)))
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                val data = document.data ?: return@mapNotNull null
                val task = TaskFirestoreMapper.fromMap(document.id, data) ?: return@mapNotNull null
                val serverUpdatedAt = document.getTimestamp(SERVER_UPDATED_AT)?.toDate()?.time ?: sinceMillis
                RemoteTask(task, serverUpdatedAt)
            }

    suspend fun upsertTasks(uid: String, tasks: List<Task>) {
        val collection = tasksCollection(uid)
        tasks.chunked(MAX_BATCH_SIZE).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { task ->
                val data = TaskFirestoreMapper.toMap(task) + (SERVER_UPDATED_AT to FieldValue.serverTimestamp())
                batch.set(collection.document(task.remoteId), data)
            }
            batch.commit().await()
        }
    }

    suspend fun deleteAllTasks(uid: String) {
        val documents = tasksCollection(uid).get().await().documents
        documents.chunked(MAX_BATCH_SIZE).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    companion object {
        const val USERS_COLLECTION = "users"
        const val TASKS_COLLECTION = "tasks"
        // Firestore allows up to 500 writes per batch
        private const val MAX_BATCH_SIZE = 400
    }
}
