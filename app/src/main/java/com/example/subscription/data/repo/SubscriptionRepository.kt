package com.example.subscription.data.repo

import com.example.subscription.data.DashboardData
import com.example.subscription.data.Renewal
import com.example.subscription.data.Subscription
import com.example.subscription.data.dao.SubscriptionDao
import com.example.subscription.data.dao.SubscriptionEntity

class SubscriptionRepository(
    private val dao: SubscriptionDao
) {

    fun getSubscriptions() =
        dao.getSubscriptions()

//    fun getDashboardData(): DashboardData {
//
//        val subscriptions = listOf(
//            Subscription("1","Netflix",649.0,"Monthly",System.currentTimeMillis(), ),
//            Subscription("2","Spotify",119.0,"Monthly",System.currentTimeMillis()),
//            Subscription("3","Amazon Prime",299.0,"Yearly",System.currentTimeMillis()),
//            Subscription("4","Gym",1200.0,"Monthly",System.currentTimeMillis())
//        )
//
//        val renewals = listOf(
//            Renewal("Netflix",649.0,2),
//            Renewal("Spotify",119.0,5),
//            Renewal("Amazon Prime",299.0,10)
//        )
//
//        val monthlySpend = subscriptions
//            .filter { it.billingCycle == "Monthly" }
//            .sumOf { it.price }
//
//        return DashboardData(
//            monthlySpend = monthlySpend,
//            upcomingRenewals = renewals,
//            subscriptions = subscriptions
//        )
//    }

    suspend fun addSubscription(subscription: SubscriptionEntity) {
        dao.insert(subscription)
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        dao.update(subscription)
    }

    suspend fun getSubscription(id: Int): SubscriptionEntity? {
        return dao.getSubscription(id)
    }

    suspend fun deleteSubscription(id: Int) {
        dao.deleteById(id)
    }
}