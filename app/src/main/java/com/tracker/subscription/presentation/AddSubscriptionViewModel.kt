package com.tracker.subscription.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.subscription.Utility.calculateNextBillingDate
import com.tracker.subscription.data.Service
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.dao.SubscriptionEntity
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.data.toDomain
import kotlinx.coroutines.launch

class AddSubscriptionViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    val allServices = repository.getAllServices() // your full list

    var suggestions by mutableStateOf<List<Service>>(allServices)
        private set

    fun searchServices(query: String) {

        if (query.isBlank()) {
            suggestions = allServices   // 👈 show all by default
            return
        }

        val q = query.lowercase().replace(" ", "")

        suggestions = allServices.filter {
            it.name.lowercase().replace(" ", "").contains(q)
        }
    }
    fun saveSubscription(
        name: String,
        price: Double,
        currency: String,
        billingCycle: String,
        category: String,
        startDate: Long,
        reminderEnabled: Boolean,
        subscriptionType: String,
        logoId: Int?,
        key: String
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
            subscriptionType = subscriptionType,
            logoResId = logoId,
            key = key
        )

        viewModelScope.launch {

            repository.addSubscription(subscription)
        }
    }

    fun filterByCategory(category: String) {
        suggestions = if (category == "All") {
            allServices
        } else {
            allServices.filter { it.category == category }
        }
    }


    fun getServiceLogo(name: String): Service? {
        return repository.getExactService(name)
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
            subscriptionType = subscription.subscriptionType,
            logoResId = subscription.logoResId,
            key = subscription.key
        )
    }


    suspend fun getSubscription(id: Int): Subscription? {
        return repository
            .getSubscription(id)
            ?.toDomain()
    }
}