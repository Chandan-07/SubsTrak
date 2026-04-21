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
import com.tracker.subscription.screens.home.DashboardUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class DashboardViewModel(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)

    val allServices = repository.getAllServices() // your full list
    var isSigningIn by mutableStateOf(false)
        private set

    fun setLoading(value: Boolean) {
        isSigningIn = value
    }
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    fun setLoggedIn(value: Boolean) {
        _isLoggedIn.value = value
        if (!value){
            viewModelScope.launch {
                repository.deleteSubData()
            }
        }
    }
    private val _smsSyncState =
        MutableStateFlow<List<ParsedSubscription>>(arrayListOf())

    val uiState = _uiState.asStateFlow()
    val smsSyncState: StateFlow<List<ParsedSubscription>> = _smsSyncState
    var currentUser by mutableStateOf<AuthUser?>(null)
        private set

    private val _isLoadingSMS = MutableStateFlow(false)
    val isLoadingSMS = _isLoadingSMS.asStateFlow()

    private val _filteredSubscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val filteredSubscriptions: StateFlow<List<Subscription>> = _filteredSubscriptions

    init {
        observeSubscriptions()
    }

    private fun observeSubscriptions() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            combine(
                repository.getSubscriptions(),
                repository.observeUserDetails()
            ) { subs, user ->


                _filteredSubscriptions.value = subs.map {
                    Subscription(
                        id = it.id.toString(),
                        name = it.name,
                        price = it.price,
                        subscriptionType = it.subscriptionType,
                        logoResId = it.logoResId,
                        key = it.key,
                        currency = it.currency,
                        nextBillingDate = it.nextBillingDate,
                        billingCycle = it.billingCycle,
                        category = it.category,
                        startDate = it.startDate,
                        reminderEnabled = it.reminderEnabled,
                        reminderDaysBefore = it.reminderDaysBefore
                    )
                }

                val monthlySpend =
                    subs.filter { it.billingCycle == "Monthly" }
                        .sumOf { it.price }

                val currency = subs.firstOrNull()?.currency ?: ""

                val upcomingRenewals =
                    subs.sortedBy { it.nextBillingDate }
                        .filter { it.subscriptionType == SubscriptionType.PAID_SUBSCRIPTION.value }
                        .map {
                            Renewal(
                                id = it.id.toString(),
                                name = it.name,
                                price = it.price,
                                daysLeft = getDaysLeft(it.nextBillingDate),
                                subscriptionType = it.subscriptionType,
                                logoResId = it.logoResId,
                                key = it.key,
                                currency = it.currency,
                                nextBillingDate = it.nextBillingDate
                            )
                        }

                val subscriptionList = subs.map { it.toDomain() }

                val freeTrialList =
                    subs.filter { it.subscriptionType == SubscriptionType.FREE_TRIAL.value }
                        .map {
                            Renewal(
                                id = it.id.toString(),
                                name = it.name,
                                price = it.price,
                                daysLeft = getDaysLeft(it.nextBillingDate),
                                subscriptionType = it.subscriptionType,
                                logoResId = it.logoResId,
                                key = it.key,
                                currency = it.currency,
                                packageName = allServices
                                    .firstOrNull { filter -> it.name == filter.name }
                                    ?.packageName ?: "",
                                nextBillingDate = it.nextBillingDate
                            )
                        }

                DashboardUiState.Success(
                    DashboardData(
                        monthlySpend = monthlySpend,
                        currency = currency,
                        upcomingRenewals = upcomingRenewals,
                        subscriptions = subscriptionList,
                        freeTrials = freeTrialList,
                        user = user, // ✅ FIXED
                        smsSuggestions = smsSyncState.value,
                        isLoggedIn = isLoggedIn.value
                    )
                )
            }.collect { dashboardData ->
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
                if (isLoggedIn.value){
                    repository.saveUserDetails(
                        UserEntity(
                            id ="34567",
                            name =  "Guest",
                            email =  "",
                            logoResId = "",
                            phone = "",
                        )
                    )
                } else{
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
    }


    fun deleteSubscription(id: String) {

        viewModelScope.launch {

            repository.deleteSubscription(id.toInt())
        }
    }

    fun searchSubscriptions(query: String) {

        val state = _uiState.value

        val currentSubs = if (state is DashboardUiState.Success) {
            state.data.subscriptions
        } else {
            emptyList()
        }

        if (query.isBlank()) {
            _filteredSubscriptions.value = currentSubs
            return
        }

        val q = query.lowercase().replace(" ", "")

        _filteredSubscriptions.value = currentSubs.filter {
            it.name.lowercase().replace(" ", "").contains(q)
        }
    }

    fun filterByCategory(category: String) {

        val state = _uiState.value

        val currentSubs = if (state is DashboardUiState.Success) {
            state.data.subscriptions
        } else {
            emptyList()
        }

        _filteredSubscriptions.value =
            if (category == "All") {
                currentSubs
            } else {
                currentSubs.filter { it.category == category }
            }
    }
}