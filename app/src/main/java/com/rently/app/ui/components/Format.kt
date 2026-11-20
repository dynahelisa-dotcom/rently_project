package com.rently.app.ui.components

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

fun Double.mwk(): String = "MK " + NumberFormat.getIntegerInstance(Locale.US).format(roundToLong())

fun String.dateOnly(): String = take(10)
