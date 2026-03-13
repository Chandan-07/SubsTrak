package com.tracker.subscription.data.repo

import android.content.Context
import androidx.work.WorkManager
import com.tracker.subscription.ReminderScheduler
import com.tracker.subscription.data.dao.SubscriptionDao
import com.tracker.subscription.data.dao.SubscriptionEntity

class SubscriptionRepository(
    private val dao: SubscriptionDao,
    private val context: Context
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
        val id = dao.insert(subscription)   // capture generated ID
        if (subscription.reminderEnabled) {
            ReminderScheduler.scheduleReminder(
                context,
                id.toInt(),
                subscription.nextBillingDate
            )
        }
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        dao.update(subscription)

        // cancel old reminder
        ReminderScheduler.cancelReminder(context, subscription.id)


        // schedule new reminder if enabled
        if (subscription.reminderEnabled) {
            ReminderScheduler.scheduleReminder(
                context,
                subscription.id,
                subscription.nextBillingDate
            )
        }
    }

    suspend fun getSubscription(id: Int): SubscriptionEntity? {
        return dao.getSubscription(id)
    }

    suspend fun deleteSubscription(id: Int) {
        dao.deleteById(id)
        WorkManager.getInstance(context)
            .cancelUniqueWork("subscription_${id}")
    }


}