package com.tracker.subscription.data

import com.tracker.subscription.data.dao.SubscriptionEntity

fun SubscriptionEntity.toDomain(): Subscription {
    return Subscription(
        key = key,
        id = id.toString(),
        name = name,
        price = price,
        billingCycle = billingCycle,
        nextBillingDate = nextBillingDate,
        startDate = startDate,
        category = category,
        currency = currency,
        reminderEnabled = reminderEnabled,
        subscriptionType = subscriptionType,
        logoResId = logoResId
    )
}