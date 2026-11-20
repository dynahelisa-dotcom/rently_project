package com.rently.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rently.app.data.LandlordProfile
import com.rently.app.data.RentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(
    val checking: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null,
    val profile: LandlordProfile? = null,
)

class AuthViewModel(private val repo: RentRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = runCatching { repo.currentProfile() }.getOrNull()
            _state.update { it.copy(checking = false, profile = existing) }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _state.update { it.copy(error = "Enter your email and a password of at least 6 characters.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repo.signIn(email.trim(), password) }
                .onSuccess { p -> _state.update { it.copy(loading = false, profile = p) } }
                .onFailure { e ->
                    _state.update {
                        it.copy(loading = false, error = e.message?.takeIf { m -> m.contains("landlord") }
                            ?: "We couldn't sign you in. Check your details and connection.")
                    }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { repo.signOut() }
            _state.update { it.copy(profile = null, error = null) }
        }
    }
}
