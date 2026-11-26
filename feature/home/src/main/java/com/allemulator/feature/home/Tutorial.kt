/*
 * Tutorial.kt
 * -----------------------------------------------------------------------------
 * หน้าสอนใช้งาน (บรีฟรอบ 2 ข้อ 8) — ขึ้นเองครั้งแรกที่เปิดแอพ, กด "ข้าม" ได้ทุกหน้า
 * ดูอีกครั้งได้ที่ ตั้งค่า → ดูวิธีใช้งานอีกครั้ง
 *
 * 4 หน้า: ยินดีต้อนรับ → เพิ่มเกม → ระหว่างเล่น → ปรับแต่ง
 * เลื่อนซ้าย-ขวา หรือกด "ถัดไป" ก็ได้
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.home

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allemulator.core.data.SettingsRepository
import com.allemulator.core.designsystem.glass.GlassButton
import com.allemulator.core.designsystem.glass.glass
import com.allemulator.core.designsystem.theme.GlassColors
import com.allemulator.core.emulation.tr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {
    /** true = ต้องโชว์หน้าสอนใช้งาน (null = ยังโหลดค่าไม่เสร็จ → ยังไม่โชว์อะไร) */
    val show: StateFlow<Boolean?> = settings.tutorialSeen.map { !it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun finish() = viewModelScope.launch { settings.setTutorialSeen(true) }
}

private data class TutorialPage(val icon: ImageVector, val title: String, val body: String)

private fun pages() = listOf(
    TutorialPage(
        Icons.Rounded.SportsEsports,
        tr("ยินดีต้อนรับสู่ Emu-X", "Welcome to Emu-X"),
        tr(
            "เล่นเกม PSP, PS1, PS2 (Beta), Game Boy, GBC, GBA และ NES ได้ในแอพเดียว\nแอพไม่มีเกมแถมมา ใช้ไฟล์เกมที่คุณเป็นเจ้าของ",
            "Play PSP, PS1, PS2 (Beta), Game Boy, GBC, GBA and NES games in one app.\nNo games are included — use game files you own.",
        ),
    ),
    TutorialPage(
        Icons.Rounded.Add,
        tr("เพิ่มเกม", "Add your games"),
        tr(
            "แตะการ์ดเครื่องเกม แล้วกดปุ่ม +\nเลือกทั้งโฟลเดอร์ หรือเลือกไฟล์เกม/.zip ทีละไฟล์ (แตก zip ให้อัตโนมัติ)",
            "Tap a console card, then the + button.\nPick a whole folder, or single game / .zip files (zips are extracted for you).",
        ),
    ),
    TutorialPage(
        Icons.Rounded.Menu,
        tr("ระหว่างเล่น", "While playing"),
        tr(
            "กดปุ่ม ☰ ด้านบนหรือปุ่มย้อนกลับ เพื่อเปิดเมนู: เซฟ/โหลด 4 ช่อง, เร่งความเร็ว, ตั้งค่าภาพและ FPS\nต่อจอยจริงได้ จอยบนจอจะซ่อนเอง",
            "Tap ☰ at the top or press Back to open the menu: 4 save slots, fast forward, video and FPS settings.\nPlug in a real controller and the on-screen pad hides itself.",
        ),
    ),
    TutorialPage(
        Icons.Rounded.Settings,
        tr("ปรับแต่งได้ตามใจ", "Make it yours"),
        tr(
            "กดค้างที่เกมเพื่อใส่ปกเกมหรือติดดาว\nปุ่มเฟืองในหน้าเครื่องเกม: ย้าย/ย่อขยายปุ่ม และปรับความลื่น\nสลับภาษาไทย/อังกฤษได้ที่สวิตช์ TH/EN หน้าแรก",
            "Long-press a game to add a cover or favorite it.\nThe gear on a console page lets you move/resize buttons and tune performance.\nSwitch Thai/English with the TH/EN switch on the home screen.",
        ),
    ),
)

/** หน้าสอนใช้งานแบบเต็มจอ วางทับทุกอย่าง */
@Composable
fun TutorialOverlay(onDone: () -> Unit) {
    val list = pages()
    val pager = rememberPagerState { list.size }
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == list.lastIndex
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        Modifier
            .fillMaxSize()
            .background(GlassColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // ปุ่มข้าม (มุมขวาบน) — กดได้ทุกหน้า
        TextButton(onClick = onDone, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
            Text(tr("ข้าม", "Skip"), color = GlassColors.TextSecondary)
        }

        Column(Modifier.fillMaxSize().padding(top = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalPager(pager, modifier = Modifier.weight(1f)) { index ->
                val page = list[index]
                // แนวนอน: จอเตี้ย → ไอคอนซ้าย ข้อความขวา (ถ้าเรียงลงล่าง ข้อความจะล้นจอจนมองไม่เห็น)
                if (landscape) {
                    Row(
                        Modifier.fillMaxSize().padding(horizontal = 48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PageIcon(page.icon, 96)
                        Spacer(Modifier.width(32.dp))
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            Text(page.title, style = MaterialTheme.typography.headlineMedium, color = GlassColors.TextPrimary)
                            Spacer(Modifier.height(12.dp))
                            Text(page.body, style = MaterialTheme.typography.bodyLarge, color = GlassColors.TextSecondary)
                        }
                    }
                } else {
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 32.dp).verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        PageIcon(page.icon, 120)
                        Spacer(Modifier.height(32.dp))
                        Text(page.title, style = MaterialTheme.typography.headlineMedium, color = GlassColors.TextPrimary, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Text(page.body, style = MaterialTheme.typography.bodyLarge, color = GlassColors.TextSecondary, textAlign = TextAlign.Center)
                    }
                }
            }

            // จุดบอกหน้า
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 16.dp)) {
                list.indices.forEach { i ->
                    Box(
                        Modifier
                            .size(if (i == pager.currentPage) 10.dp else 8.dp)
                            .background(if (i == pager.currentPage) GlassColors.Accent else GlassColors.TextPrimary.copy(alpha = 0.3f), CircleShape),
                    )
                }
            }

            GlassButton(
                if (last) tr("เริ่มเลย", "Get started") else tr("ถัดไป", "Next"),
                onClick = { if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                tint = GlassColors.Accent,
            )
            Spacer(Modifier.width(0.dp).height(24.dp))
        }
    }
}

/** ไอคอนประจำหน้าในกล่องกระจก */
@Composable
private fun PageIcon(icon: ImageVector, size: Int) {
    Box(
        Modifier.size(size.dp).glass(RoundedCornerShape((size * 0.3f).dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = GlassColors.Accent, modifier = Modifier.size((size * 0.47f).dp))
    }
}
