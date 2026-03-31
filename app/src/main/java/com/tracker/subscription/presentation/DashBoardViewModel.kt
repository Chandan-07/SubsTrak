package com.tracker.subscription.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.tracker.subscription.data.DashboardData
import com.tracker.subscription.data.Renewal
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.SubscriptionType
import com.tracker.subscription.data.dao.UserEntity
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
    val allServices = repository.getAllServices() // your full list

    val uiState: StateFlow<DashboardData?> = _uiState
    var currentUser by mutableStateOf<FirebaseUser?>(null)
        private set
    init {
        observeSubscriptions()
    }

    private fun observeSubscriptions() {

        viewModelScope.launch {

            repository.getSubscriptions().collect { subs ->

                val monthlySpend =
                    subs.filter { it.billingCycle == "Monthly" }
                        .sumOf { it.price }


                var currency = ""
               if(subs.isNotEmpty()) {
                   currency = subs[0].currency
               }

                val upcomingRenewals =
                    subs.sortedBy { it.nextBillingDate }
                    .filter { it.subscriptionType == SubscriptionType.PAID_SUBSCRIPTION.value }
                    .take(3)
                        .map {

                            Renewal(
                                name = it.name,
                                price = it.price,
                                daysLeft = getDaysLeft(it.nextBillingDate),
                                subscriptionType = it.subscriptionType,
                                logoResId = it.logoResId
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
                                subscriptionType = it.subscriptionType,
                                logoResId = it.logoResId,
                                packageName = allServices.filter { filter -> it.name == filter.name }.get(0).packageName
                            )
                        }


                Log.d("ASFSDF", "observeSubscriptions: "+freeTrialList.size+" - "+upcomingRenewals.size+" - "+subscriptionList.size)
                val dashboardData = DashboardData(
                    monthlySpend = monthlySpend,
                    currency = currency,
                    upcomingRenewals = upcomingRenewals,
                    subscriptions = subscriptionList,
                    freeTrials = freeTrialList
                )

                _uiState.value = dashboardData
            }
        }
    }



    fun setUser(user: FirebaseUser?) {
        currentUser = user
        user?.let {
            viewModelScope.launch {
                repository.saveUserDetails(UserEntity(
                    id = user.uid, name = user.displayName.toString(),
                    email = user.email.toString(),
                    phone = user.phoneNumber.toString(),
                   ))
            }
        }

    }

    fun getUser():  String{
        var name = "Guest"
        viewModelScope.launch {
            repository.getUserDetails()?.name?.let {
                name = it
            }
        }

        return  name
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