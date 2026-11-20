package com.rently.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

// One accent (indigo) + neutrals. Everything else is a tint of these.
val Indigo = Color(0xFF4F46E5)
val IndigoDeep = Color(0xFF3730A3)
val IndigoSoft = Color(0xFFE0E7FF)
val ChartMid = Color(0xFFA5B4FC)
val ChartLow = Color(0xFFCBD5E1)
val Ink = Color(0xFF0F172A)
val Slate = Color(0xFF64748B)
val Line = Color(0xFFE2E8F0)
val Canvas = Color(0xFFF6F7FB)

private val Scheme = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoSoft,
    onPrimaryContainer = IndigoDeep,
    background = Canvas,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEEF0F7),
    onSurfaceVariant = Slate,
    outline = Line,
    error = IndigoDeep, // keep the palette to one hue
    onError = Color.White,
)

private val AppTypography = Typography().copy(
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun RentlyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = AppTypography, shapes = AppShapes, content = content)
}
