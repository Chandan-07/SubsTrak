package com.example.subscription.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscription.data.DashboardData
import com.example.subscription.data.Renewal
import com.example.subscription.data.Subscription
import com.example.subscription.data.repo.SubscriptionRepository
import com.example.subscription.data.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DashboardViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<DashboardData?>(null)

    val uiState: StateFlow<DashboardData?> = _uiState

    init {
        observeSubscriptions()
    }

    private fun observeSubscriptions() {

        viewModelScope.launch {

            repository.getSubscriptions().collect { subs ->

                val monthlySpend =
                    subs.filter { it.billingCycle == "Monthly" }
                        .sumOf { it.price }

                val upcomingRenewals =
                    subs.sortedBy { it.nextBillingDate }
                        .take(3)
                        .map {

                            Renewal(
                                name = it.name,
                                price = it.price,
                                daysLeft = getDaysLeft(it.nextBillingDate)
                            )
                        }
                val subscriptionList =
                    subs.map { it.toDomain() }


                val dashboardData = DashboardData(
                    monthlySpend = monthlySpend,
                    upcomingRenewals = upcomingRenewals,
                    subscriptions = subscriptionList
                )

                Log.d("ASJHFKDS", "observeSubscriptions: "+dashboardData.subscriptions.size)
                _uiState.value = dashboardData
            }
        }
    }
    fun getDaysLeft(date: Long): Int {

        val diff =
            date - System.currentTimeMillis()

        return TimeUnit.MILLISECONDS
            .toDays(diff)
            .toInt()
    }

    fun deleteSubscription(subscription: Subscription) {

        viewModelScope.launch {

            repository.deleteSubscription(subscription.id.toInt())
        }
    }
}