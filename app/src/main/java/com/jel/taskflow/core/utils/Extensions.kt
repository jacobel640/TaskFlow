package com.jel.taskflow.core.utils

import android.text.format.DateUtils
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlin.time.Instant

fun Instant.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val time = this.toEpochMilliseconds()

    return DateUtils.getRelativeTimeSpanString(
        time,
        now,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarDefaults.flatColors(
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground
): TopAppBarColors {
    return topAppBarColors(
        containerColor = containerColor,
        scrolledContainerColor = containerColor,
        navigationIconContentColor = contentColor,
        titleContentColor = contentColor,
        actionIconContentColor = contentColor
    )
}