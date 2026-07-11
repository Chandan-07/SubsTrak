package com.tracker.subscription.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object SubtlyAnalytics {
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
        }
    }

    private fun logEvent(name: String, params: Bundle? = null) {
        firebaseAnalytics?.logEvent(name, params)
    }

    // 1. Onboarding Events
    fun logOnboardingStart() {
        logEvent("onboarding_start")
    }

    fun logOnboardingNext(slideIndex: Int) {
        val bundle = Bundle().apply {
            putInt("slide_index", slideIndex)
        }
        logEvent("onboarding_next", bundle)
    }

    fun logOnboardingComplete() {
        logEvent("onboarding_complete")
    }

    // 2. Authentication Events
    fun logSignInStart() {
        logEvent("sign_in_start")
    }

    fun logSignInSuccess(uid: String, email: String?) {
        val bundle = Bundle().apply {
            putString("method", "google")
            putString("uid", uid)
            email?.let { putString("email_domain", it.substringAfter("@", "")) }
        }
        logEvent("sign_in_success", bundle)
    }

    fun logSignInFailed(errorMessage: String) {
        val bundle = Bundle().apply {
            putString("error", errorMessage)
        }
        logEvent("sign_in_failed", bundle)
    }

    fun logSignInSkip() {
        logEvent("sign_in_skip")
    }

    fun logSignOut() {
        logEvent("sign_out")
    }

    // 3. Subscription Management Events
    fun logSubscriptionAddStart(source: String) {
        val bundle = Bundle().apply {
            putString("source", source) // "manual" or "sms"
        }
        logEvent("subscription_add_start", bundle)
    }

    fun logSubscriptionAddSuccess(
        name: String,
        price: Double,
        currency: String,
        billingCycle: String,
        category: String,
        type: String,
        source: String
    ) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, name)
            putDouble(FirebaseAnalytics.Param.VALUE, price)
            putString(FirebaseAnalytics.Param.CURRENCY, currency)
            putString("billing_cycle", billingCycle)
            putString("category", category)
            putString("subscription_type", type) // "PAID_SUBSCRIPTION" or "FREE_TRIAL"
            putString("source", source) // "manual" or "sms"
        }
        logEvent("subscription_add_success", bundle)
    }

    fun logSubscriptionUpdateSuccess(
        name: String,
        price: Double,
        currency: String,
        billingCycle: String,
        category: String,
        type: String
    ) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, name)
            putDouble(FirebaseAnalytics.Param.VALUE, price)
            putString(FirebaseAnalytics.Param.CURRENCY, currency)
            putString("billing_cycle", billingCycle)
            putString("category", category)
            putString("subscription_type", type)
        }
        logEvent("subscription_update_success", bundle)
    }

    fun logSubscriptionDelete(name: String, price: Double, category: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, name)
            putDouble(FirebaseAnalytics.Param.VALUE, price)
            putString("category", category)
        }
        logEvent("subscription_delete", bundle)
    }

    // 4. SMS Sync Events
    fun logSmsScanStart() {
        logEvent("sms_scan_start")
    }

    fun logSmsScanSuccess(count: Int) {
        val bundle = Bundle().apply {
            putInt("detected_count", count)
        }
        logEvent("sms_scan_success", bundle)
    }

    fun logSmsSyncApproved(count: Int) {
        val bundle = Bundle().apply {
            putInt("imported_count", count)
        }
        logEvent("sms_sync_approved", bundle)
    }

    // 5. Premium / Billing Events
    fun logPremiumScreenView() {
        logEvent("premium_screen_view")
    }

    fun logPremiumTriggerLimit(subsCount: Int) {
        val bundle = Bundle().apply {
            putInt("subs_count", subsCount)
        }
        logEvent("premium_trigger_limit", bundle)
    }

    fun logPremiumPurchaseStart() {
        logEvent("premium_purchase_start")
    }

    fun logPremiumPurchaseSuccess() {
        logEvent("premium_purchase_success")
    }

    fun logPremiumPurchaseFailed(error: String) {
        val bundle = Bundle().apply {
            putString("error", error)
        }
        logEvent("premium_purchase_failed", bundle)
    }

    // 6. Navigation / Tab Screen Views
    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        }
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // 7. Search Events
    fun logServiceSearch(query: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
        }
        logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
    }

    // 8. Options Sheet Events
    fun logOptionOpenApp(name: String) {
        val bundle = Bundle().apply {
            putString("subscription_name", name)
        }
        logEvent("option_open_app", bundle)
    }

    fun logOptionManageSubscription(name: String) {
        val bundle = Bundle().apply {
            putString("subscription_name", name)
        }
        logEvent("option_manage_sub", bundle)
    }

    fun logOptionCancelGuide(name: String) {
        val bundle = Bundle().apply {
            putString("subscription_name", name)
        }
        logEvent("option_cancel_guide", bundle)
    }
}
