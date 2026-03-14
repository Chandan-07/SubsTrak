package com.tracker.subscription.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.subscription.data.DashboardData
import com.tracker.subscription.data.Renewal
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.SubscriptionType
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.data.toDomain
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
                    .filter { it.subscriptionType == SubscriptionType.PAID_SUBSCRIPTION.value }
                    .take(3)
                        .map {

                            Renewal(
                                name = it.name,
                                price = it.price,
                                daysLeft = getDaysLeft(it.nextBillingDate),
                                subscriptionType = it.subscriptionType
                            )
                        }

                val subscriptionList = subs.map { it.toDomain() }

                val freeTrialList =
                    subs.filter { it.subscriptionType == SubscriptionType.FREE_TRIAL.value }
                        .take(4)
                        .map {
                            Renewal(
                                name = it.name,
                                price = it.price,
                                daysLeft = getDaysLeft(it.nextBillingDate),
                                subscriptionType = it.subscriptionType
                            )
                        }


                Log.d("ASFSDF", "observeSubscriptions: "+freeTrialList.size+" - "+upcomingRenewals.size+" - "+subscriptionList.size)
                val dashboardData = DashboardData(
                    monthlySpend = monthlySpend,
                    upcomingRenewals = upcomingRenewals,
                    subscriptions = subscriptionList,
                    freeTrials = freeTrialList
                )

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