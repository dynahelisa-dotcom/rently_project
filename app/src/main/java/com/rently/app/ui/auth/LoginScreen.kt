package com.rently.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rently.app.ui.AuthState
import com.rently.app.ui.components.AppCard
import com.rently.app.ui.components.AppField
import com.rently.app.ui.components.PrimaryButton

@Composable
fun LoginScreen(
    state: AuthState,
    isDemo: Boolean,
    onSignIn: (email: String, password: String) -> Unit,
    onLoggedIn: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val c = MaterialTheme.colorScheme

    LaunchedEffect(state.profile) { if (state.profile != null) onLoggedIn() }

    Box(Modifier.fillMaxSize().background(c.background).systemBarsPadding().imePadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = c.primary, shadowElevation = 10.dp) {
                Icon(Icons.Filled.Home, null, tint = c.onPrimary, modifier = Modifier.padding(16.dp).size(30.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("Welcome back", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                "Track rent and manage your compound in one place.",
                style = MaterialTheme.typography.bodyLarge,
                color = c.onSurfaceVariant,
            )
            Spacer(Modifier.height(28.dp))

            AppCard(padding = 20.dp) {
                AppField(email, { email = it }, "Email", icon = Icons.Filled.Email, keyboardType = KeyboardType.Email)
                Spacer(Modifier.height(14.dp))
                AppField(password, { password = it }, "Password", icon = Icons.Filled.Lock, password = true)

                if (state.error != null) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier.fillMaxWidth()
                            .background(c.primaryContainer, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Warning, null, tint = c.onPrimaryContainer, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(state.error, style = MaterialTheme.typography.bodySmall, color = c.onPrimaryContainer)
                    }
                }
                Spacer(Modifier.height(20.dp))
                PrimaryButton("Sign in", onClick = { onSignIn(email, password) }, loading = state.loading)
            }

            if (isDemo) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Demo mode: use any email and a 6+ character password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
