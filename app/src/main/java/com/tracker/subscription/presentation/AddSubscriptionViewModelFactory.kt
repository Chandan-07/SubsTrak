package com.tracker.subscription.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tracker.subscription.data.repo.SubscriptionRepository

class AddSubscriptionViewModelFactory(
    private val repository: SubscriptionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AddSubscriptionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddSubscriptionViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}