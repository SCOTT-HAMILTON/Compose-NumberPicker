package com.chargemap.compose.numberpicker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

interface Hours {
    val hours: Int
    val minutes: Int
}

data class FullHours(
    override val hours: Int,
    override val minutes: Int,
) : Hours

data class AMPMHours(
    override val hours: Int,
    override val minutes: Int,
    val dayTime: DayTime
) : Hours {
    enum class DayTime {
        AM,
        PM;
    }
}

@ExperimentalFoundationApi
@Composable
fun HoursNumberPicker(
    modifier: Modifier = Modifier,
    value: Hours,
    hoursDivider: (@Composable () -> Unit)? = null,
    minutesDivider: (@Composable () -> Unit)? = null,
    onValueChange: (Hours) -> Unit,
    dividersColor: Color = MaterialTheme.colors.primary,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    when (value) {
        is FullHours ->
            FullHoursNumberPicker(
                modifier = modifier,
                value = value,
                hoursRange = (0..23),
                hoursDivider = hoursDivider,
                minutesDivider = minutesDivider,
                onValueChange = onValueChange,
                dividersColor = dividersColor,
                textStyle = textStyle,
            )
        is AMPMHours ->
            AMPMHoursNumberPicker(
                modifier = modifier,
                value = value,
                hoursRange = (1..12),
                hoursDivider = hoursDivider,
                minutesDivider = minutesDivider,
                onValueChange = onValueChange,
                dividersColor = dividersColor,
                textStyle = textStyle,
            )
    }
}

@ExperimentalFoundationApi
@Composable
fun FullHoursNumberPicker(
    modifier: Modifier = Modifier,
    value: FullHours,
    hoursRange: IntRange,
    hoursDivider: (@Composable () -> Unit)? = null,
    minutesDivider: (@Composable () -> Unit)? = null,
    onValueChange: (Hours) -> Unit,
    dividersColor: Color = MaterialTheme.colors.primary,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberPicker(
            modifier = Modifier.weight(1f),
            value = value.hours,
            onValueChange = {
                onValueChange(value.copy(hours = it))
            },
            dividersColor = dividersColor,
            textStyle = textStyle,
            range = hoursRange
        )

        hoursDivider?.invoke()

        NumberPicker(
            modifier = Modifier.weight(1f),
            value = value.minutes,
            onValueChange = {
                onValueChange(value.copy(minutes = it))
            },
            dividersColor = dividersColor,
            textStyle = textStyle,
            range = (0..59)
        )

        minutesDivider?.invoke()
    }
}

@ExperimentalFoundationApi
@Composable
fun AMPMHoursNumberPicker(
    modifier: Modifier = Modifier,
    value: AMPMHours,
    hoursRange: IntRange,
    hoursDivider: (@Composable () -> Unit)? = null,
    minutesDivider: (@Composable () -> Unit)? = null,
    onValueChange: (Hours) -> Unit,
    dividersColor: Color = MaterialTheme.colors.primary,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberPicker(
            modifier = Modifier.weight(1f),
            value = value.hours,
            onValueChange = {
                onValueChange(value.copy(hours = it))
            },
            dividersColor = dividersColor,
            textStyle = textStyle,
            range = hoursRange
        )

        hoursDivider?.invoke()

        NumberPicker(
            modifier = Modifier.weight(1f),
            value = value.minutes,
            onValueChange = {
                onValueChange(value.copy(minutes = it))
            },
            dividersColor = dividersColor,
            textStyle = textStyle,
            range = (0..59)
        )

        minutesDivider?.invoke()

        NumberPicker(
            value = when (value.dayTime) {
                AMPMHours.DayTime.AM -> 0
                else -> 1
            },
            label = {
                when (it) {
                    0 -> "AM"
                    else -> "PM"
                }
            },
            onValueChange = {
                onValueChange(
                    value.copy(
                        dayTime = when (it) {
                            0 -> AMPMHours.DayTime.AM
                            else -> AMPMHours.DayTime.PM
                        }
                    )
                )
            },
            dividersColor = dividersColor,
            textStyle = textStyle,
            range = (0..1)
        )
    }
}