package com.chargemap.compose.numberpicker

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NumberPickerDialog(value: Int,
                       showDialog: Boolean,
                       setShowDialog: (Boolean) -> Unit,
                       onValueChange: (Int) -> Unit,
                       onValueValidate: (Int) -> Unit) {
    var invalidText = remember { mutableStateOf(false) }
    if (showDialog) {
        Dialog(
            onDismissRequest = {
                setShowDialog(false)
            }
        ) {
            Surface(modifier = Modifier.width(200.dp),
                    color = MaterialTheme.colors.background,
                    shape = RoundedCornerShape(20.dp)
                    ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    )
                    TextField(
                        modifier = Modifier.padding(20.dp),
                        readOnly = false,
                        isError = invalidText.value,
                        value = if (value == -1) "" else "$value",
                        onValueChange = { value ->
                            if (value.isEmpty()) {
                                onValueChange(-1)
                            } else {
                                value.toIntOrNull()?.let {
                                    onValueChange(it)
                                    invalidText.value = it !in 1_000..1_000_000
                                }
                            }
                        },
                        placeholder = { Text("example: 8085") },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = MaterialTheme.colors.onSurface,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.padding(end = 10.dp).fillMaxWidth()
                    ) {
                        Button(onClick = {
                            setShowDialog(false)
                            onValueValidate(value)
                        },modifier = Modifier.padding(end = 20.dp)) {
                            Text(stringResource(R.string.validate))
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    )
                }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    label: (Int) -> String = {
        it.toString()
    },
    value: Int,
    onValueChange: (Int) -> Unit,
    dividersColor: Color = MaterialTheme.colors.primary,
    range: IntRange,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    val dialogPortValue = remember { mutableStateOf(value) }
    val (showDialog, setShowDialog) =  remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val numbersColumnHeight = 80.dp
    val halfNumbersColumnHeight = numbersColumnHeight / 2
    val halfNumbersColumnHeightPx = with(LocalDensity.current) { halfNumbersColumnHeight.toPx() }

    fun animatedStateValue(offset: Float): Int = value - (offset / halfNumbersColumnHeightPx).toInt()

    val animatedOffset = remember { Animatable(0f) }
        .apply {
            val offsetRange = remember(value, range) {
                val first = -(range.last - value) * halfNumbersColumnHeightPx
                val last = -(range.first - value) * halfNumbersColumnHeightPx
                first..last
            }
            updateBounds(offsetRange.start, offsetRange.endInclusive)
        }

    val coercedAnimatedOffset = animatedOffset.value % halfNumbersColumnHeightPx
    val animatedStateValue = animatedStateValue(animatedOffset.value)

    var dividersWidth by remember { mutableStateOf(0.dp) }
    val minimumAlpha = 0.3f
    val verticalMargin = 8.dp

    NumberPickerDialog(dialogPortValue.value, showDialog, setShowDialog, {
        dialogPortValue.value = it
    }, onValueChange)

    Layout(
        modifier = modifier
            .padding(vertical = numbersColumnHeight / 3 + verticalMargin * 2)
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { deltaY ->
                    coroutineScope.launch {
                        animatedOffset.snapTo(animatedOffset.value + deltaY)
                    }
                },
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        val endValue = animatedOffset.fling(
                            initialVelocity = velocity,
                            animationSpec = exponentialDecay(frictionMultiplier = 20f),
                            adjustTarget = { target ->
                                val coercedTarget = target % halfNumbersColumnHeightPx
                                val coercedAnchors = listOf(
                                    -halfNumbersColumnHeightPx,
                                    0f,
                                    halfNumbersColumnHeightPx
                                )
                                val coercedPoint =
                                    coercedAnchors.minByOrNull { kotlin.math.abs(it - coercedTarget) }!!
                                val base =
                                    halfNumbersColumnHeightPx * (target / halfNumbersColumnHeightPx).toInt()
                                coercedPoint + base
                            }
                        ).endState.value

                        onValueChange(animatedStateValue(endValue))
                        animatedOffset.snapTo(0f)
                    }
                }
            )
            .dialogOpenner(setShowDialog),
        content = {
            Box(
                modifier
                    .width(dividersWidth)
                    .height(2.dp)
                    .background(color = dividersColor)
                    .dialogOpenner(setShowDialog)
            )
            Box(
                modifier = Modifier
                    .padding(vertical = verticalMargin, horizontal = 20.dp)
                    .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) }
                    .dialogOpenner(setShowDialog)
            ) {
                val baseLabelModifier = Modifier.align(Alignment.Center)
                ProvideTextStyle(textStyle) {
                    if (range.contains(animatedStateValue - 1))
                        Label(
                            text = label(animatedStateValue - 1),
                            modifier = baseLabelModifier
                                .offset(y = -halfNumbersColumnHeight)
                                .alpha(
                                    maxOf(
                                        minimumAlpha,
                                        coercedAnimatedOffset / halfNumbersColumnHeightPx
                                    )
                                )
                                .dialogOpenner(setShowDialog)
                        )
                    Label(
                        text = label(animatedStateValue),
                        modifier = baseLabelModifier
                            .alpha(
                                (maxOf(
                                    minimumAlpha,
                                    1 - kotlin.math.abs(coercedAnimatedOffset) / halfNumbersColumnHeightPx
                                ))
                            )
                            .dialogOpenner(setShowDialog)
                    )
                    if (range.contains(animatedStateValue + 1))
                        Label(
                            text = label(animatedStateValue + 1),
                            modifier = baseLabelModifier
                                .offset(y = halfNumbersColumnHeight)
                                .alpha(
                                    maxOf(
                                        minimumAlpha,
                                        -coercedAnimatedOffset / halfNumbersColumnHeightPx
                                    )
                                )
                                .dialogOpenner(setShowDialog)
                        )
                }
            }
            Box(
                modifier
                    .width(dividersWidth)
                    .height(2.dp)
                    .background(color = dividersColor)
                    .dialogOpenner(setShowDialog)
            )
        }
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.map { measurable ->
            // Measure each children
            measurable.measure(constraints)
        }

        dividersWidth = placeables
            .drop(1)
            .first()
            .width
            .toDp()

        // Set the size of the layout as big as it can
        layout(dividersWidth.toPx().toInt(), placeables
            .sumOf {
                it.height
            }
        ) {
            // Track the y co-ord we have placed children up to
            var yPosition = 0

            // Place children in the parent layout
            placeables.forEach { placeable ->

                // Position item on the screen
                placeable.placeRelative(x = 0, y = yPosition)

                // Record the y co-ord placed up to
                yPosition += placeable.height
            }
        }
    }
}

@ExperimentalFoundationApi
private fun Modifier.dialogOpenner(setShowDialog: (Boolean) -> Unit): Modifier {
    return combinedClickable(
        enabled = true,
        onDoubleClick = {
            setShowDialog(true)
            println("Showing Dialog")
        },
        onLongClick = {
            setShowDialog(true)
            println("Showing Dialog")
        },
        onClick = {
            setShowDialog(true)
            println("Showing Dialog")
        }
    )
}

@Composable
private fun Label(text: String, modifier: Modifier) {
    Text(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onLongPress = {
                // FIXME: Empty to disable text selection
            })
        },
        text = text,
        textAlign = TextAlign.Center,
    )
}

private suspend fun Animatable<Float, AnimationVector1D>.fling(
    initialVelocity: Float,
    animationSpec: DecayAnimationSpec<Float>,
    adjustTarget: ((Float) -> Float)?,
    block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null,
): AnimationResult<Float, AnimationVector1D> {
    val targetValue = animationSpec.calculateTargetValue(value, initialVelocity)
    val adjustedTarget = adjustTarget?.invoke(targetValue)
    return if (adjustedTarget != null) {
        animateTo(
            targetValue = adjustedTarget,
            initialVelocity = initialVelocity,
            block = block
        )
    } else {
        animateDecay(
            initialVelocity = initialVelocity,
            animationSpec = animationSpec,
            block = block,
        )
    }
}