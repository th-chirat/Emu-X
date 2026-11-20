/*
 * AppSettingsScreen.kt
 * -----------------------------------------------------------------------------
 * ตั้งค่าทั่วไปของแอพ (เปิดจากเฟืองหน้าแรก)
 *
 *   ลบโฆษณาตลอดชีพ : ปุ่มซื้อ (แสดงราคาจาก Play Store) + กู้คืนการซื้อ
 *   ทั่วไป          : สั่นเมื่อกดปุ่ม
 *   เกี่ยวกับ        : เวอร์ชัน, นโยบายความเป็นส่วนตัว, ซอร์สโค้ด, license ของ emulator
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.allemulator.core.data.SettingsRepository
import com.allemulator.core.designsystem.glass.GlassButton
import com.allemulator.core.designsystem.glass.GlassIconButton
import com.allemulator.core.designsystem.glass.GlassScaffold
import com.allemulator.core.designsystem.glass.GlassSection
import com.allemulator.core.designsystem.glass.GlassTopBar
import com.allemulator.core.designsystem.theme.GlassColors
import com.allemulator.monetization.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    val billing: BillingManager,
    private val settings: SettingsRepository,
) : ViewModel() {
    val isPremium = billing.isPremium.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val haptics = settings.haptics.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    fun setHaptics(on: Boolean) = viewModelScope.launch { settings.setHaptics(on) }
}

/** ข้อมูล license ของ emulator core ที่ใช้ (GPL บังคับให้แจ้งผู้ใช้) */
private val CORE_LICENSES = listOf(
    "PPSSPP (PSP)" to "GPL-2.0+ — github.com/hrydgard/ppsspp",
    "PCSX-ReARMed (PS1)" to "GPL-2.0 — github.com/libretro/pcsx_rearmed",
    "Play! (PS2)" to "BSD-2-Clause — github.com/jpd002/Play-",
    "Gambatte (GB/GBC)" to "GPL-2.0 — github.com/libretro/gambatte-libretro",
    "FCEUmm (NES)" to "GPL-2.0 — github.com/libretro/libretro-fceumm",
    "melonDS DS (NDS)" to "GPL-3.0 — github.com/JesseTG/melonds-ds",
    "LibretroDroid" to "GPL-3.0 — github.com/Swordfish90/LibretroDroid",
    "Haze (เอฟเฟกต์กระจก)" to "Apache-2.0 — github.com/chrisbanes/haze",
)

/**
 * @param versionName  เวอร์ชันแอพ (ส่งมาจากโมดูล app)
 * @param privacyUrl   ลิงก์นโยบายความเป็นส่วนตัว
 * @param sourceUrl    ลิงก์ซอร์สโค้ดบน GitHub (GPL บังคับให้เปิดเผย)
 */
@Composable
fun AppSettingsScreen(
    onBack: () -> Unit,
    versionName: String,
    privacyUrl: String,
    sourceUrl: String,
    viewModel: AppSettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val premium by viewModel.isPremium.collectAsStateWithLifecycle()
    val price by viewModel.billing.price.collectAsStateWithLifecycle()
    val message by viewModel.billing.messages.collectAsStateWithLifecycle()
    val haptics by viewModel.haptics.collectAsStateWithLifecycle()
    var showLicenses by remember { mutableStateOf(false) }

    GlassScaffold {
        Column(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = "ตั้งค่า",
                navigation = { GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "กลับ", onBack) },
            )
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // ---------------- ลบโฆษณา ----------------
                GlassSection("ALL Emulator Premium") {
                    if (premium) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF5BE38A))
                        Text("คุณเป็นสมาชิก Premium ตลอดชีพ ไม่มีโฆษณาอีกต่อไป ขอบคุณที่สนับสนุน!", color = Color.White)
                    } else {
                        Icon(Icons.Rounded.Block, null, tint = GlassColors.Beta)
                        Text("จ่ายครั้งเดียว ไม่มีโฆษณาตลอดชีพ", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        Text("โฆษณาแสดงไม่เกิน 1 ครั้งทุก 3 ชั่วโมง และจะไม่ขึ้นระหว่างเล่นเกม", color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        GlassButton(
                            text = "ลบโฆษณา " + (price ?: ""),
                            onClick = { context.findActivity()?.let(viewModel.billing::purchase) },
                            modifier = Modifier.fillMaxWidth(),
                            tint = GlassColors.Accent,
                        )
                    }
                    NavRow("กู้คืนการซื้อ", { viewModel.billing.refresh() }, "สำหรับคนที่เคยซื้อแล้วลงแอพใหม่/เปลี่ยนเครื่อง")
                    message?.let { Text(it, color = GlassColors.Accent) }
                }

                // ---------------- ทั่วไป ----------------
                GlassSection("ทั่วไป") {
                    SwitchRow("สั่นเมื่อกดปุ่มจอย", haptics, { viewModel.setHaptics(it) })
                }

                // ---------------- เกี่ยวกับ ----------------
                GlassSection("เกี่ยวกับ") {
                    Text("ALL Emulator เวอร์ชัน $versionName", color = Color.White)
                    Text(
                        "แอพนี้ไม่มีเกมหรือ BIOS แถมมาให้ กรุณาใช้ไฟล์เกมที่คุณเป็นเจ้าของโดยชอบด้วยกฎหมายเท่านั้น",
                        color = GlassColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    NavRow("นโยบายความเป็นส่วนตัว", { context.openUrl(privacyUrl) })
                    NavRow("ซอร์สโค้ด (โอเพนซอร์ส GPL)", { context.openUrl(sourceUrl) })
                    NavRow("License ของ emulator ที่ใช้", { showLicenses = true })
                }
            }
        }
    }

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            title = { Text("โอเพนซอร์สที่ใช้") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CORE_LICENSES.forEach { (name, license) ->
                        Text(name, style = MaterialTheme.typography.titleMedium)
                        Text(license, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLicenses = false }) { Text("ปิด") } },
        )
    }
}

private fun Context.openUrl(url: String) {
    runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

/** หา Activity จาก Context ของ Compose (จำเป็นสำหรับเปิดหน้าจ่ายเงิน) */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
