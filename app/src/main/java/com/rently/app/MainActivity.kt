package com.rently.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rently.app.ui.AppNav
import com.rently.app.ui.AuthViewModel
import com.rently.app.ui.RentViewModel
import com.rently.app.ui.theme.RentlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        val app = application as RentApp
        setContent {
            RentlyTheme {
                val authVm: AuthViewModel = viewModel(factory = viewModelFactory { initializer { AuthViewModel(app.repo) } })
                val rentVm: RentViewModel = viewModel(factory = viewModelFactory { initializer { RentViewModel(app.repo) } })
                AppNav(authVm, rentVm, app.isDemo)
            }
        }
    }
}
