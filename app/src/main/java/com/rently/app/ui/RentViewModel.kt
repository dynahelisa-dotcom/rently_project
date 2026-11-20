package com.rently.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rently.app.data.Payment
import com.rently.app.data.RentRepository
import com.rently.app.data.RentStatus
import com.rently.app.data.TenantRent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RentSummary(
    val paid: Int,
    val owing: Int,
    val unpaid: Int,
    val expected: Double,
    val collected: Double,
) {
    val total get() = paid + owing + unpaid
    val fraction: Float get() = if (expected <= 0) 0f else (collected / expected).toFloat().coerceIn(0f, 1f)
}

data class RentState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val tenants: List<TenantRent> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val receipt: Payment? = null,
    val error: String? = null,
) {
    val summary: RentSummary
        get() = RentSummary(
            paid = tenants.count { it.status == RentStatus.PAID },
            owing = tenants.count { it.status == RentStatus.OWING },
            unpaid = tenants.count { it.status == RentStatus.UNPAID },
            expected = tenants.sumOf { it.monthlyRent },
            collected = tenants.sumOf { it.paid },
        )
}

class RentViewModel(private val repo: RentRepository) : ViewModel() {
    private val _state = MutableStateFlow(RentState())
    val state: StateFlow<RentState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repo.tenants() }
                .onSuccess { list -> _state.update { it.copy(loading = false, tenants = list) } }
                .onFailure { _state.update { s -> s.copy(loading = false, error = "Couldn't load tenants. Pull to retry.") } }
        }
    }

    fun loadPayments(tenancyId: String) {
        viewModelScope.launch {
            _state.update { it.copy(payments = emptyList()) }
            runCatching { repo.payments(tenancyId) }
                .onSuccess { list -> _state.update { it.copy(payments = list) } }
        }
    }

    /** Records a payment, then re-reads tenants so balance and status update everywhere. */
    fun recordPayment(tenancyId: String, amount: Double, method: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            runCatching { repo.recordPayment(tenancyId, amount, method) }
                .onSuccess { receipt ->
                    val tenants = runCatching { repo.tenants() }.getOrDefault(_state.value.tenants)
                    val payments = runCatching { repo.payments(tenancyId) }.getOrDefault(_state.value.payments)
                    _state.update { it.copy(saving = false, tenants = tenants, payments = payments, receipt = receipt) }
                    onDone()
                }
                .onFailure { e ->
                    _state.update { it.copy(saving = false, error = e.message ?: "Couldn't record the payment.") }
                }
        }
    }

    fun dismissReceipt() = _state.update { it.copy(receipt = null) }
    fun clearError() = _state.update { it.copy(error = null) }
    fun reset() { _state.value = RentState() }
}
