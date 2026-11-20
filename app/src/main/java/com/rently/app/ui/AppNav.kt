package com.rently.app.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rently.app.ui.auth.LoginScreen
import com.rently.app.ui.dashboard.DashboardScreen
import com.rently.app.ui.tenants.TenantDetailScreen
import com.rently.app.ui.tenants.TenantsScreen

private object Route {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val TENANTS = "tenants/{filter}"
    const val TENANT = "tenant/{id}"
    fun tenants(filter: String) = "tenants/$filter"
    fun tenant(id: String) = "tenant/$id"
}

/**
 * login -> dashboard -> tenants (filtered) -> tenant detail + record payment
 */
@Composable
fun AppNav(authVm: AuthViewModel, rentVm: RentViewModel, isDemo: Boolean) {
    val nav = rememberNavController()
    val auth by authVm.state.collectAsStateWithLifecycle()
    val rent by rentVm.state.collectAsStateWithLifecycle()

    if (auth.checking) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }
    val start = remember { if (auth.profile != null) Route.DASHBOARD else Route.LOGIN }

    NavHost(
        navController = nav,
        startDestination = start,
        enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 10 } },
        exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(220)) { it / 10 } },
    ) {
        composable(Route.LOGIN) {
            LoginScreen(
                state = auth,
                isDemo = isDemo,
                onSignIn = authVm::signIn,
                onLoggedIn = {
                    nav.navigate(Route.DASHBOARD) { popUpTo(Route.LOGIN) { inclusive = true } }
                },
            )
        }
        composable(Route.DASHBOARD) {
            DashboardScreen(
                profile = auth.profile,
                state = rent,
                onRefresh = rentVm::refresh,
                onOpenTenants = { nav.navigate(Route.tenants(it)) },
                onOpenTenant = { nav.navigate(Route.tenant(it)) },
                onSignOut = {
                    authVm.signOut()
                    rentVm.reset()
                    nav.navigate(Route.LOGIN) { popUpTo(0) }
                },
            )
        }
        composable(
            Route.TENANTS,
            arguments = listOf(navArgument("filter") { type = NavType.StringType }),
        ) { entry ->
            TenantsScreen(
                state = rent,
                initialFilter = entry.arguments?.getString("filter") ?: "all",
                onBack = { nav.popBackStack() },
                onOpenTenant = { nav.navigate(Route.tenant(it)) },
            )
        }
        composable(
            Route.TENANT,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            TenantDetailScreen(
                tenantId = id,
                state = rent,
                onLoad = rentVm::loadPayments,
                onRecord = { amount, method, done -> rentVm.recordPayment(id, amount, method, done) },
                onDismissReceipt = rentVm::dismissReceipt,
                onClearError = rentVm::clearError,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
