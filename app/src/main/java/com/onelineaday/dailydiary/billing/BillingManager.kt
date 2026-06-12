package com.onelineaday.dailydiary.billing

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.android.billingclient.api.*
import com.onelineaday.dailydiary.PremiumManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object BillingManager : PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private var isConnected = false
    private var appContext: Context? = null
    
    // Replace these with your actual product IDs from the Google Play Console
    const val PRODUCT_MONTHLY = "premium_monthly"
    const val PRODUCT_LIFETIME = "premium_lifetime"

    fun init(context: Context) {
        appContext = context.applicationContext
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
            
        connectToBilling()
    }

    private fun connectToBilling() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    isConnected = true
                    checkPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                isConnected = false
            }
        })
    }

    private fun checkPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
        
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        var hasPremium = false
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                hasPremium = true
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { _ -> }
                }
            }
        }
        
        // If we found a valid purchase, enable premium using the app context
        if (hasPremium && !PremiumManager.isPremium.value) {
            appContext?.let { ctx ->
                PremiumManager.setPremium(ctx, true)
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productId: String, isSubscription: Boolean) {
        if (!isConnected) {
            Toast.makeText(activity, "Billing service unavailable. Simulating purchase...", Toast.LENGTH_SHORT).show()
            simulatePurchase(activity)
            return
        }

        val productType = if (isSubscription) BillingClient.ProductType.SUBS else BillingClient.ProductType.INAPP

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(productType)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        // setProductDetails requires a valid offerToken for subscriptions
                        .setProductDetails(productDetails)
                        .apply {
                            if (isSubscription) {
                                productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken?.let { token ->
                                    setOfferToken(token)
                                }
                            }
                        }
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                // Launch the billing flow
                billingClient.launchBillingFlow(activity, billingFlowParams)
            } else {
                // Product not found in console yet. Simulate for testing.
                activity.runOnUiThread {
                    Toast.makeText(activity, "Product not configured in Play Console yet. Simulating purchase...", Toast.LENGTH_SHORT).show()
                    simulatePurchase(activity)
                }
            }
        }
    }

    private fun simulatePurchase(activity: Activity) {
        PremiumManager.setPremium(activity, true)
        Toast.makeText(activity, "Welcome to Premium! Ads removed.", Toast.LENGTH_LONG).show()
    }
}
