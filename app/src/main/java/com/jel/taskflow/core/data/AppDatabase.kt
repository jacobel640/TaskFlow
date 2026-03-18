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
    version = 4,
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
    }
}