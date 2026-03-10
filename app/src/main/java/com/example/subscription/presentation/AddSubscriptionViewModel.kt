package com.example.subscription.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscription.Utility.calculateNextBillingDate
import com.example.subscription.data.Subscription
import com.example.subscription.data.dao.SubscriptionEntity
import com.example.subscription.data.repo.SubscriptionRepository
import com.example.subscription.data.toDomain
import com.example.subscription.screens.formatDate
import kotlinx.coroutines.flow.toList
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
        reminderEnabled: Boolean
    ) {

        val nextBillingDate =
            calculateNextBillingDate(startDate, billingCycle)

        val subscription = SubscriptionEntity(
            name = name,
            price = price,
            currency = currency,
            billingCycle = billingCycle,
            category = category,
            startDate = startDate,
            nextBillingDate = nextBillingDate,
            reminderEnabled = reminderEnabled
        )

        viewModelScope.launch {

            repository.addSubscription(subscription)
        }
    }

    fun addSubscription(subscription: Subscription) {

        viewModelScope.launch {

            repository.addSubscription(convertSubscriptionEntity(subscription))
        }
    }



    fun updateSubscription(subscription: Subscription) {

        viewModelScope.launch {
            repository.updateSubscription(convertSubscriptionEntity(subscription))
        }
    }

    fun convertSubscriptionEntity(subscription: Subscription): SubscriptionEntity {

        val nextBillingDate =
            calculateNextBillingDate(subscription.startDate, subscription.billingCycle)

        return SubscriptionEntity(
            id = subscription.id.toInt(),
            name = subscription.name,
            price = subscription.price,
            currency = subscription.currency,
            billingCycle = subscription.billingCycle,
            category = subscription.category,
            startDate = subscription.startDate,
            nextBillingDate = nextBillingDate,
            reminderEnabled = subscription.reminderEnabled
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