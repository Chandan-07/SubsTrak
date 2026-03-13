package com.tracker.subscription.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.subscription.Utility.calculateNextBillingDate
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.dao.SubscriptionEntity
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.data.toDomain
import kotlinx.coroutines.launch

class AddSubscriptionViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    fun saveSubscription(
        name: String,
        price: Double,
        currency: String,
        billingCycle: String,
        category: String,
        startDate: Long,
        reminderEnabled: Boolean,
        subscriptionType: String
    ) {

        val nextBillingDate =
            calculateNextBillingDate(startDate, billingCycle, subscriptionType)

        val subscription = SubscriptionEntity(
            name = name,
            price = price,
            currency = currency,
            billingCycle = billingCycle,
            category = category,
            startDate = startDate,
            nextBillingDate = nextBillingDate,
            reminderEnabled = reminderEnabled,
            subscriptionType = subscriptionType
        )

        viewModelScope.launch {

            repository.addSubscription(subscription)
        }
    }



    fun updateSubscription(subscription: Subscription) {

        viewModelScope.launch {
            repository.updateSubscription(convertSubscriptionEntity(subscription))
        }
    }

    fun convertSubscriptionEntity(subscription: Subscription): SubscriptionEntity {

        val nextBillingDate =
            calculateNextBillingDate(subscription.startDate, subscription.billingCycle, subscription.subscriptionType)

        return SubscriptionEntity(
            id = subscription.id.toInt(),
            name = subscription.name,
            price = subscription.price,
            currency = subscription.currency,
            billingCycle = subscription.billingCycle,
            category = subscription.category,
            startDate = subscription.startDate,
            nextBillingDate = nextBillingDate,
            reminderEnabled = subscription.reminderEnabled,
            subscriptionType = subscription.subscriptionType
        )
    }


    suspend fun getSubscription(id: Int): Subscription? {
        Log.d("ASDNKJS", "getSubscription: "+repository
            .getSubscription(id))
        return repository
            .getSubscription(id)
            ?.toDomain()
    }
}