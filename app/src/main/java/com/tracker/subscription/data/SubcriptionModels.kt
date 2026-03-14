package com.tracker.subscription.data

data class Subscription(
    val id: String,
    val name: String,
    val price: Double,
    val billingCycle: String,
    val startDate: Long,
    val nextBillingDate: Long,
    val currency: String,
    val category: String,
    val subscriptionType: String,   // NEW
    val reminderEnabled: Boolean
)

data class Renewal(
    val name: String,
    val price: Double,
    val daysLeft: Int,
    val subscriptionType: String,
)

data class DashboardData(
    val monthlySpend: Double,
    val upcomingRenewals: List<Renewal>,
    val subscriptions: List<Subscription>,
    val freeTrials: List<Renewal>
)