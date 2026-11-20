package com.rently.app

import android.app.Application
import com.rently.app.data.DemoRentRepository
import com.rently.app.data.RentRepository
import com.rently.app.data.SupabaseRentRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

class RentApp : Application() {

    val isDemo: Boolean = BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_KEY.isBlank()

    val repo: RentRepository by lazy {
        if (isDemo) {
            DemoRentRepository()
        } else {
            SupabaseRentRepository(
                createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) {
                    install(Auth)
                    install(Postgrest)
                }
            )
        }
    }
}
