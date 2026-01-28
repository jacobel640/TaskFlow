package com.jel.taskflow.tasks.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jel.taskflow.tasks.model.Task
import com.jel.taskflow.utils.Converters

@Database(
    entities = [Task::class],
    version = 1,
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
//    ]
)
@TypeConverters(Converters::class)
abstract class TaskDatabase: RoomDatabase() {

    abstract val taskDao: TaskDao

    companion object {
        const val DATABASE_NAME = "tasks_db"
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Task ADD COLUMN priority INTEGER NOT NULL DEFAULT MEDIUM")
            }
        }
    }
}