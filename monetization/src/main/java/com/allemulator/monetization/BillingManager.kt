/*
 * BillingManager.kt
 * -----------------------------------------------------------------------------
 * ซื้อ "ลบโฆษณาตลอดชีพ" ครั้งเดียว (บรีฟข้อ 2.2) ผ่าน Google Play Billing
 *
 * ขั้นตอนที่ต้องทำใน Play Console ก่อน (ดู docs/RELEASE.md):
 *   Monetize → Products → In-app products → สร้างสินค้า id = "remove_ads" แล้วกด Activate
 *
 * การทำงาน:
 *   connect()  → ต่อกับ Play Store แล้วเช็คว่าเคยซื้อไว้หรือยัง (กู้คืนอัตโนมัติเมื่อลงแอพใหม่)
 *   purchase() → เปิดหน้าจ่ายเงินของ Google
 *   ซื้อสำเร็จ → "acknowledge" (ยืนยันรับสินค้า) ภายใน 3 วัน ไม่งั้น Google คืนเงินอัตโนมัติ
 *   ผลลัพธ์เก็บใน SettingsRepository.isPremium → AdManager จะหยุดโชว์โฆษณาทันที
 * -----------------------------------------------------------------------------
 */
package com.allemulator.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.allemulator.core.data.SettingsRepository
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext context: Context,
    private val settings: SettingsRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // listener แยกเป็น private เพื่อไม่ให้โมดูลอื่นต้องรู้จัก library ของ Billing
    private val purchasesListener = PurchasesUpdatedListener { result, purchases -> onPurchasesUpdated(result, purchases) }

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private var product: ProductDetails? = null

    /** ราคาที่แสดงบนปุ่ม (เช่น "฿99.00") — null = ยังโหลดไม่เสร็จ/ยังไม่ได้ตั้งสินค้าใน Play Console */
    private val _price = MutableStateFlow<String?>(null)
    val price: StateFlow<String?> = _price

    /** ซื้อแล้วหรือยัง */
    val isPremium: Flow<Boolean> = settings.isPremium

    /** ข้อความแจ้งผล (เช่น "ขอบคุณที่สนับสนุน!") */
    val messages = MutableStateFlow<String?>(null)

    /** ต่อกับ Google Play (เรียกตอนเปิดแอพ) */
    fun connect() {
        if (client.isReady) { refresh(); return }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    refresh()
                    loadProduct()
                } else {
                    Log.w(TAG, "setup: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() = Unit // ต่อใหม่ตอนผู้ใช้กดซื้อ
        })
    }

    /** เช็คประวัติการซื้อกับ Google (ใช้เป็นปุ่ม "กู้คืนการซื้อ" ด้วย) */
    fun refresh() = scope.launch {
        if (!client.isReady) return@launch
        val result = client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
        )
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) return@launch
        val owned = result.purchasesList.firstOrNull { PRODUCT_ID in it.products && it.purchaseState == Purchase.PurchaseState.PURCHASED }
        if (owned != null) grant(owned)
        else settings.setPremium(false) // ไม่พบการซื้อ (หรือถูกคืนเงินแล้ว)
    }

    private fun loadProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            ).build()
        client.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            product = details.productDetailsList.firstOrNull()
            _price.value = product?.oneTimePurchaseOfferDetails?.formattedPrice
        }
    }

    /** เปิดหน้าจ่ายเงิน */
    fun purchase(activity: Activity) {
        val details = product
        if (details == null) {
            messages.value = "ยังเชื่อมต่อร้านค้าไม่ได้ ลองใหม่อีกครั้ง"
            connect()
            return
        }
        val flow = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()),
            ).build()
        client.launchBillingFlow(activity, flow)
    }

    /** Google แจ้งผลการซื้อกลับมา */
    private fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty()
                .filter { PRODUCT_ID in it.products }
                .forEach { p ->
                    when (p.purchaseState) {
                        Purchase.PurchaseState.PURCHASED -> scope.launch { grant(p); messages.value = "ขอบคุณที่สนับสนุน! ลบโฆษณาเรียบร้อย" }
                        Purchase.PurchaseState.PENDING -> messages.value = "รอการชำระเงิน เมื่อชำระแล้วโฆษณาจะหายเอง"
                        else -> Unit
                    }
                }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> refresh()
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> messages.value = "ซื้อไม่สำเร็จ: ${result.debugMessage}"
        }
    }

    /** ให้สิทธิ์ + ยืนยันรับสินค้ากับ Google */
    private suspend fun grant(purchase: Purchase) {
        settings.setPremium(true)
        if (!purchase.isAcknowledged) {
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(),
            )
        }
    }

    companion object {
        private const val TAG = "BillingManager"
        /** ต้องตรงกับ Product ID ที่สร้างใน Play Console */
        const val PRODUCT_ID = "remove_ads"
    }
}
