/*
 * LanguageSwitch.kt
 * -----------------------------------------------------------------------------
 * สวิตช์สลับภาษา TH / EN (บรีฟรอบ 2 ข้อ 4) — เป็นสวิตช์แบบเลื่อน ตัวหนังสือล้วน ไม่มีอิโมจิ/ธง
 * แตะที่ไหนก็ได้บนสวิตช์ = สลับภาษา ปุ่มกลมเลื่อนไปทางภาษาที่เลือก
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.glass

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.allemulator.core.designsystem.theme.GlassColors

/**
 * @param isThai    true = เลือกภาษาไทยอยู่
 * @param onToggle  ผู้ใช้แตะสวิตช์ (ส่งค่า true = อยากได้ภาษาไทย)
 */
@Composable
fun LanguageSwitch(isThai: Boolean, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val width = 88.dp
    val height = 40.dp
    val thumb = 34.dp
    val inset = 3.dp
    // ปุ่มกลม: ซ้าย = TH, ขวา = EN
    val thumbX by animateDpAsState(if (isThai) inset else width - thumb - inset, label = "langThumb")

    Box(
        modifier
            .size(width, height)
            .glass(CircleShape)
            .clip(CircleShape)
            .clickable(role = Role.Switch) { onToggle(!isThai) }
            .semantics { contentDescription = if (isThai) "Language: Thai" else "Language: English" },
    ) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = thumbX)
                .size(thumb)
                .background(GlassColors.Accent, CircleShape),
        )
        // ตัวหนังสือวางตรงตำแหน่งเดียวกับปุ่มกลมเป๊ะ (ซ้าย = inset, ขวา = width - thumb - inset)
        // → ตัวอักษรอยู่กลางปุ่มกลมพอดีทั้งสองฝั่ง
        Label("TH", selected = isThai, Modifier.align(Alignment.CenterStart).offset(x = inset).size(thumb))
        Label("EN", selected = !isThai, Modifier.align(Alignment.CenterStart).offset(x = width - thumb - inset).size(thumb))
    }
}

@Composable
private fun Label(text: String, selected: Boolean, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            text,
            // ตัด padding บน-ล่างของฟอนต์ + จัดบรรทัดให้อยู่กึ่งกลาง → ตัวหนังสือไม่ลอยสูง/ต่ำกว่าปุ่ม
            style = MaterialTheme.typography.labelLarge.copy(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both),
            ),
            textAlign = TextAlign.Center,
            color = if (selected) Color.White else GlassColors.TextSecondary,
        )
    }
}
