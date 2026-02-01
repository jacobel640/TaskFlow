package com.jel.taskflow.utils

import androidx.room.TypeConverter
import com.jel.taskflow.tasks.model.enums.Priority
import com.jel.taskflow.tasks.model.enums.Status
import kotlin.time.Instant

class Converters {

    @TypeConverter
    fun fromStatus(status: Status): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(value: String): Status {
        return Status.valueOf(value)
    }

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }

    @TypeConverter
    fun fromInstantToTimestamp(instant: Instant): Long {
        return instant.toEpochMilliseconds()
    }

    @TypeConverter
    fun fromTimestamp(timestamp: Long): Instant {
        return Instant.fromEpochMilliseconds(timestamp)
    }
}