/*
 * AppSettingsScreen.kt
 * -----------------------------------------------------------------------------
 * ตั้งค่าทั่วไปของแอพ (เปิดจากเฟืองหน้าแรก)
 *
 *   ลบโฆษณาตลอดชีพ : ปุ่มซื้อ (แสดงราคาจาก Play Store) + กู้คืนการซื้อ
 *   ทั่วไป          : ภาษา (สวิตช์ TH/EN), ธีม (ตามระบบ/สว่าง/มืด), สั่นเมื่อกดปุ่ม, ดูวิธีใช้งาน
 *   เกี่ยวกับ        : เวอร์ชัน, นโยบายความเป็นส่วนตัว, ซอร์สโค้ด, เครดิตและ license ของ emulator
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.settings

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import com.allemulator.core.emulation.ThemePrefs
import com.allemulator.core.emulation.ThemeMode
import com.allemulator.core.emulation.L10n
import com.allemulator.core.emulation.AppLanguage
import com.allemulator.core.designsystem.glass.LanguageSwitch
import com.allemulator.core.emulation.tr
import com.allemulator.core.designsystem.settings.ChipChoice
import com.allemulator.core.designsystem.settings.NavRow
import com.allemulator.core.designsystem.settings.SwitchRow
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

    /** สวิตช์ภาษา (ย้ายมาจากหน้าแรก — บรีฟรอบ 4 ข้อ 2): เปลี่ยนทันทีแล้วค่อยบันทึก */
    fun setThai(thai: Boolean) {
        val lang = if (thai) AppLanguage.TH else AppLanguage.EN
        L10n.set(lang)
        viewModelScope.launch { settings.setLanguage(lang) }
    }

    /** โหมดสี: ตามระบบ / สว่าง / มืด (บรีฟรอบ 4 ข้อ 3) */
    val themeMode = settings.themeMode.stateIn(viewModelScope, SharingStarted.Eagerly, ThemePrefs.mode.value)
    fun setThemeMode(mode: ThemeMode) {
        ThemePrefs.set(mode)
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    /** เปิดหน้าสอนใช้งานอีกครั้ง (ขึ้นทับหน้าแรก) */
    fun showTutorial() = viewModelScope.launch { settings.setTutorialSeen(false) }
}

/**
 * เครดิต + license ของโอเพนซอร์สที่แอพใช้ (บรีฟรอบ 5 ข้อ 1)
 * GPL บังคับให้แจ้งผู้ใช้ว่าใช้โค้ดของใคร ภายใต้ license อะไร และหาซอร์สได้ที่ไหน
 * และเป็นการให้เกียรติทีมที่ทำ emulator ด้วย — ส่วนที่ทำงานหนักจริงคือของพวกเขา
 */
private data class Credit(val name: String, val role: String, val authors: String, val license: String)

private val CREDITS get() = listOf(
    Credit("ARMSX2", tr("emulator PS2", "PS2 emulator"), tr("ทีม ARMSX2 — พัฒนาต่อจาก PCSX2 ของทีม PCSX2", "The ARMSX2 team — built on PCSX2 by the PCSX2 team"), "GPL-3.0 — github.com/ARMSX2/ARMSX2"),
    Credit("PPSSPP", tr("emulator PSP", "PSP emulator"), "Henrik Rydgård " + tr("และผู้ร่วมพัฒนา", "and contributors"), "GPL-2.0+ — github.com/hrydgard/ppsspp"),
    Credit("PCSX-ReARMed", tr("emulator PS1", "PS1 emulator"), "notaz, libretro " + tr("และผู้ร่วมพัฒนา", "and contributors"), "GPL-2.0 — github.com/libretro/pcsx_rearmed"),
    Credit("Gambatte", tr("emulator Game Boy / GBC", "Game Boy / GBC emulator"), "sinamas " + tr("และผู้ร่วมพัฒนา", "and contributors"), "GPL-2.0 — github.com/libretro/gambatte-libretro"),
    Credit("mGBA", tr("emulator GBA", "GBA emulator"), "Vicki Pfau (endrift) " + tr("และผู้ร่วมพัฒนา", "and contributors"), "MPL-2.0 — github.com/libretro/mgba"),
    Credit("FCEUmm", tr("emulator NES", "NES emulator"), tr("ทีม FCEU และ libretro", "The FCEU team and libretro"), "GPL-2.0 — github.com/libretro/libretro-fceumm"),
    Credit("LibretroDroid", tr("ตัวเล่นเกม (frontend)", "Game player (frontend)"), "Filippo Scognamiglio (Swordfish90)", "GPL-3.0 — github.com/Swordfish90/LibretroDroid"),
    Credit("libretro API", tr("มาตรฐานเชื่อม emulator", "Emulator interface standard"), tr("ทีม RetroArch", "The RetroArch team"), "MIT — github.com/libretro/RetroArch"),
    Credit("Haze", tr("เอฟเฟกต์กระจก", "Glass effect"), "Chris Banes", "Apache-2.0 — github.com/chrisbanes/haze"),
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
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    var showLicenses by remember { mutableStateOf(false) }

    GlassScaffold {
        Column(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = tr("ตั้งค่า", "Settings"),
                navigation = { GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, tr("กลับ", "Back"), onBack) },
            )
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // ---------------- ลบโฆษณา ----------------
                GlassSection("Emu-X Premium") {
                    if (premium) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF5BE38A))
                        Text(tr("คุณเป็นสมาชิก Premium ตลอดชีพ ไม่มีโฆษณาอีกต่อไป ขอบคุณที่สนับสนุน!", "You have lifetime Premium — no more ads. Thanks for your support!"), color = GlassColors.TextPrimary)
                    } else {
                        Icon(Icons.Rounded.Block, null, tint = GlassColors.Beta)
                        Text(tr("จ่ายครั้งเดียว ไม่มีโฆษณาตลอดชีพ", "Pay once, no ads forever"), color = GlassColors.TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(tr("โฆษณาแสดงไม่เกิน 1 ครั้งทุก 1 ชั่วโมง และจะไม่ขึ้นระหว่างเล่นเกม", "Ads show at most once an hour and never during gameplay"), color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        GlassButton(
                            text = tr("ลบโฆษณา ", "Remove ads ") + (price ?: ""),
                            onClick = { context.findActivity()?.let(viewModel.billing::purchase) },
                            modifier = Modifier.fillMaxWidth(),
                            tint = GlassColors.Accent,
                        )
                    }
                    NavRow(tr("กู้คืนการซื้อ", "Restore purchase"), { viewModel.billing.refresh() }, tr("สำหรับคนที่เคยซื้อแล้วลงแอพใหม่/เปลี่ยนเครื่อง", "If you bought before and reinstalled or changed phones"))
                    message?.let { Text(it, color = GlassColors.Accent) }
                }

                // ---------------- ทั่วไป ----------------
                GlassSection(tr("ทั่วไป", "General")) {
                    // ภาษา: ข้อความซ้าย + สวิตช์ TH/EN ขวา
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(tr("ภาษา", "Language"), color = GlassColors.TextPrimary, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        LanguageSwitch(isThai = L10n.isThai, onToggle = viewModel::setThai)
                    }
                    ChipChoice(
                        tr("ธีม", "Theme"),
                        ThemeMode.entries.map { it to it.title },
                        themeMode,
                    ) { viewModel.setThemeMode(it) }
                    SwitchRow(tr("สั่นเมื่อกดปุ่มจอย", "Vibrate on button press"), haptics, { viewModel.setHaptics(it) })
                    NavRow(tr("ดูวิธีใช้งานอีกครั้ง", "Show tutorial again"), { viewModel.showTutorial(); onBack() })
                }

                // ---------------- เกี่ยวกับ ----------------
                GlassSection(tr("เกี่ยวกับ", "About")) {
                    Text(tr("Emu-X เวอร์ชัน $versionName", "Emu-X version $versionName"), color = GlassColors.TextPrimary)
                    Text(
                        tr("แอพนี้ไม่มีเกมหรือ BIOS แถมมาให้ กรุณาใช้ไฟล์เกมที่คุณเป็นเจ้าของโดยชอบด้วยกฎหมายเท่านั้น", "This app includes no games or BIOS. Only use game files you legally own."),
                        color = GlassColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    NavRow(tr("นโยบายความเป็นส่วนตัว", "Privacy policy"), { context.openUrl(privacyUrl) })
                    NavRow(tr("ซอร์สโค้ด (โอเพนซอร์ส GPL)", "Source code (open source, GPL)"), { context.openUrl(sourceUrl) })
                    NavRow(tr("เครดิตและ license", "Credits & licenses"), { showLicenses = true }, tr("ขอบคุณทีมผู้สร้าง emulator ทุกตัวที่แอพนี้ใช้", "Thanks to the teams behind every emulator in this app"))
                }
            }
        }
    }

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            title = { Text(tr("เครดิต", "Credits")) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        tr("Emu-X ทำงานได้เพราะโอเพนซอร์สเหล่านี้ ขอบคุณทุกทีมมาก", "Emu-X is possible thanks to these open-source projects. Huge thanks to every team."),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    CREDITS.forEach { c ->
                        Column {
                            Text("${c.name} — ${c.role}", style = MaterialTheme.typography.titleMedium)
                            Text(c.authors, style = MaterialTheme.typography.bodyMedium)
                            Text(c.license, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLicenses = false }) { Text(tr("ปิด", "Close")) } },
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
