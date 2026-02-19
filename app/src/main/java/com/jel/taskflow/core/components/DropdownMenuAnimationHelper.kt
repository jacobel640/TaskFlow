package com.jel.taskflow.core.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> rememberDropdownSelectionState(
    selectedValue: T,
    items: List<T>
): DropdownSelectionState<T> {
    val density = LocalDensity.current
    return remember(items) { DropdownSelectionState(selectedValue, items, density) }.apply {
        UpdateSelected(selectedValue)
    }
}

class DropdownSelectionState<T>(
    initialSelected: T,
    private val items: List<T>,
    private val density: Density
) {
    val animatedY = Animatable(0f)
    var itemHeightPx by mutableFloatStateOf(0f)
    private val itemPositionsY = mutableStateMapOf<Int, Float>()
    private var initialJumpDone by mutableStateOf(false)
    private var currentIndex = items.indexOf(initialSelected)

    @Composable
    fun UpdateSelected(newValue: T) {
        val newIndex = items.indexOf(newValue)
        LaunchedEffect(newIndex, itemPositionsY[newIndex]) {
            val targetY = itemPositionsY[newIndex] ?: 0f
            if (targetY != 0f || newIndex == 0) {
                if (!initialJumpDone) {
                    animatedY.snapTo(targetY)
                    initialJumpDone = true
                } else {
                    animatedY.animateTo(targetY, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow))
                }
            }
        }
    }

    fun onItemLayout(index: Int, coordinates: LayoutCoordinates) {
        itemHeightPx = coordinates.size.height.toFloat()
        itemPositionsY[index] = coordinates.positionInParent().y
    }

    val offset: Dp @Composable get() = with(density) { animatedY.value.toDp() }
    val height: Dp @Composable get() = with(density) { itemHeightPx.toDp() }
    val isReady: Boolean get() = initialJumpDone
}

@Composable
fun <T> AnimatedSelector(
    state: DropdownSelectionState<T>,
    shape: Shape = RoundedCornerShape(8.dp),
    color: Color = MaterialTheme.colorScheme.primaryContainer
) {
    if (state.isReady) {
        Box(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .fillMaxWidth()
                .height(state.height)
                .offset(y = state.offset)
                .background(color = color, shape = shape)
        )
    }
}