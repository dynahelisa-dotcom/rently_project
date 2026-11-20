package com.rently.app.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

/** Everything the UI needs from the backend. Swap implementations without touching screens. */
interface RentRepository {
    suspend fun currentProfile(): LandlordProfile?
    suspend fun signIn(email: String, password: String): LandlordProfile
    suspend fun signOut()
    suspend fun tenants(): List<TenantRent>
    suspend fun payments(tenancyId: String): List<Payment>
    suspend fun recordPayment(tenancyId: String, amount: Double, method: String): Payment
}

class SupabaseRentRepository(private val client: SupabaseClient) : RentRepository {

    override suspend fun currentProfile(): LandlordProfile? {
        client.auth.awaitInitialization()
        val user = client.auth.currentUserOrNull() ?: return null
        val profile = client.from("profiles")
            .select { filter { eq("id", user.id) } }
            .decodeSingleOrNull<ProfileRow>() ?: return null
        if (profile.role != "landlord") {
            client.auth.signOut()
            error("This app version is for landlords only.")
        }
        val property = client.from("properties")
            .select { limit(1) }
            .decodeList<PropertyRow>().firstOrNull()
        return LandlordProfile(profile.fullName, property?.name ?: "My compound")
    }

    override suspend fun signIn(email: String, password: String): LandlordProfile {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return currentProfile() ?: error("No landlord profile found for this account.")
    }

    override suspend fun signOut() = client.auth.signOut()

    override suspend fun tenants(): List<TenantRent> =
        client.from("tenant_rent_status").select().decodeList()

    override suspend fun payments(tenancyId: String): List<Payment> =
        client.from("payments").select {
            filter { eq("tenancy_id", tenancyId) }
            order("paid_at", Order.DESCENDING)
        }.decodeList()

    override suspend fun recordPayment(tenancyId: String, amount: Double, method: String): Payment =
        client.from("payments")
            .insert(NewPayment(tenancyId, amount, method)) { select() }
            .decodeSingle()
}
