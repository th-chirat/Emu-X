/*
 * AdManager.kt
 * -----------------------------------------------------------------------------
 * โฆษณา (บรีฟข้อ 2): แสดงโฆษณาเต็มจอ (interstitial) "ไม่เกิน 1 ครั้งทุก 1 ชั่วโมง"
 *
 * กติกา:
 *   1) ผู้ใช้ที่ซื้อ "ลบโฆษณาตลอดชีพ" → ไม่โชว์เลย
 *   2) โชว์เฉพาะ "จังหวะพัก" — ตอนเปิดแอพ / กลับจากเกม (MainActivity.onResume)
 *      ไม่โชว์ระหว่างเล่นเกม (นโยบาย Google Play ห้ามโฆษณาโผล่แบบไม่คาดคิดกลางการใช้งาน)
 *   3) 1 ชั่วโมงแรกหลังติดตั้งไม่โชว์ ให้ผู้ใช้ใหม่ได้ลองแอพก่อน
 *   4) ต้องขอความยินยอม (UMP consent) ก่อน — กฎหมาย GDPR ของยุโรปบังคับ
 *
 * ช่วงห่าง 1 ชม. ปรับได้ที่ AD_INTERVAL_MS (debug และ release ใช้ค่าเดียวกัน)
 * รหัสโฆษณาตั้งใน app/build.gradle.kts (ตอนนี้เป็นรหัสทดสอบของ Google)
 * -----------------------------------------------------------------------------
 */
package com.allemulator.monetization

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.allemulator.core.data.SettingsRepository
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val sdkStarted = AtomicBoolean(false)
    private var interstitial: InterstitialAd? = null
    private var loading = false


    /** ระยะห่างขั้นต่ำระหว่างโฆษณา = 1 ชั่วโมง (ทั้ง debug และ release) */
    private val adIntervalMs = AD_INTERVAL_MS

    /** รหัสโฆษณา interstitial (อ่านจาก meta-data ใน AndroidManifest ของแอพ) */
    private val interstitialId: String by lazy {
        @Suppress("DEPRECATION")
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData?.getString(META_INTERSTITIAL_ID) ?: TEST_INTERSTITIAL_ID
    }

    /**
     * เรียกครั้งเดียวตอนเปิดแอพ (MainActivity.onCreate)
     * ขอความยินยอม → เริ่ม SDK โฆษณา → โหลดโฆษณารอไว้
     */
    fun initialize(activity: Activity) {
        scope.launch {
            if (settings.isPremium.first()) return@launch // ซื้อแล้ว ไม่ต้องโหลด SDK โฆษณาเลย
            settings.firstLaunchAt()

            val consent = UserMessagingPlatform.getConsentInformation(activity)
            consent.requestConsentInfoUpdate(
                activity,
                ConsentRequestParameters.Builder().build(),
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                        if (error != null) Log.w(TAG, "consent form: ${error.message}")
                        if (consent.canRequestAds()) startSdk()
                    }
                },
                { error -> Log.w(TAG, "consent update: ${error.message}") },
            )
            // ถ้าเคยให้ความยินยอมไว้แล้ว เริ่มได้เลยไม่ต้องรอ
            if (consent.canRequestAds()) startSdk()
        }
    }

    private fun startSdk() {
        if (!sdkStarted.compareAndSet(false, true)) return
        MobileAds.initialize(context) { preload() }
    }

    /** โหลดโฆษณารอไว้ล่วงหน้า (ตอนจะโชว์จะได้ขึ้นทันที) */
    private fun preload() {
        if (interstitial != null || loading || !sdkStarted.get()) return
        loading = true
        InterstitialAd.load(
            context,
            interstitialId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "load failed: ${error.message}")
                    loading = false
                }
            },
        )
    }

    /**
     * เรียกใน "จังหวะพัก" (MainActivity.onResume) — จะโชว์ก็ต่อเมื่อครบเงื่อนไขทั้งหมด
     */
    fun maybeShowAd(activity: Activity) {
        scope.launch {
            if (settings.isPremium.first()) return@launch
            val now = System.currentTimeMillis()
            // ถ้ายังไม่เคยโชว์ ให้นับจากเวลาติดตั้ง (= 1 ชม.แรกไม่มีโฆษณา)
            val last = settings.lastAdShownAt().takeIf { it > 0 } ?: settings.firstLaunchAt()
            if (now - last < adIntervalMs) return@launch

            val ad = interstitial ?: run { preload(); return@launch }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitial = null
                    preload()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitial = null
                    preload()
                }
            }
            settings.setLastAdShownAt(now)
            ad.show(activity)
        }
    }

    companion object {
        private const val TAG = "AdManager"
        const val AD_INTERVAL_MS = 60 * 60 * 1000L // 1 ชั่วโมง
        const val META_INTERSTITIAL_ID = "com.allemulator.ADMOB_INTERSTITIAL_ID"

        /** รหัสทดสอบของ Google — ปลอดภัย ใช้ตอนพัฒนา (โฆษณาจริงห้ามกดเองเด็ดขาด บัญชีจะโดนแบน) */
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    }
}
