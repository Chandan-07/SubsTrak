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
import com.tracker.subscription.data.dao.SubscriptionEntity
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.data.toDomain
import com.tracker.subscription.screens.home.DashboardUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import com.tracker.subscription.analytics.SubtlyAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
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

    private val _showGreenToast = MutableStateFlow<String?>(null)
    val showGreenToast: StateFlow<String?> = _showGreenToast.asStateFlow()

    fun clearGreenToast() {
        _showGreenToast.value = null
    }

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
                        reminderDaysBefore = it.reminderDaysBefore,
                        freeTrialPeriod = it.freeTrialPeriod,
                        packageName = allServices
                            .firstOrNull { filter -> it.name == filter.name }
                            ?.packageName ?: "",
                    )
                }

                val monthlySpend =
                    subs.filter { it.billingCycle == "Monthly" }
                        .sumOf { it.price }

                val monthlySpendChangePercent = calculateMonthlySpendChangePercent(subs, monthlySpend)

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
                                packageName = allServices
                                    .firstOrNull { filter -> it.name == filter.name }
                                    ?.packageName ?: "",
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
                        monthlySpendChangePercent = monthlySpendChangePercent,
                        currency = currency,
                        upcomingRenewals = upcomingRenewals,
                        subscriptions = subscriptionList,
                        freeTrials = freeTrialList,
                        user = user, // ✅ FIXED
                        smsSuggestions = smsSyncState.value,
                        isLoggedIn = isLoggedIn.value,
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
            SubtlyAnalytics.logSmsScanStart()

            _isLoadingSMS.value = true

            delay(100) // optional UX improvement

            val smsSuggestionList = withContext(Dispatchers.IO) {
                repository.fetchSubscriptionsFromSms()
            }

            _smsSyncState.value = smsSuggestionList
            Log.d("IOJASID", "scanSms: "+smsSuggestionList)

            SubtlyAnalytics.logSmsScanSuccess(smsSuggestionList.size)

            _isLoadingSMS.value = false
        }
    }

    fun clearSmsSuggestions() {
        _smsSyncState.value = emptyList()
    }

    fun addSmsSuggestionsToSubscriptions(
        suggestionsToAdd: List<ParsedSubscription> = _smsSyncState.value
    ) {
        val suggestions = suggestionsToAdd
            .distinctBy { it.service.lowercase().trim() }

        if (suggestions.isEmpty()) return

        viewModelScope.launch {
            suggestions.forEach { suggestion ->
                val service = repository.getExactService(suggestion.service)
                val resolvedName = service?.name ?: suggestion.service
                val resolvedCategory = service?.category ?: "Other"
                repository.addSubscription(
                    SubscriptionEntity(
                        name = resolvedName,
                        price = suggestion.amount,
                        currency = suggestion.currency,
                        billingCycle = "Monthly",
                        category = resolvedCategory,
                        subscriptionType = SubscriptionType.PAID_SUBSCRIPTION.value,
                        startDate = suggestion.date,
                        nextBillingDate = nextMonthlyBillingDate(suggestion.date),
                        reminderEnabled = true,
                        reminderDaysBefore = 1,
                        logoResId = service?.logo,
                        key = service?.key ?: suggestion.service
                            .lowercase()
                            .replace(Regex("[^a-z0-9]+"), "_")
                            .trim('_'),
                    )
                )
                SubtlyAnalytics.logSubscriptionAddSuccess(
                    name = resolvedName,
                    price = suggestion.amount,
                    currency = suggestion.currency,
                    billingCycle = "Monthly",
                    category = resolvedCategory,
                    type = SubscriptionType.PAID_SUBSCRIPTION.value,
                    source = "sms"
                )
            }
            SubtlyAnalytics.logSmsSyncApproved(suggestions.size)
            _smsSyncState.value = _smsSyncState.value - suggestions
            val count = suggestions.size
            val msg = if (count == 1) "1 subscription added successfully." else "$count subscriptions added successfully."
            _showGreenToast.value = msg
        }
    }

    private fun nextMonthlyBillingDate(startDate: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = startDate
            add(Calendar.MONTH, 1)
        }.timeInMillis
    }


    fun syncLoggedInState(loggedIn: Boolean) {
        _isLoggedIn.value = loggedIn
    }

    fun onSignOut() {
        _isLoggedIn.value = false
        currentUser = null
        viewModelScope.launch {
            repository.deleteSubData()
            repository.clearUserProfile()
        }
    }

    suspend fun persistAuthUser(user: AuthUser) {
        currentUser = user
        val displayName = user.name?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "User"
        repository.saveUserDetails(
            UserEntity(
                id = user.uid,
                name = displayName,
                email = user.email ?: "",
                logoResId = user.photo,
                phone = "",
            )
        )
    }

    fun setUser(user: AuthUser?) {
        currentUser = user
        user?.let {
            viewModelScope.launch {
                persistAuthUser(it)
            }
        }
    }


    private val _optionsSheetRenewal = MutableStateFlow<Renewal?>(null)
    val optionsSheetRenewal = _optionsSheetRenewal.asStateFlow()

    fun showOptionsSheetForSubscription(subscriptionId: Int) {
        viewModelScope.launch {
            val sub = repository.getSubscription(subscriptionId) ?: return@launch
            _optionsSheetRenewal.value = Renewal(
                id = sub.id.toString(),
                key = sub.key,
                name = sub.name,
                price = sub.price,
                daysLeft = getDaysLeft(sub.nextBillingDate),
                currency = sub.currency,
                subscriptionType = sub.subscriptionType,
                logoResId = sub.logoResId,
                packageName = allServices
                    .firstOrNull { it.key == sub.key || it.name == sub.name }
                    ?.packageName,
                nextBillingDate = sub.nextBillingDate
            )
        }
    }

    fun dismissOptionsSheet() {
        _optionsSheetRenewal.value = null
    }

    fun deleteSubscription(id: String) {

        viewModelScope.launch {
            val currentSubs = (_uiState.value as? DashboardUiState.Success)?.data?.subscriptions
            currentSubs?.find { it.id == id }?.let { sub ->
                SubtlyAnalytics.logSubscriptionDelete(
                    name = sub.name,
                    price = sub.price,
                    category = sub.category
                )
            }
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

    private fun calculateMonthlySpendChangePercent(
        subs: List<SubscriptionEntity>,
        currentMonthlySpend: Double
    ): Double? {
        if (currentMonthlySpend <= 0) return null

        val startOfCurrentMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val lastMonthSpend = subs
            .filter { it.billingCycle == "Monthly" && it.startDate < startOfCurrentMonth }
            .sumOf { it.price }

        if (lastMonthSpend <= 0) return null

        return ((currentMonthlySpend - lastMonthSpend) / lastMonthSpend) * 100.0
    }
}
