package com.rently.app.data

import kotlinx.coroutines.delay
import java.time.LocalDate
import java.util.UUID

/** In-memory sample data so the UI runs before Supabase is configured. */
class DemoRentRepository : RentRepository {

    private var signedIn = false

    private val tenants = mutableListOf(
        TenantRent("t1", "Chikondi Banda", "0999 120 331", "A1", 150_000.0, 150_000.0, 5),
        TenantRent("t2", "Thoko Phiri", "0888 442 190", "A2", 120_000.0, 60_000.0, 5),
        TenantRent("t3", "Yamikani Mwale", "0991 774 028", "B1", 180_000.0, 0.0, 1),
        TenantRent("t4", "Mphatso Kalua", "0882 019 553", "B2", 100_000.0, 100_000.0, 10),
        TenantRent("t5", "Limbani Gondwe", "0995 310 876", "C1", 90_000.0, 30_000.0, 7),
        TenantRent("t6", "Dalitso Nyirenda", "0884 602 447", "C2", 160_000.0, 0.0, 1),
    )

    private val payments = mutableMapOf(
        "t1" to mutableListOf(demoPayment("t1", 150_000.0, "Mobile money")),
        "t2" to mutableListOf(demoPayment("t2", 60_000.0, "Cash")),
        "t4" to mutableListOf(demoPayment("t4", 100_000.0, "Bank")),
        "t5" to mutableListOf(demoPayment("t5", 30_000.0, "Mobile money")),
    )

    private fun demoPayment(tenancyId: String, amount: Double, method: String) = Payment(
        id = UUID.randomUUID().toString(),
        tenancyId = tenancyId,
        amount = amount,
        method = method,
        reference = "RCP-" + UUID.randomUUID().toString().take(8).uppercase(),
        paidAt = LocalDate.now().toString(),
    )

    private val profile = LandlordProfile("Demo Landlord", "Sunrise Court")

    override suspend fun currentProfile(): LandlordProfile? = if (signedIn) profile else null

    override suspend fun signIn(email: String, password: String): LandlordProfile {
        delay(600)
        signedIn = true
        return profile
    }

    override suspend fun signOut() { signedIn = false }

    override suspend fun tenants(): List<TenantRent> { delay(350); return tenants.toList() }

    override suspend fun payments(tenancyId: String): List<Payment> =
        payments[tenancyId].orEmpty().reversed()

    override suspend fun recordPayment(tenancyId: String, amount: Double, method: String): Payment {
        delay(500)
        val i = tenants.indexOfFirst { it.id == tenancyId }
        require(i >= 0) { "Tenant not found" }
        tenants[i] = tenants[i].copy(paid = tenants[i].paid + amount)
        val p = demoPayment(tenancyId, amount, method)
        payments.getOrPut(tenancyId) { mutableListOf() }.add(p)
        return p
    }
}
