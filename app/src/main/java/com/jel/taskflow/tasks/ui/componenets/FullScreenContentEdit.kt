package com.jel.taskflow.tasks.ui.componenets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jel.taskflow.R
import com.jel.taskflow.tasks.model.AddEditTaskViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FullScreenContentEdit(
    viewModel: AddEditTaskViewModel,
    onNavigationBack: () -> Unit
) {
    val state = viewModel.uiState
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val isKeyboardVisible = WindowInsets.isImeVisible
    var hasKeyboardBeenOpened by remember { mutableStateOf(false) }

    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible) hasKeyboardBeenOpened = true
        else if (hasKeyboardBeenOpened) onNavigationBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            ScrollableTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                label = { Text(text = stringResource(R.string.task_content)) },
                value = state.content,
                onValueChange = viewModel::onContentChanged,
                bottomPadding = 50.dp
            )
            UndoRedoControl(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 5.dp),
                canUndo = state.canUndo,
                canRedo = state.canRedo,
                onUndo = { viewModel.revertChanges() },
                onRedo = { viewModel.revertChanges(forwards = true) }
            )
        }
    }
}