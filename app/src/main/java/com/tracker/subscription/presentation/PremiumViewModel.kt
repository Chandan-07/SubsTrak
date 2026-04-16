package com.tracker.subscription.presentation

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.tracker.subscription.data.PlanUi
import com.tracker.subscription.data.repo.BillingRepository
import kotlinx.coroutines.launch

class PremiumViewModel(
    private val repo: BillingRepository
) : ViewModel() {

    var plans by mutableStateOf<List<PlanUi>>(arrayListOf())
        private set

    var selectedPlan by mutableStateOf<PlanUi?>(null)

    fun loadPlans() {
        viewModelScope.launch {
            repo.connect()

            val products = repo.getSubscriptions()

            Log.d("ASLMDS", "loadPlans: "+products)
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
            }.sortedByDescending { it.isYearly } // yearly on top

            selectedPlan = plans.firstOrNull()
        }
    }

    fun purchase(activity: Activity) {
        selectedPlan?.let {
            repo.launchPurchase(activity, it.productDetails)
        }
    }
}