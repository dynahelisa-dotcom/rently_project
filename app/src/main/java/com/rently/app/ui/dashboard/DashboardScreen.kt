package com.rently.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rently.app.data.LandlordProfile
import com.rently.app.data.RentStatus
import com.rently.app.ui.RentState
import com.rently.app.ui.components.AppCard
import com.rently.app.ui.components.Avatar
import com.rently.app.ui.components.CardShape
import com.rently.app.ui.components.DonutChart
import com.rently.app.ui.components.PrimaryButton
import com.rently.app.ui.components.ProgressBar
import com.rently.app.ui.components.RoundIconButton
import com.rently.app.ui.components.mwk
import com.rently.app.ui.components.screenPadding
import com.rently.app.ui.theme.ChartLow
import com.rently.app.ui.theme.ChartMid
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    profile: LandlordProfile?,
    state: RentState,
    onRefresh: () -> Unit,
    onOpenTenants: (filter: String) -> Unit,
    onOpenTenant: (id: String) -> Unit,
    onSignOut: () -> Unit,
) {
    LaunchedEffect(Unit) { onRefresh() }
    val c = MaterialTheme.colorScheme
    val s = state.summary
    val attention = state.tenants
        .filter { it.status != RentStatus.PAID }
        .sortedByDescending { it.balance }
        .take(3)

    Scaffold(containerColor = c.background) { pad ->
        LazyColumn(
            contentPadding = screenPadding(pad),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Hello, ${profile?.name?.substringBefore(" ") ?: "there"}", style = MaterialTheme.typography.headlineMedium)
                        Text(profile?.property ?: "", style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
                    }
                    RoundIconButton(Icons.AutoMirrored.Filled.ExitToApp, "Sign out", onSignOut)
                }
            }

            if (state.loading && state.tenants.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { CircularProgressIndicator() } }
            }

            if (state.error != null && state.tenants.isEmpty()) {
                item {
                    AppCard(onClick = onRefresh) {
                        Text(state.error, color = c.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap to retry", color = c.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (state.tenants.isNotEmpty()) {
                item {
                    Surface(Modifier.fillMaxWidth(), shape = CardShape, color = c.primary, shadowElevation = 10.dp) {
                        Column(Modifier.padding(22.dp)) {
                            Text("Collected this month", color = c.onPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(6.dp))
                            Text(s.collected.mwk(), color = c.onPrimary, style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(18.dp))
                            ProgressBar(s.fraction, track = c.onPrimary.copy(alpha = 0.25f), fill = c.onPrimary)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${(s.fraction * 100).roundToInt()}% of ${s.expected.mwk()} expected",
                                color = c.onPrimary.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                item {
                    AppCard {
                        Text("Payment status", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DonutChart(s.paid, s.owing, s.unpaid, Modifier.size(128.dp))
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                LegendRow(c.primary, "Paid", s.paid) { onOpenTenants("paid") }
                                LegendRow(ChartMid, "Owing", s.owing) { onOpenTenants("owing") }
                                LegendRow(ChartLow, "Unpaid", s.unpaid) { onOpenTenants("unpaid") }
                            }
                        }
                    }
                }

                if (attention.isNotEmpty()) {
                    item {
                        AppCard(padding = 0.dp) {
                            Text(
                                "Needs attention",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 6.dp),
                            )
                            attention.forEachIndexed { i, t ->
                                if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = c.outline)
                                Row(
                                    Modifier.fillMaxWidth().clickable { onOpenTenant(t.id) }.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Avatar(t.name, 40.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(t.name, fontWeight = FontWeight.SemiBold)
                                        Text("Room ${t.room}", style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
                                    }
                                    Text(t.balance.mwk(), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }

                item { PrimaryButton("View all tenants", onClick = { onOpenTenants("all") }) }
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String, count: Int, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 9.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        Text(label, Modifier.weight(1f), color = c.onSurface)
        Text("$count", fontWeight = FontWeight.Bold)
        Icon(Icons.Filled.KeyboardArrowRight, null, tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}
