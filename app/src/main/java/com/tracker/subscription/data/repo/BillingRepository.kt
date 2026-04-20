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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class BillingRepository(
    private val context: Context,
    private val userDao: UserDao
) {

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                purchases.forEach { handlePurchase(it) }
            }
        }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    suspend fun connect() = suspendCancellableCoroutine<Unit> { cont ->
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    cont.resume(Unit) {}
                    restorePurchases()
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
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

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(params) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        processPurchase(purchase)
                    }
                }
            } else {
                processPurchase(purchase)
            }
        }
    }

    private fun processPurchase(purchase: Purchase) {
        CoroutineScope(Dispatchers.IO).launch {

            val productId = purchase.products.firstOrNull()

            val expiry = when (productId) {
                "monthly_premium" ->
                    purchase.purchaseTime + 30L * 24 * 60 * 60 * 1000
                "premium_yearly" ->
                    purchase.purchaseTime + 365L * 24 * 60 * 60 * 1000
                else -> purchase.purchaseTime
            }

            userDao.updatePremiumStatus(1, true)
            userDao.updatePurchaseToken(1, purchase.purchaseToken)
            userDao.updateExpiry(1, expiry)
        }
    }

    private fun restorePurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { _, purchases ->
            purchases.forEach { handlePurchase(it) }
        }
    }

    suspend fun validateSubscription() {
        val user = userDao.observeUser() ?: return

        val currentTime = System.currentTimeMillis()
        userDao.updatePremiumStatus(1, true)
        userDao.updatePremiumStatus(1, false)

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