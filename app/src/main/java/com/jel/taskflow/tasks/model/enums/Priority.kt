package com.jel.taskflow.tasks.model.enums

import android.content.Context
import com.jel.taskflow.R

enum class Priority {
    HIGH, MEDIUM, LOW;

    fun getLabel(context: Context): String = when (this) {
        LOW -> context.getString(R.string.priority_low)
        MEDIUM -> context.getString(R.string.priority_medium)
        HIGH -> context.getString(R.string.priority_high)
    }
}