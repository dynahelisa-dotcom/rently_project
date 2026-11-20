package com.rently.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rently.app.data.RentStatus
import com.rently.app.ui.theme.ChartLow
import com.rently.app.ui.theme.ChartMid

val CardShape = RoundedCornerShape(22.dp)

/** White card with a soft shadow. Pass onClick to make the whole card tappable. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(padding),
            content = content,
        )
    }
}

@Composable
fun StatusBadge(status: RentStatus) {
    val c = MaterialTheme.colorScheme
    val (bg, fg, label) = when (status) {
        RentStatus.PAID -> Triple(c.primary, c.onPrimary, "Paid")
        RentStatus.OWING -> Triple(c.primaryContainer, c.onPrimaryContainer, "Owing")
        RentStatus.UNPAID -> Triple(c.surfaceVariant, c.onSurface, "Unpaid")
    }
    Box(Modifier.clip(CircleShape).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(label, color = fg, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Avatar(name: String, size: Dp = 46.dp) {
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Box(
        Modifier.size(size).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProgressBar(fraction: Float, track: Color, fill: Color, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(track)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).clip(CircleShape).background(fill))
    }
}

@Composable
fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = if (selected) c.primary else c.surface,
        shadowElevation = if (selected) 0.dp else 3.dp,
        modifier = Modifier.clip(CircleShape).clickable(onClick = onClick),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            color = if (selected) c.onPrimary else c.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun RoundIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
            Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        } else {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AppField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
) {
    val c = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        leadingIcon = icon?.let { ic -> @Composable { Icon(ic, contentDescription = null) } },
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.primary,
            unfocusedBorderColor = c.outline,
            focusedContainerColor = c.surface,
            unfocusedContainerColor = c.surface,
        ),
    )
}

/** Three-segment ring: paid (solid) · owing (mid tint) · unpaid (light grey). */
@Composable
fun DonutChart(paid: Int, owing: Int, unpaid: Int, modifier: Modifier = Modifier) {
    val total = (paid + owing + unpaid).coerceAtLeast(1)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(paid, owing, unpaid) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(800))
    }
    val track = MaterialTheme.colorScheme.surfaceVariant
    val solid = MaterialTheme.colorScheme.primary
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 20.dp.toPx()
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arc = Size(size.width - stroke, size.height - stroke)
            drawArc(track, 0f, 360f, false, topLeft, arc, style = Stroke(stroke))
            var start = -90f
            listOf(paid to solid, owing to ChartMid, unpaid to ChartLow).forEach { (count, color) ->
                val sweep = 360f * count / total * progress.value
                if (count > 0) {
                    drawArc(color, start, sweep, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Butt))
                }
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${paid + owing + unpaid}", style = MaterialTheme.typography.headlineMedium)
            Text("tenants", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun screenPadding(inner: PaddingValues): PaddingValues = PaddingValues(
    start = 20.dp,
    end = 20.dp,
    top = inner.calculateTopPadding() + 12.dp,
    bottom = inner.calculateBottomPadding() + 28.dp,
)
