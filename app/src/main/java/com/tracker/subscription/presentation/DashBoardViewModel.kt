package com.tracker.subscription.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.subscription.Utility.getDaysLeft
import com.tracker.subscription.data.AuthUser
import com.tracker.subscription.data.DashboardData
import com.tracker.subscription.data.ParsedSubscription
import com.tracker.subscription.data.Renewal
import com.tracker.subscription.data.Service
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.SubscriptionType
import com.tracker.subscription.data.dao.SmsDataSource
import com.tracker.subscription.data.dao.UserEntity
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.data.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class DashboardViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<DashboardData?>(null)
    val allServices = repository.getAllServices() // your full list
    var isSigningIn by mutableStateOf(false)
        private set

    fun setLoading(value: Boolean) {
        isSigningIn = value
    }

    private val _smsSyncState =
        MutableStateFlow<List<ParsedSubscription>>(arrayListOf())

    val uiState: StateFlow<DashboardData?> = _uiState
    val smsSyncState: StateFlow<List<ParsedSubscription>> = _smsSyncState
    var currentUser by mutableStateOf<AuthUser?>(null)
        private set

    private val _isLoadingSMS = MutableStateFlow(false)
    val isLoadingSMS = _isLoadingSMS.asStateFlow()

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
                        .map {

                            Renewal(
                                name = it.name,
                                price = it.price,
                                daysLeft = getDaysLeft(it.nextBillingDate),
                                subscriptionType = it.subscriptionType,
                                logoResId = it.logoResId,
                                key = it.key,
                                nextBillingDate = it.nextBillingDate
                            )
                        }

                val subscriptionList = subs.map { it.toDomain() }

                val freeTrialList =
                    subs.filter { it.subscriptionType == SubscriptionType.FREE_TRIAL.value }
                        .map {
                            Renewal(
                                name = it.name,
                                price = it.price,
                                daysLeft = getDaysLeft(it.nextBillingDate),
                                subscriptionType = it.subscriptionType,
                                logoResId = it.logoResId,
                                key = it.key,
                                packageName = allServices.filter { filter -> it.name == filter.name }.get(0).packageName,
                                nextBillingDate = it.nextBillingDate
                            )
                        }
                val dashboardData = DashboardData(
                    monthlySpend = monthlySpend,
                    currency = currency,
                    upcomingRenewals = upcomingRenewals,
                    subscriptions = subscriptionList,
                    freeTrials = freeTrialList,
                    user = getUser(),
                    smsSuggestions = smsSyncState.value
                )

                _uiState.value = dashboardData
            }
        }
    }

    fun getServiceByKey(key: String): Service? {
        return allServices.find { it.key == key }
    }
    fun getFirstName(fullName: String?): String {
        return fullName
            ?.trim()
            ?.split(" ")
            ?.firstOrNull()
            ?: "Guest"
    }


    fun scanSms() {
        viewModelScope.launch {

            _isLoadingSMS.value = true

            delay(100) // optional UX improvement

            val smsSuggestionList = withContext(Dispatchers.IO) {
                repository.fetchSubscriptionsFromSms()
            }

            _smsSyncState.value = smsSuggestionList
            Log.d("IOJASID", "scanSms: "+smsSuggestionList)

            _isLoadingSMS.value = false
        }
    }


    fun setUser(user: AuthUser?) {
        currentUser = user
        user?.let {
            viewModelScope.launch {
                repository.saveUserDetails(
                    UserEntity(
                        id = user.uid,
                        name = user.name ?: "Guest",
                        email = user.email ?: "",
                        logoResId = user.photo,
                        phone = "",
                    )
                )
            }
        }
    }

    suspend fun getUser(): AuthUser {

        repository.getUserDetails()?.let {
            return AuthUser(uid = it.id, name = it.name, email = it.email, photo = it.logoResId)
        }
        return AuthUser(uid = "1", name = "Guest", email = "", photo = "")
    }





    fun deleteSubscription(subscription: Subscription) {

        viewModelScope.launch {

            repository.deleteSubscription(subscription.id.toInt())
        }
    }
}