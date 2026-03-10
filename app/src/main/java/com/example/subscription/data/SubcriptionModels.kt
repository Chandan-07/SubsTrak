package com.example.subscription.data

import android.util.Log

data class Subscription(
    val id: String,
    val name: String,
    val price: Double,
    val billingCycle: String,
    val startDate: Long,
    val nextBillingDate: Long,
    val currency: String,
    val category: String,
    val reminderEnabled: Boolean
)

data class Renewal(
    val name: String,
    val price: Double,
    val daysLeft: Int
)

data class DashboardData(
    val monthlySpend: Double,
    val upcomingRenewals: List<Renewal>,
    val subscriptions: List<Subscription>
)