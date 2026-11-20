package com.rently.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Paid = nothing left to pay · Owing = part-paid · Unpaid = nothing paid yet this month. */
enum class RentStatus { PAID, OWING, UNPAID }

/** One row of the `tenant_rent_status` view: a tenancy plus what was paid this month. */
@Serializable
data class TenantRent(
    @SerialName("tenancy_id") val id: String,
    @SerialName("tenant_name") val name: String,
    val phone: String = "",
    @SerialName("room_label") val room: String,
    @SerialName("monthly_rent") val monthlyRent: Double,
    @SerialName("paid_amount") val paid: Double = 0.0,
    @SerialName("due_day") val dueDay: Int = 1,
) {
    val balance: Double get() = (monthlyRent - paid).coerceAtLeast(0.0)
    val status: RentStatus
        get() = when {
            balance <= 0.0 -> RentStatus.PAID
            paid > 0.0 -> RentStatus.OWING
            else -> RentStatus.UNPAID
        }
}

@Serializable
data class Payment(
    val id: String = "",
    @SerialName("tenancy_id") val tenancyId: String = "",
    val amount: Double,
    val method: String,
    val reference: String = "",
    @SerialName("paid_at") val paidAt: String = "",
)

@Serializable
data class NewPayment(
    @SerialName("tenancy_id") val tenancyId: String,
    val amount: Double,
    val method: String,
)

@Serializable
data class ProfileRow(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val role: String,
)

@Serializable
data class PropertyRow(val name: String)

data class LandlordProfile(val name: String, val property: String)
