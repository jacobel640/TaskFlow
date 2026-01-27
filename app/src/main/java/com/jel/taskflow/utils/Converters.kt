package com.jel.taskflow.utils

import androidx.room.TypeConverter
import com.jel.taskflow.tasks.model.Priority
import com.jel.taskflow.tasks.model.Status
import java.util.Date

class Converters {

    @TypeConverter
    fun fromStatus(status: Status): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(value: String): Status {
        return Status.valueOf(value)
    }

    class Converters {
        @TypeConverter
        fun fromPriority(priority: Priority): String {
            return priority.name
        }

        @TypeConverter
        fun toPriority(priority: String): Priority {
            return Priority.valueOf(priority)
        }
    }

    @TypeConverter
    fun fromDateToTimestamp(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun fromTimestamp(timestamp: Long): Date {
        return Date(timestamp)
    }
}