package com.thekeeperofpie.artistalleydatabase.play

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

class PlaySubscriptionProvider(
    private val scopedApplication: ScopedApplication,
    private val settings: MonetizationSettings,
) : SubscriptionProvider, DefaultLifecycleObserver {

    override val subscriptionDetails =
        MutableStateFlow(LoadingResult.loading<SubscriptionProvider.SubscriptionDetails<*>>())

    override val error = MutableStateFlow<Pair<Int, Throwable?>?>(null)

    override var loading by mutableStateOf(false)
        private set

    private lateinit var activity: Activity

    private lateinit var billingClient: BillingClient

    private lateinit var billingResult: BillingResult

    private val refresh = MutableSharedFlow<Unit>(1, 1)
    private val subscriptionRefresh = MutableSharedFlow<Unit>(1, 1)

    private val purchases = MutableSharedFlow<List<SubscriptionPurchase>>(1, 5)

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            this.purchases.tryEmit(purchases.onlyPremiumSubscriptions())
        } else when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK,
            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            -> {
                subscriptionRefresh.tryEmit(Unit)
            }
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED,
            BillingClient.BillingResponseCode.NETWORK_ERROR,
            -> {
                error.value =
                    R.string.play_error_purchasing to Exception(billingResult.debugMessage)
            }
        }
    }

    private val clientStateListener = object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            // TODO: Reconnect
        }

        override fun onBillingSetupFinished(result: BillingResult) {
            billingResult = result
            refresh.tryEmit(Unit)
        }
    }

    override fun initialize(activity: ComponentActivity) {
        if (::activity.isInitialized) return
        this.activity = activity
        billingClient = BillingClient.newBuilder(scopedApplication.app)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(clientStateListener)
        activity.lifecycle.addObserver(this)
        activity.lifecycleScope.launch(CustomDispatchers.IO) {
            refresh.collectLatest { checkSubscriptionStatus() }
        }
        activity.lifecycleScope.launch(CustomDispatchers.IO) {
            purchases.collect(::handlePurchase)
        }
        activity.lifecycleScope.launch(CustomDispatchers.IO) {
            subscriptionRefresh.collectLatest {
                val queryParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(
                        listOf(
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId("premium_subscription")
                                .setProductType(ProductType.SUBS)
                                .build()
                        )
                    )
                    .build()

                val productDetails = billingClient.queryProductDetails(queryParams)
                    .productDetailsList
                    ?.firstOrNull()

                val offerToken = productDetails?.subscriptionOfferDetails?.firstOrNull()?.offerToken
                val result: LoadingResult<SubscriptionProvider.SubscriptionDetails<*>> =
                    if (offerToken != null) {
                        LoadingResult.success(
                            SubscriptionProvider.SubscriptionDetails(
                                id = productDetails.productId,
                                value = productDetails,
                            )
                        )
                    } else {
                        LoadingResult.error(R.string.play_error_loading_subscription)
                    }
                subscriptionDetails.emit(result)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        refresh.tryEmit(Unit)
    }

    override fun loadSubscriptionDetails() {
        subscriptionRefresh.tryEmit(Unit)
    }

    private suspend fun handlePurchase(purchases: List<SubscriptionPurchase>) {
        acknowledge(purchases)
        val subscribed = purchases.any { it.purchase.purchaseState == PurchaseState.PURCHASED }
        val hasUnknownState =
            purchases.any { it.purchase.purchaseState == PurchaseState.UNSPECIFIED_STATE }

        if (subscribed || hasUnknownState) {
            settings.subscribed.emit(true)
            error.emit(null)
        }

        // If state unknown, pretend the subscription went through and check again in a bit
        if (hasUnknownState) {
            scopedApplication.scope.launch {
                delay(10.seconds)
                checkSubscriptionStatus()
            }
        }
    }

    private suspend fun checkSubscriptionStatus() {
        if (!billingClient.isReady) return
        val subscriptionPurchases = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(ProductType.SUBS)
                .build()
        ).purchasesList.onlyPremiumSubscriptions()

        acknowledge(subscriptionPurchases)

        val subscribed =
            subscriptionPurchases.any { it.purchase.purchaseState == PurchaseState.PURCHASED }

        // TODO: Handle pending

        // TODO: If was subscribed and no longer subscribed, show an error message
        // TODO: Handle root level app error messages
        settings.subscribed.emit(subscribed)
    }

    private suspend fun acknowledge(purchases: List<SubscriptionPurchase>) = purchases.forEach {
        if (!it.purchase.isAcknowledged) {
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchase.purchaseToken)
                    .build()
            )
        }
    }

    override fun requestSubscribe(subscription: SubscriptionProvider.SubscriptionDetails<*>) {
        if (loading) return
        loading = true
        scopedApplication.scope.launch(CustomDispatchers.IO) {
            val productDetails = subscription.value as ProductDetails
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(productDetails.subscriptionOfferDetails!!.first().offerToken)
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            withContext(CustomDispatchers.Main) {
                val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    error.value = R.string.play_error_loading_subscription to
                            Exception(billingResult.debugMessage)
                }

                loading = false
            }
        }
    }

    override fun getManageSubscriptionUrl(
        subscription: SubscriptionProvider.SubscriptionDetails<*>?,
    ) = if (subscription == null) {
        "https://play.google.com/store/account/subscriptions"
    } else {
        "https://play.google.com/store/account/subscriptions" +
                "?sku=${(subscription.value as ProductDetails).productId}" +
                "&package=com.thekeeperofpie.anichive"
    }

    private fun List<Purchase>.onlyPremiumSubscriptions() =
        filter { it.products.any { it.contains("premium", ignoreCase = true) } }
            .map(::SubscriptionPurchase)

    /**
     * Wraps purchases so that only subscriptions the app knows about are handled, for forwards
     * compatibility with newer product IDs.
     */
    data class SubscriptionPurchase(
        val purchase: Purchase,
    )
}
