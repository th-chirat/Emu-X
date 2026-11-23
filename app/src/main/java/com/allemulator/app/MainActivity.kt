/*
 * MainActivity.kt
 * -----------------------------------------------------------------------------
 * หน้าต่างหลักของแอพ (ทุกหน้ายกเว้นหน้าเล่นเกมอยู่ในนี้)
 *
 *   onCreate  → โชว์ splash, เริ่มระบบโฆษณา + ระบบซื้อ, วาด UI (AppNavHost)
 *   onResume  → "จังหวะพัก" (เพิ่งเปิดแอพ หรือเพิ่งออกจากเกม)
 *               → ให้ AdManager ตัดสินใจว่าครบ 1 ชม. หรือยัง ถ้าครบก็โชว์โฆษณา
 * -----------------------------------------------------------------------------
 */
package com.allemulator.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.allemulator.core.designsystem.theme.AllEmulatorTheme
import com.allemulator.feature.player.GameActivity
import com.allemulator.monetization.AdManager
import com.allemulator.monetization.BillingManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adManager: AdManager
    @Inject lateinit var billing: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        billing.connect()           // เช็คว่าซื้อลบโฆษณาไว้หรือยัง
        adManager.initialize(this)  // ขอความยินยอม + โหลดโฆษณารอไว้

        setContent {
            AllEmulatorTheme {
                AppNavHost(
                    onPlayGame = { game -> startActivity(GameActivity.intent(this, game.uri)) },
                    versionName = BuildConfig.VERSION_NAME,
                    privacyUrl = BuildConfig.PRIVACY_URL,
                    sourceUrl = BuildConfig.SOURCE_URL,
                )
            }
        }
    }

    /** true = ยังไม่เคยถึงจังหวะเปิดแอพ (resume ครั้งแรก) */
    private var firstResume = true

    override fun onResume() {
        super.onResume()
        // โชว์โฆษณาเฉพาะ "จังหวะพัก" จริงๆ: ตอนเปิดแอพ หรือเพิ่งออกจากเกม
        // ไม่โชว์ตอนกลับมาจากหน้าต่างเลือกไฟล์/โฟลเดอร์ของระบบ (จะรบกวนผู้ใช้)
        val returnedFromGame = com.allemulator.feature.player.GameActivity.consumeExitedFlag()
        if (firstResume || returnedFromGame) adManager.maybeShowAd(this)
        firstResume = false
    }
}
