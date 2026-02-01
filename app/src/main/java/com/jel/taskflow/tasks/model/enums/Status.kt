package com.jel.taskflow.tasks.model.enums

import android.content.Context
import com.jel.taskflow.R

enum class Status {
    TODO, IN_PROGRESS, COMPLETED;

    fun getLabel(context: Context): String = when (this) {
        TODO -> context.getString(R.string.status_todo)
        IN_PROGRESS -> context.getString(R.string.status_pending)
        COMPLETED -> context.getString(R.string.status_completed)
    }
}