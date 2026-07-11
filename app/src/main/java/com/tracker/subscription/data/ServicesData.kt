package com.tracker.subscription.data

import com.android.billingclient.api.ProductDetails

data class Service(
    val key: String,        // 👈 NEW (stable)
    val name: String,
    val logo: Int,
    val packageName: String,
    val category: String,
    val prices: List<SubscriptionPrice>
)

data class SubscriptionPrice(
    val countryCode: String,
    val currency: String,
    val monthlyPrice: Double?= null,
    val yearlyPrice: Double? = null
)
data class AuthUser(
    val uid: String,
    val name: String?,
    val email: String?,
    val photo: String?,
    val isPremium: Boolean = false
)

data class ParsedSubscription(
    val service: String,
    val amount: Double,
    val date: Long,
    val currency: String = "₹"
)

data class Sms(
    val body: String,
    val date: Long
)

data class PlanUi(
    val title: String,
    val productDetails: ProductDetails,
    val price: String,
    val isYearly: Boolean
)
