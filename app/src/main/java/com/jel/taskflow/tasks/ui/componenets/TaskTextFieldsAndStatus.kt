package com.jel.taskflow.tasks.ui.componenets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jel.taskflow.R
import com.jel.taskflow.tasks.model.enums.Priority
import com.jel.taskflow.tasks.model.enums.Status
import com.jel.taskflow.tasks.ui.AddEditTaskUiState
import com.jel.taskflow.ui.theme.TaskFlowTheme
import com.jel.taskflow.utils.toRelativeTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaskTextFieldsAndStatus(
    modifier: Modifier = Modifier,
    state: AddEditTaskUiState,
    onTitleChanged: (String) -> Unit,
    onOpenContentFullScreen: () -> Unit,
    onStatusChanged: (Status) -> Unit,
    onPriorityChanged: (Priority) -> Unit
) {

//    val imeState = rememberImeState()
//    val scrollState = rememberScrollState()
//
//    LaunchedEffect(key1 = imeState.value) {
//        if (imeState.value) {
//            scrollState.animateScrollTo(scrollState.maxValue)
//        }
//    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
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
        Box(
            modifier = Modifier.weight(1f)
        ) {
            ScrollableTextField(
                modifier = Modifier.fillMaxSize(),
                label = { Text(text = stringResource(R.string.task_content)) },
                value = state.content,
                onValueChange = onContentChanged,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                bottomPadding = 50.dp
            )
            UndoRedoControl(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 5.dp),
                canUndo = state.canUndo,
                canRedo = state.canRedo,
                onUndo = onUndo,
                onRedo = onRedo
            )
        }
        ScrollableTextField(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onOpenContentFullScreen)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val up = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (up != null) {
                            onOpenContentFullScreen()
                        }
                    }
                }
                .focusProperties { canFocus = false },
            readOnly = true,
            label = { Text(text = stringResource(R.string.task_content)) },
            value = state.content,
            onValueChange = { },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
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

@Preview
@Composable
fun TaskTextFieldsAndStatusPreview() {
    TaskFlowTheme {
        TaskTextFieldsAndStatus(
            state = AddEditTaskUiState(),
            onTitleChanged = {},
            onOpenContentFullScreen = {},
            onStatusChanged = {},
            onPriorityChanged = {}
        )
    }
}