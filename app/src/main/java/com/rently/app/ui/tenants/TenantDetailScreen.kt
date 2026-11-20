package com.rently.app.ui.tenants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.rently.app.data.Payment
import com.rently.app.data.TenantRent
import com.rently.app.ui.RentState
import com.rently.app.ui.components.AppCard
import com.rently.app.ui.components.AppField
import com.rently.app.ui.components.Avatar
import com.rently.app.ui.components.Chip
import com.rently.app.ui.components.PrimaryButton
import com.rently.app.ui.components.ProgressBar
import com.rently.app.ui.components.RoundIconButton
import com.rently.app.ui.components.StatusBadge
import com.rently.app.ui.components.dateOnly
import com.rently.app.ui.components.mwk
import com.rently.app.ui.components.screenPadding
import kotlin.math.roundToLong

@Composable
fun TenantDetailScreen(
    tenantId: String,
    state: RentState,
    onLoad: (String) -> Unit,
    onRecord: (amount: Double, method: String, onDone: () -> Unit) -> Unit,
    onDismissReceipt: () -> Unit,
    onClearError: () -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(tenantId) { onLoad(tenantId) }
    val tenant = state.tenants.firstOrNull { it.id == tenantId }
    val c = MaterialTheme.colorScheme
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(containerColor = c.background) { pad ->
        LazyColumn(
            contentPadding = screenPadding(pad),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back", onBack)
                    Spacer(Modifier.width(14.dp))
                    Text("Tenant", style = MaterialTheme.typography.headlineMedium)
                }
            }
            if (tenant == null) {
                item { Text("Tenant not found.", color = c.onSurfaceVariant) }
                return@LazyColumn
            }

            item {
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(tenant.name, 56.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(tenant.name, style = MaterialTheme.typography.titleLarge)
                            Text("Room ${tenant.room}", color = c.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                        StatusBadge(tenant.status)
                    }
                    if (tenant.phone.isNotBlank()) {
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Phone, null, tint = c.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(tenant.phone, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                AppCard(padding = 20.dp) {
                    Text("Balance this month", style = MaterialTheme.typography.labelLarge, color = c.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(tenant.balance.mwk(), style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(16.dp))
                    ProgressBar(
                        if (tenant.monthlyRent > 0) (tenant.paid / tenant.monthlyRent).toFloat() else 0f,
                        track = c.surfaceVariant,
                        fill = c.primary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${tenant.paid.mwk()} paid of ${tenant.monthlyRent.mwk()} · due on day ${tenant.dueDay}",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(20.dp))
                    PrimaryButton(
                        text = if (tenant.balance > 0) "Record payment" else "Fully paid",
                        enabled = tenant.balance > 0,
                        onClick = { onClearError(); showSheet = true },
                    )
                }
            }

            item {
                AppCard(padding = 0.dp) {
                    Text(
                        "Payment history",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 6.dp),
                    )
                    if (state.payments.isEmpty()) {
                        Text(
                            "No payments recorded yet.",
                            color = c.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                    state.payments.forEachIndexed { i, p ->
                        if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = c.outline)
                        PaymentRow(p)
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }

    if (showSheet && tenant != null) {
        PaymentSheet(
            tenant = tenant,
            saving = state.saving,
            serverError = state.error,
            onDismiss = { showSheet = false },
            onConfirm = { amount, method -> onRecord(amount, method) { showSheet = false } },
        )
    }

    state.receipt?.let { receipt ->
        ReceiptDialog(receipt, tenant, onDismissReceipt)
    }
}

@Composable
private fun PaymentRow(p: Payment) {
    val c = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(p.reference.ifBlank { "Payment" }, fontWeight = FontWeight.SemiBold)
            Text("${p.method} · ${p.paidAt.dateOnly()}", style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
        }
        Text(p.amount.mwk(), fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentSheet(
    tenant: TenantRent,
    saving: Boolean,
    serverError: String?,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit,
) {
    val c = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amount by remember { mutableStateOf(tenant.balance.roundToLong().toString()) }
    var method by remember { mutableStateOf("Cash") }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding().imePadding()) {
            Text("Record payment", style = MaterialTheme.typography.titleLarge)
            Text(
                "${tenant.name} · Room ${tenant.room} · balance ${tenant.balance.mwk()}",
                style = MaterialTheme.typography.bodySmall,
                color = c.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            AppField(amount, { amount = it.filter(Char::isDigit); error = null }, "Amount (MWK)", keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(16.dp))
            Text("Method", style = MaterialTheme.typography.labelLarge, color = c.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Cash", "Mobile money", "Bank").forEach { m -> Chip(m, method == m) { method = m } }
            }
            val message = error ?: serverError
            if (message != null) {
                Spacer(Modifier.height(12.dp))
                Text(message, color = c.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(22.dp))
            PrimaryButton("Confirm payment", loading = saving, onClick = {
                val value = amount.toDoubleOrNull()
                when {
                    value == null || value <= 0 -> error = "Enter an amount greater than zero."
                    value > tenant.balance -> error = "Amount can't be more than the balance (${tenant.balance.mwk()})."
                    else -> onConfirm(value, method)
                }
            })
        }
    }
}

@Composable
private fun ReceiptDialog(receipt: Payment, tenant: TenantRent?, onDismiss: () -> Unit) {
    val c = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = c.surface,
        title = { Text("Payment recorded") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReceiptLine("Receipt", receipt.reference)
                ReceiptLine("Tenant", tenant?.name ?: "-")
                ReceiptLine("Amount", receipt.amount.mwk())
                ReceiptLine("Method", receipt.method)
                ReceiptLine("Date", receipt.paidAt.dateOnly())
                if (tenant != null) ReceiptLine("New balance", tenant.balance.mwk())
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done", fontWeight = FontWeight.SemiBold) } },
    )
}

@Composable
private fun ReceiptLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
