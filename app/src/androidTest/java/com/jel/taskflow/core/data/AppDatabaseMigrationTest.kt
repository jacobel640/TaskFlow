package com.jel.taskflow.core.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val testDatabase = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate4To5_keepsTasksAndGivesEachAUniqueRemoteId() {
        helper.createDatabase(testDatabase, 4).use { db ->
            db.execSQL("INSERT INTO tasks (title, content, status, createdDate, changedDate) VALUES ('a', '', 'TODO', 1, 10)")
            db.execSQL("INSERT INTO tasks (title, content, status, createdDate, changedDate) VALUES ('b', '', 'COMPLETED', 2, 20)")
        }

        val db = helper.runMigrationsAndValidate(testDatabase, 5, true, AppDatabase.MIGRATION_4_5)

        db.query("SELECT remoteId, isDeleted, isDirty, lastModified, changedDate FROM tasks").use { cursor ->
            val remoteIds = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                remoteIds.add(cursor.getString(0))
                assertEquals(0, cursor.getInt(1))
                assertEquals(1, cursor.getInt(2))
                assertEquals(cursor.getLong(4), cursor.getLong(3))
            }
            assertEquals(2, remoteIds.size)
            assertTrue(remoteIds.none { it.isBlank() })
        }
    }
}
