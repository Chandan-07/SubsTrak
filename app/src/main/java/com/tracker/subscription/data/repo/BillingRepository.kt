package com.tracker.subscription.data.repo

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.tracker.subscription.data.dao.UserDao
import com.tracker.subscription.data.dao.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

enum class PurchaseEvent {
    Success,
    Failed,
    Pending
}

class BillingRepository(
    private val context: Context,
    private val userDao: UserDao
) {

    companion object {
        private val PREMIUM_PRODUCT_IDS = setOf("monthly_premium", "premium_yearly")

        fun isAppUserSignedIn(isLoggedIn: Boolean, firebaseUid: String?): Boolean =
            isLoggedIn || firebaseUid != null
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _purchaseEvents = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 1)
    val purchaseEvents = _purchaseEvents.asSharedFlow()

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    purchases?.forEach { handlePurchase(it, notifyUi = true) }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    scope.launch { _purchaseEvents.emit(PurchaseEvent.Failed) }
                }
                else -> {
                    scope.launch { _purchaseEvents.emit(PurchaseEvent.Failed) }
                }
            }
        }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    /** Connects to Play Billing only. Does not read purchases or update premium in DB. */
    suspend fun connect() = suspendCancellableCoroutine<Unit> { cont ->
        if (billingClient.isReady) {
            cont.resume(Unit) {}
            return@suspendCancellableCoroutine
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    cont.resume(Unit) {}
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    /** Restores entitlements from Play into local DB — use on app start / account switch, not on paywall open. */
    suspend fun syncPremiumFromPlay() {
        if (!billingClient.isReady) {
            connect()
        }
        syncPurchasesWithPlay()
    }

    suspend fun getSubscriptions(): List<ProductDetails> {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("monthly_premium")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("premium_yearly")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        val result = billingClient.queryProductDetails(params)
        return result.productDetailsList ?: emptyList()
    }

    /**
     * @param notifyUi When true, emits [PurchaseEvent.Success] for the paywall UI.
     *   Restore/sync paths pass false so reconnect does not look like a new purchase.
     */
    private fun handlePurchase(purchase: Purchase, notifyUi: Boolean) {
        if (!purchase.products.any { it in PREMIUM_PRODUCT_IDS }) {
            if (notifyUi) {
                scope.launch { _purchaseEvents.emit(PurchaseEvent.Failed) }
            }
            return
        }

        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (!purchase.isAcknowledged) {
                    val params = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    billingClient.acknowledgePurchase(params) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            processPurchase(purchase, notifyUi)
                        } else if (notifyUi) {
                            scope.launch { _purchaseEvents.emit(PurchaseEvent.Failed) }
                        }
                    }
                } else {
                    processPurchase(purchase, notifyUi)
                }
            }
            Purchase.PurchaseState.PENDING -> {
                if (notifyUi) {
                    scope.launch { _purchaseEvents.emit(PurchaseEvent.Pending) }
                }
            }
            else -> {
                if (notifyUi) {
                    scope.launch { _purchaseEvents.emit(PurchaseEvent.Failed) }
                }
            }
        }
    }

    /**
     * Called only after Play Billing reports PURCHASED and acknowledge succeeds (if needed).
     * This is on-device verification via Play; for production, also verify the token on your backend.
     */
    private fun processPurchase(purchase: Purchase, notifyUi: Boolean) {
        scope.launch {
            val productId = purchase.products.firstOrNull { it in PREMIUM_PRODUCT_IDS }
                ?: return@launch

            val expiry = when (productId) {
                "monthly_premium" ->
                    purchase.purchaseTime + 30L * 24 * 60 * 60 * 1000
                "premium_yearly" ->
                    purchase.purchaseTime + 365L * 24 * 60 * 60 * 1000
                else -> return@launch
            }

            grantPremium(purchase.purchaseToken, expiry)
            if (notifyUi) {
                _purchaseEvents.emit(PurchaseEvent.Success)
            }
        }
    }

    private suspend fun queryActiveSubscriptions(): List<Purchase> =
        suspendCancellableCoroutine { cont ->
            if (!billingClient.isReady) {
                cont.resume(emptyList()) {}
                return@suspendCancellableCoroutine
            }
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    cont.resume(purchases.orEmpty()) {}
                } else {
                    cont.resume(emptyList()) {}
                }
            }
        }

    private suspend fun syncPurchasesWithPlay() {
        val purchases = queryActiveSubscriptions()
        if (purchases.isEmpty()) {
            if (userDao.isUserPremium() == true) {
                userDao.updatePremiumStatus(false)
            }
            return
        }
        purchases.forEach { handlePurchase(it, notifyUi = false) }
    }

    private suspend fun grantPremium(token: String, expiry: Long) {
        val existing = userDao.observeUser().firstOrNull()
        if (existing == null) {
            userDao.insert(
                UserEntity(
                    id = "guest",
                    name = "Guest",
                    email = "",
                    phone = "",
                    isPremium = true,
                    purchaseToken = token,
                    expiryTime = expiry
                )
            )
        } else {
            userDao.updatePremiumStatus(true)
            userDao.updatePurchaseToken(token)
            userDao.updateExpiry(expiry)
        }
    }

    /** @see syncPremiumFromPlay */
    suspend fun validateSubscription() = syncPremiumFromPlay()

    suspend fun clearPremiumOnSignOut() {
        userDao.clearPremiumStatus()
    }

    /** Clears local premium, then re-checks Play for the current device account. */
    suspend fun onAccountSwitch() {
        userDao.clearPremiumStatus()
        syncPremiumFromPlay()
    }

    suspend fun isUserPremium(): Boolean = userDao.isUserPremium() == true

    /**
     * Whether the paywall should show "already premium".
     * Guests ignore Play/device subscriptions — only local profile + guest purchase flag.
     */
    suspend fun shouldShowAlreadyPremium(
        isAppUserSignedIn: Boolean,
        guestPremiumOwned: Boolean
    ): Boolean {
        if (!isAppUserSignedIn) {
            return guestPremiumOwned && isUserPremium()
        }
        return isUserPremium()
    }

    fun launchPurchase(activity: Activity, product: ProductDetails) {
        val offerToken = product.subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken ?: return

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, params)
    }
}
