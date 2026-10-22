package com.jel.taskflow.core.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jel.taskflow.tasks.data.TaskDao
import com.jel.taskflow.tasks.domain.model.Task

@Database(
    entities = [Task::class],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract val taskDao: TaskDao

    companion object {
        const val DATABASE_NAME = "tasks_db"
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                var columnExists = false
                val cursor = db.query("PRAGMA table_info(tasks)")
                cursor.use {
                    val nameIndex = it.getColumnIndex("name")
                    while (it.moveToNext()) {
                        if (it.getString(nameIndex) == "priority") {
                            columnExists = true
                            break
                        }
                    }
                }

                if (!columnExists) {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN priority TEXT NOT NULL DEFAULT 'MEDIUM'")
                }
            }
        }

        // adds the sync metadata columns - every existing task gets a unique remoteId
        // and is marked dirty so it will be backed up on the first sign in
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN remoteId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE tasks ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE tasks ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE tasks SET remoteId = lower(hex(randomblob(16))), lastModified = changedDate")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tasks_remoteId ON tasks (remoteId)")
            }
        }
    }
}
