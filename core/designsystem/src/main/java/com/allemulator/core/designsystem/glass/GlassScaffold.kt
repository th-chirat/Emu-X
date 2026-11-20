/*
 * GlassScaffold.kt
 * -----------------------------------------------------------------------------
 * โครงหน้าจอมาตรฐานของแอพ: พื้นหลังของเหลว + เปิดระบบเบลอ (Haze) ให้กระจกทุกชิ้นในหน้า
 *
 * ทุกหน้าจอ (ยกเว้นหน้าเล่นเกม) ควรห่อด้วย GlassScaffold
 * แล้วใช้ GlassCard / GlassButton ข้างในได้เลย กระจกจะเบลอพื้นหลังให้อัตโนมัติ
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.allemulator.core.designsystem.theme.GlassColors
import com.allemulator.core.designsystem.theme.LocalHazeState
import dev.chrisbanes.haze.rememberHazeState

/**
 * @param tint สีเสริมของพื้นหลัง (เช่นสีประจำเครื่องเกมในหน้ารายการเกม)
 */
@Composable
fun GlassScaffold(
    modifier: Modifier = Modifier,
    tint: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val hazeState = rememberHazeState()
    LiquidBackground(modifier, hazeState = hazeState, tint = tint) {
        CompositionLocalProvider(LocalHazeState provides hazeState) {
            Box(Modifier.fillMaxSize(), content = content)
        }
    }
}

/** ช่องค้นหาแบบกระจก */
@Composable
fun GlassSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .glass(RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, null, tint = GlassColors.TextSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = GlassColors.TextSecondary, style = MaterialTheme.typography.bodyLarge)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                cursorBrush = SolidColor(GlassColors.Accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
