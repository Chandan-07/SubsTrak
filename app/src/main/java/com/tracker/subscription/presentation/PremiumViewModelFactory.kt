package com.tracker.subscription.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tracker.subscription.data.repo.BillingRepository
import com.tracker.subscription.data.repo.SubscriptionRepository

class PremiumViewModelFactory(
    private val repository: BillingRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(PremiumViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return PremiumViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}