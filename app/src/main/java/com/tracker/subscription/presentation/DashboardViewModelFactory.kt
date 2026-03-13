package com.tracker.subscription.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tracker.subscription.data.repo.SubscriptionRepository

class DashboardViewModelFactory(
    private val repository: SubscriptionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}