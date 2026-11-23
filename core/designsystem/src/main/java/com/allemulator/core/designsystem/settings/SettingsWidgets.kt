/*
 * SettingsWidgets.kt
 * -----------------------------------------------------------------------------
 * ชิ้นส่วนเล็กๆ ที่ใช้ซ้ำในหน้าตั้งค่า (หน้าตั้งค่าเครื่อง + ตั้งค่าระหว่างเล่น ใช้แบบเดียวกัน): แถวสวิตช์, แถวกดได้, ปุ่มเลือกแบบชิป
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.allemulator.core.designsystem.theme.GlassColors

/** แถว: หัวข้อ + คำอธิบาย + สวิตช์ */
@Composable
fun SwitchRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit, subtitle: String? = null) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let { Text(it, color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyMedium) }
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** แถวกดได้ มีลูกศร > */
@Composable
fun NavRow(title: String, onClick: () -> Unit, subtitle: String? = null) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let { Text(it, color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyMedium) }
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = GlassColors.TextSecondary)
    }
}

/** กลุ่มชิปให้เลือก 1 ตัว (เช่น ปิด / อัตโนมัติ / 1 / 2 / 3) */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipChoice(title: String, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    Column {
        Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            options.forEach { (value, label) ->
                val on = value == selected
                Text(
                    label,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .background(if (on) GlassColors.Accent.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.08f), CircleShape)
                        .border(1.dp, if (on) GlassColors.Accent else Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { onSelect(value) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}
