package com.jel.taskflow.tasks.ui.componenets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScrollableTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    bottomPadding: Dp = 0.dp
) {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isFocused by actualInteractionSource.collectIsFocusedAsState()

    val textColor = textStyle.color.takeOrElse {
        colors.textColor(enabled, isError, isFocused)
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    // auto scroll handling...
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val ime = WindowInsets.ime
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val currentSelection by rememberUpdatedState(value.selection)
    val currentLayout by rememberUpdatedState(textLayoutResult)

    suspend fun scrollCursorIntoView() {
        val layout = currentLayout ?: return

        val cursorRect = if (currentSelection.max >= layout.layoutInput.text.length) {
            layout.getCursorRect(layout.layoutInput.text.length)
        } else layout.getCursorRect(currentSelection.max)
        val paddingPx = with(density) { bottomPadding.toPx() }
        val targetRect = Rect(
            left = cursorRect.left,
            top = cursorRect.top,
            right = cursorRect.right,
            bottom = cursorRect.bottom + paddingPx
        )
        bringIntoViewRequester.bringIntoView(targetRect)
    }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            snapshotFlow {
                Pair(ime.getBottom(density), currentSelection)
            }.collectLatest {
                scrollCursorIntoView()
            }
        }
    }
    // ____________


    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            modifier = modifier.defaultMinSize(
                minWidth = TextFieldDefaults.MinWidth,
                minHeight = TextFieldDefaults.MinHeight,
            ),
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            cursorBrush = SolidColor(colors.cursorColor(isError)),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = actualInteractionSource,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            onTextLayout = { textLayoutResult = it },
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = value.text,
                    visualTransformation = visualTransformation,
                    innerTextField = {
                        Column(modifier = Modifier.verticalScroll(scrollState)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bringIntoViewRequester(bringIntoViewRequester)
                            ) {
                                innerTextField()
                            }
                            Spacer(modifier = Modifier.height(bottomPadding))
                        }
                    },
                    placeholder = placeholder,
                    label = label,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    shape = shape,
                    singleLine = singleLine,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = actualInteractionSource,
                    colors = colors
                )
            }
        )
    }
}