package com.tracker.subscription.presentation

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.subscription.data.PlanUi
import com.tracker.subscription.data.repo.BillingRepository
import com.tracker.subscription.data.repo.PurchaseEvent
import kotlinx.coroutines.launch
import com.tracker.subscription.analytics.SubtlyAnalytics

class PremiumViewModel(
    private val repo: BillingRepository
) : ViewModel() {

    var plans by mutableStateOf<List<PlanUi>>(emptyList())
        private set

    var selectedPlan by mutableStateOf<PlanUi?>(null)

    var isPurchasing by mutableStateOf(false)
        private set

    var purchaseSuccess by mutableStateOf(false)
        private set

    var isAlreadyPremium by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repo.purchaseEvents.collect { event ->
                when (event) {
                    PurchaseEvent.Success -> {
                        isPurchasing = false
                        purchaseSuccess = true
                        isAlreadyPremium = true
                        SubtlyAnalytics.logPremiumPurchaseSuccess()
                    }
                    PurchaseEvent.Failed -> {
                        isPurchasing = false
                        SubtlyAnalytics.logPremiumPurchaseFailed("Billing repository returned purchase failure")
                    }
                    PurchaseEvent.Pending -> isPurchasing = true
                }
            }
        }
    }

    fun loadPlans(isAppUserSignedIn: Boolean, guestPremiumOwned: Boolean) {
        viewModelScope.launch {
            repo.connect()
            isAlreadyPremium = repo.shouldShowAlreadyPremium(
                isAppUserSignedIn = isAppUserSignedIn,
                guestPremiumOwned = guestPremiumOwned
            )

            val products = repo.getSubscriptions()
            plans = products.map {
                val price =
                    it.subscriptionOfferDetails?.first()?.pricingPhases
                        ?.pricingPhaseList?.first()?.formattedPrice ?: ""

                PlanUi(
                    title = if (it.productId.contains("year")) "Yearly" else "Monthly",
                    productDetails = it,
                    price = price,
                    isYearly = it.productId.contains("year")
                )
            }.sortedByDescending { it.isYearly }

            selectedPlan = plans.firstOrNull()
        }
    }

    fun purchase(activity: Activity) {
        selectedPlan?.let {
            isPurchasing = true
            SubtlyAnalytics.logPremiumPurchaseStart()
            repo.launchPurchase(activity, it.productDetails)
        }
    }

    fun resetPurchaseSuccess() {
        purchaseSuccess = false
    }
}
