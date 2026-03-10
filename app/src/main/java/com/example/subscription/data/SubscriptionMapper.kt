package com.example.subscription.data

import com.example.subscription.data.dao.SubscriptionEntity
import com.example.subscription.screens.formatDate

fun SubscriptionEntity.toDomain(): Subscription {
    return Subscription(
        id = id.toString(),
        name = name,
        price = price,
        billingCycle = billingCycle,
        nextBillingDate = nextBillingDate,
        startDate = startDate,
        category = category,
        currency = currency,
        reminderEnabled = reminderEnabled
    )
}