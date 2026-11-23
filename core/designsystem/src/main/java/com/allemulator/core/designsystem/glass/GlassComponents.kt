/*
 * GlassComponents.kt
 * -----------------------------------------------------------------------------
 * ชิ้นส่วน UI สำเร็จรูปแบบกระจกที่ใช้ซ้ำทั่วแอพ:
 *   GlassCard        การ์ดกดได้ (เด้งยุบเล็กน้อยตอนกด)
 *   GlassButton      ปุ่มทรงแคปซูล
 *   GlassIconButton  ปุ่มไอคอนวงกลม
 *   GlassTopBar      แถบหัวหน้าจอ
 *   GlassFab         ปุ่ม + ลอย
 *   BetaBadge        ป้าย "BETA"
 *   GlassSection     กล่องกลุ่มตั้งค่า
 * -----------------------------------------------------------------------------
 */
package com.allemulator.core.designsystem.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.allemulator.core.designsystem.theme.GlassColors

/** ทำให้ element ยุบลงเล็กน้อยตอนกด (ความรู้สึก "ของเหลว") */
@Composable
private fun pressScale(source: MutableInteractionSource): Float {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "pressScale",
    )
    return scale
}

/** การ์ดกระจกกดได้ */
@Composable
fun GlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale = pressScale(source)
    Column(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .glass(shape, tint = tint, pressed = pressed)
            .clickable(interactionSource = source, indication = null, onClick = onClick),
        content = content,
    )
}

/** ปุ่มแคปซูลกระจก */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color? = null,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale = pressScale(source)
    Row(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .glass(CircleShape, tint = tint, pressed = pressed)
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

/** ปุ่มไอคอนวงกลมกระจก */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 44,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale = pressScale(source)
    Box(
        modifier
            .size(size.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .glass(CircleShape, pressed = pressed)
            .clickable(interactionSource = source, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.size((size * 0.5f).dp))
    }
}

/** ปุ่ม + ลอยมุมขวาล่าง */
@Composable
fun GlassFab(icon: ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassIconButton(icon, contentDescription, onClick, modifier, size = 64)
}

/** แถบหัวหน้าจอ: [ปุ่มซ้าย] ชื่อเรื่อง ........ [ปุ่มขวา] */
@Composable
fun GlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigation: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (navigation != null) {
            navigation()
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            // จอแคบ (เช่น ตั้งการแสดงผลใหญ่) ตัวหนังสือจะย่อลงเองให้อ่านได้ครบ ไม่ถูกตัดเป็น "..."
            ShrinkToFitText(title, MaterialTheme.typography.headlineMedium, Color.White, minSize = 16f)
            if (subtitle != null) {
                ShrinkToFitText(subtitle, MaterialTheme.typography.bodyMedium, GlassColors.TextSecondary, minSize = 11f)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), content = actions)
    }
}

/** ข้อความ 1 บรรทัดที่ย่อขนาดตัวอักษรลงเรื่อยๆ จนพอดีความกว้าง (ไม่ต่ำกว่า [minSize] sp) */
@Composable
private fun ShrinkToFitText(text: String, style: TextStyle, color: Color, minSize: Float) {
    var size by remember(text) { mutableStateOf(style.fontSize.value) }
    Text(
        text,
        style = style.copy(fontSize = size.sp),
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layout ->
            if (layout.hasVisualOverflow && size > minSize) size = (size * 0.9f).coerceAtLeast(minSize)
        },
    )
}

/** ป้าย BETA สีส้ม */
@Composable
fun BetaBadge(modifier: Modifier = Modifier) {
    Text(
        "BETA",
        style = MaterialTheme.typography.labelSmall,
        color = Color.Black,
        modifier = modifier
            .background(GlassColors.Beta, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** กล่องกลุ่ม (ใช้ในหน้าตั้งค่า) มีหัวข้อ + เนื้อหา */
@Composable
fun GlassSection(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth()) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = GlassColors.TextSecondary,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .glass(RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}
