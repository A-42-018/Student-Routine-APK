package com.alif.studentroutine.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

internal fun fixedColor(argb: Long): ColorProvider = ColorProvider(Color(argb))

internal object WidgetPalette {
    val primary = fixedColor(0xFF6C63FF)
    val onPrimary = fixedColor(0xFFFFFFFF)
    val onPrimaryMuted = fixedColor(0xBFFFFFFF)
    val surface = fixedColor(0xFFFFFFFF)
    val onSurface = fixedColor(0xFF1A1A2E)
    val textGray = fixedColor(0xFF8E8E93)
    val error = fixedColor(0xFFE74C3C)
    val secondary = fixedColor(0xFF00BFA6)
    val primaryText = fixedColor(0xFF6C63FF)
}
