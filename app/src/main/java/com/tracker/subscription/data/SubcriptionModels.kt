package com.tracker.subscription.data

import com.tracker.subscription.R

data class Subscription(
    val key: String,
    val id: String,
    val name: String,
    val price: Double,
    val billingCycle: String,
    val startDate: Long,
    val nextBillingDate: Long,
    val currency: String,
    val category: String,
    val subscriptionType: String,   // NEW
    val reminderEnabled: Boolean,
    val reminderDaysBefore: Int = 1,
    val logoResId: Int? = R.drawable.empty,
    val packageName: String? = ""
)

data class Renewal(
    val id: String,
    val key: String,
    val name: String,
    val price: Double,
    val daysLeft: Int,
    val currency: String,
    val subscriptionType: String,
    val logoResId: Int? = null,
    val nextBillingDate: Long,
    val packageName: String? = null
)

data class DashboardData(
    val monthlySpend: Double,
    val currency: String,
    val upcomingRenewals: List<Renewal>,
    val subscriptions: List<Subscription>,
    val freeTrials: List<Renewal>,
    val user : AuthUser? = null,
    val isLoggedIn: Boolean = false,
    val smsSuggestions: List<ParsedSubscription> = emptyList()
)

data class Category(
    val name: String,
    val emoji: String
)

data class Option(
    val name: String,
    val emoji: String
)