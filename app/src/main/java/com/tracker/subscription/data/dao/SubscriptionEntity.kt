package com.tracker.subscription.data.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val price: Double,

    val currency: String,

    val billingCycle: String,

    val category: String,

    val subscriptionType: String,   // NEW

    val startDate: Long,

    val nextBillingDate: Long,

    val reminderEnabled: Boolean,

    val reminderDaysBefore: Int = 1,

    val logoResId: Int? = null,

    val key: String
)