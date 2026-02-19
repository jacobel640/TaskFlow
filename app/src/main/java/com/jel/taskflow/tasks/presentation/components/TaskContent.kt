package com.jel.taskflow.tasks.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jel.taskflow.R
import com.jel.taskflow.core.components.ScrollableTextField
import com.jel.taskflow.tasks.domain.model.enums.Priority
import com.jel.taskflow.tasks.domain.model.enums.Status
import com.jel.taskflow.tasks.presentation.task.AddEditTaskUiState
import com.jel.taskflow.core.theme.TaskFlowTheme
import com.jel.taskflow.core.utils.toRelativeTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaskContent(
    state: AddEditTaskUiState,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (TextFieldValue) -> Unit,
    onStatusChanged: (Status) -> Unit,
    onPriorityChanged: (Priority) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit
) {
    var isContentFullScreen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .imePadding()
    ) {
        AnimatedVisibility(visible = !isContentFullScreen){
            Column {
                Row {
                    TextField(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.title)) },
                        value = state.title,
                        onValueChange = onTitleChanged
                    )
                    PriorityDropDown(
                        modifier = Modifier.padding(horizontal = 15.dp),
                        priority = state.priority,
                        onPriorityChanged = onPriorityChanged
                    )
                }
                StatusGroupButtons(
                    status = state.status, onStatusChanged = onStatusChanged
                )
                Spacer(modifier = Modifier.padding(vertical = 5.dp))
            }
        }
        ContentTextField(
            contentValue = state.content,
            onContentChanged = onContentChanged,
            canUndo = state.canUndo,
            canRedo = state.canRedo,
            onUndo = onUndo,
            onRedo = onRedo,
            setContentFullScreen = { isFullScreenContent ->
                isContentFullScreen = isFullScreenContent
            }
        )
        Row {
            Text(
                text = stringResource(R.string.created_at, state.createdDate.toRelativeTime()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.updated_at, state.changedDate.toRelativeTime()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentTextField(
    contentValue: TextFieldValue,
    onContentChanged: (TextFieldValue) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    setContentFullScreen: (Boolean) -> Unit
) {
    var isContentFocused by remember { mutableStateOf(false) }
    val isKeyboardVisible = WindowInsets.isImeVisible


    LaunchedEffect(isContentFocused, isKeyboardVisible) {
        if (isContentFocused && isKeyboardVisible) setContentFullScreen(true)
        else setContentFullScreen(false)
    }

    Box {
        ScrollableTextField(
            modifier = Modifier.fillMaxSize()
                .onFocusChanged { isContentFocused = it.isFocused },
            label = { Text(text = stringResource(R.string.task_content)) },
            value = contentValue,
            onValueChange = onContentChanged,
            bottomPadding = 50.dp
        )
        UndoRedoControl(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp),
            canUndo = canUndo,
            canRedo = canRedo,
            onUndo = onUndo,
            onRedo = onRedo
        )
    }
}

@Preview
@Composable
fun TaskContentPreview() {
    TaskFlowTheme {
        TaskContent(
            state = AddEditTaskUiState(),
            onTitleChanged = {},
            onContentChanged = {},
            onStatusChanged = {},
            onPriorityChanged = {},
            onUndo = {},
            onRedo = {}
        )
    }
}