package com.rently.app.ui.tenants

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rently.app.data.RentStatus
import com.rently.app.ui.RentState
import com.rently.app.ui.components.AppCard
import com.rently.app.ui.components.AppField
import com.rently.app.ui.components.Avatar
import com.rently.app.ui.components.Chip
import com.rently.app.ui.components.RoundIconButton
import com.rently.app.ui.components.StatusBadge
import com.rently.app.ui.components.mwk
import com.rently.app.ui.components.screenPadding

@Composable
fun TenantsScreen(
    state: RentState,
    initialFilter: String,
    onBack: () -> Unit,
    onOpenTenant: (String) -> Unit,
) {
    var filter by rememberSaveable { mutableStateOf(initialFilter) }
    var query by rememberSaveable { mutableStateOf("") }
    val c = MaterialTheme.colorScheme

    fun count(s: RentStatus) = state.tenants.count { it.status == s }
    val shown = state.tenants
        .filter { filter == "all" || it.status.name.lowercase() == filter }
        .filter { query.isBlank() || it.name.contains(query, true) || it.room.contains(query, true) }
        .sortedBy { it.name }

    Scaffold(containerColor = c.background) { pad ->
        LazyColumn(
            contentPadding = screenPadding(pad),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Tenants", style = MaterialTheme.typography.headlineMedium)
                        Text("${state.tenants.size} in your compound", style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
                    }
                }
            }
            item { AppField(query, { query = it }, "Search name or room", icon = Icons.Filled.Search) }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Chip("All ${state.tenants.size}", filter == "all") { filter = "all" }
                    Chip("Paid ${count(RentStatus.PAID)}", filter == "paid") { filter = "paid" }
                    Chip("Owing ${count(RentStatus.OWING)}", filter == "owing") { filter = "owing" }
                    Chip("Unpaid ${count(RentStatus.UNPAID)}", filter == "unpaid") { filter = "unpaid" }
                }
            }
            if (shown.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                        Text("No tenants match.", color = c.onSurfaceVariant)
                    }
                }
            }
            items(shown, key = { it.id }) { t ->
                AppCard(onClick = { onOpenTenant(t.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(t.name)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            Text("Room ${t.room} · due day ${t.dueDay}", style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            StatusBadge(t.status)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (t.balance > 0) t.balance.mwk() else "Settled",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
