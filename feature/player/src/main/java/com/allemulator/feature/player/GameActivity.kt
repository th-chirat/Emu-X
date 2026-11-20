/*
 * GameActivity.kt
 * -----------------------------------------------------------------------------
 * หน้าเล่นเกม (แยกเป็น Activity ของตัวเอง เพื่อคุมจอเต็ม/การหมุนจอ/วงจรชีวิต OpenGL ได้ง่าย)
 *
 * ชั้นของหน้าจอ (ล่าง → บน):
 *   1) GLRetroView   — จอ emulator (OpenGL) จาก LibretroDroid
 *   2) VirtualGamepad — จอยเสมือน
 *   3) PerfOverlay   — FPS/CPU/GPU (ถ้าเปิดไว้)
 *   4) PauseMenu     — เมนูหยุดเกม (กดปุ่มย้อนกลับ หรือปุ่ม ☰ มุมบน)
 *
 * เริ่มเกมจากที่อื่นด้วย: startActivity(GameActivity.intent(context, gameUri))
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.player

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.allemulator.core.designsystem.glass.GlassIconButton
import com.allemulator.core.designsystem.theme.AllEmulatorTheme
import com.allemulator.core.emulation.CoreOptions
import com.allemulator.core.emulation.GameSystem
import com.allemulator.core.emulation.ScreenShader
import com.allemulator.feature.player.gamepad.GamepadListener
import com.allemulator.feature.player.gamepad.MotionSource
import com.allemulator.feature.player.gamepad.VirtualGamepad
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.ShaderConfig
import com.swordfish.libretrodroid.Variable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class GameActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    /** จอ emulator — มีได้ทีละ 1 ตัวเท่านั้น (LibretroDroid เป็น singleton ฝั่ง C++) */
    private var retroView: GLRetroView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()

        setContent {
            AllEmulatorTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    when (val s = state) {
                        PlayerState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        is PlayerState.Error -> ErrorDialog(s.message)
                        is PlayerState.Ready -> GameScreen(s)
                    }
                }
            }
        }
    }

    // =========================================================================
    // สร้างจอ emulator
    // =========================================================================
    private fun createRetroView(s: PlayerState.Ready): GLRetroView {
        val data = GLRetroViewData(this).apply {
            // core ถูกแตกไฟล์ไว้ที่ nativeLibraryDir (ตั้ง useLegacyPackaging = true ใน app/build.gradle.kts)
            coreFilePath = "${applicationInfo.nativeLibraryDir}/${s.system.coreLibrary}"
            when (val r = s.resolved) {
                is ResolvedGame.Virtual -> gameVirtualFiles = r.files
                is ResolvedGame.Path -> gameFilePath = r.path
            }
            systemDirectory = s.systemDir.absolutePath
            savesDirectory = s.savesDir.absolutePath
            variables = CoreOptions.build(s.system, s.performance).map { Variable(it.key, it.value) }.toTypedArray()
            saveRAMState = s.sram
            shader = when (s.performance.shader) {
                ScreenShader.NONE -> ShaderConfig.Default
                ScreenShader.SHARP -> ShaderConfig.Sharp
                ScreenShader.LCD -> ShaderConfig.LCD
                ScreenShader.CRT -> ShaderConfig.CRT
            }
            preferLowLatencyAudio = s.performance.lowLatencyAudio
            rumbleEventsEnabled = false
        }
        return GLRetroView(this, data).also { view ->
            lifecycle.addObserver(view)
            view.isFocusable = true
            view.isFocusableInTouchMode = true
            retroView = view
        }
    }

    // =========================================================================
    // หน้าจอหลักตอนเล่น
    // =========================================================================
    @Composable
    private fun GameScreen(s: PlayerState.Ready) {
        val view = remember { createRetroView(s) }
        val scope = rememberCoroutineScope()
        val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        val monitor = remember { PerformanceMonitor(applicationContext) }

        var menuOpen by remember { mutableStateOf(false) }
        var fastForward by remember { mutableStateOf(false) }
        var showPad by remember { mutableStateOf(!hasPhysicalGamepad()) }
        var perf by remember { mutableStateOf(PerfSnapshot()) }
        var error by remember { mutableStateOf<String?>(null) }

        // ---- ฟังเหตุการณ์จาก emulator: เฟรมวาดเสร็จ (นับ FPS) และ error ----
        LaunchedEffect(view) {
            launch { view.getGLRetroEvents().collect { if (it is GLRetroView.GLRetroEvents.FrameRendered) monitor.onFrame() } }
            launch {
                view.getGLRetroErrors().collect { code ->
                    // core เปิดไฟล์แบบไฟล์เสมือนไม่ได้ → เปิดหน้าใหม่แบบก๊อปไฟล์ลง cache (ลองครั้งเดียว)
                    if (code == GLRetroView.ERROR_LOAD_GAME && s.resolved.strategy != LoadStrategy.COPY && !viewModel.forceCopy) {
                        startActivity(intent(this@GameActivity, s.game.uri, forceCopy = true))
                        finish()
                    } else {
                        error = errorMessage(code, s.system)
                    }
                }
            }
            view.requestFocus()
        }
        // ---- เก็บค่าประสิทธิภาพทุก 1 วินาที (เฉพาะตอนเปิด overlay) ----
        if (s.overlay.anyEnabled) {
            LaunchedEffect(Unit) {
                while (isActive) {
                    delay(1_000)
                    perf = withContext(Dispatchers.Default) { monitor.sample() }
                }
            }
        }
        // ---- เซฟในเกม (SRAM) อัตโนมัติทุก 30 วินาที กันข้อมูลหายถ้าแอพถูกปิดกะทันหัน ----
        LaunchedEffect(Unit) {
            while (isActive) {
                delay(30_000)
                if (!menuOpen) saveSram()
            }
        }
        // ---- หยุดเกมตอนเปิดเมนู ----
        DisposableEffect(menuOpen) {
            if (menuOpen) view.onPause() else view.onResume()
            onDispose { }
        }

        BackHandler { menuOpen = !menuOpen }

        val listener = remember(view) {
            object : GamepadListener {
                override fun onKey(keyCode: Int, pressed: Boolean) =
                    view.sendKeyEvent(if (pressed) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP, keyCode)

                override fun onMotion(source: MotionSource, x: Float, y: Float) = view.sendMotionEvent(
                    when (source) {
                        MotionSource.DPAD -> GLRetroView.MOTION_SOURCE_DPAD
                        MotionSource.ANALOG_LEFT -> GLRetroView.MOTION_SOURCE_ANALOG_LEFT
                        MotionSource.ANALOG_RIGHT -> GLRetroView.MOTION_SOURCE_ANALOG_RIGHT
                    },
                    x, y,
                )
            }
        }

        Box(Modifier.fillMaxSize()) {
            // แนวตั้ง: จอเกมอยู่ด้านบน (NDS สูงกว่าเพราะมี 2 จอ) / แนวนอน: เต็มจอ
            val gameHeight = when {
                landscape -> 1f
                s.system.dualScreen -> 0.64f
                else -> 0.5f
            }
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                AndroidView(factory = { view }, modifier = Modifier.fillMaxWidth().fillMaxHeight(gameHeight))
            }

            if (showPad) {
                VirtualGamepad(
                    buttons = s.layout.forOrientation(landscape),
                    listener = listener,
                    haptics = s.haptics,
                    modifier = Modifier.safeDrawingPadding(),
                )
            }

            if (s.overlay.anyEnabled) {
                PerfOverlay(perf, s.overlay, Modifier.safeDrawingPadding().padding(8.dp))
            }

            // ปุ่มเปิดเมนู (มุมขวาบน)
            GlassIconButton(
                Icons.Rounded.Menu, "เมนู", { menuOpen = true },
                modifier = Modifier.align(Alignment.TopCenter).safeDrawingPadding().padding(top = 4.dp),
                size = 36,
            )

            if (menuOpen) {
                PauseMenu(
                    title = s.game.title,
                    fastForward = fastForward,
                    showPad = showPad,
                    slotInfo = { slot -> viewModel.stateFile(slot)?.takeIf { it.exists() }?.lastModified() },
                    onResume = { menuOpen = false },
                    onSave = { slot ->
                        menuOpen = false
                        scope.launch { saveState(slot) }
                    },
                    onLoad = { slot ->
                        menuOpen = false
                        scope.launch { loadState(slot) }
                    },
                    onToggleFastForward = {
                        fastForward = !fastForward
                        view.frameSpeed = if (fastForward) 2 else 1
                    },
                    onTogglePad = { showPad = !showPad },
                    onReset = { menuOpen = false; scope.launch { delay(100); view.reset() } },
                    onExit = {
                        // ต้อง resume ก่อน เพื่อให้ thread ของ emulator ทำงานเซฟได้
                        view.onResume()
                        scope.launch { saveSram(); finish() }
                    },
                )
            }
        }

        error?.let { ErrorDialog(it) }
    }

    // =========================================================================
    // เซฟ/โหลด (ทำบน background thread เพราะต้องรอ thread ของ emulator)
    // =========================================================================
    private suspend fun saveSram() {
        val v = retroView ?: return
        val bytes = withContext(Dispatchers.IO) { runCatching { v.serializeSRAM() }.getOrNull() } ?: return
        viewModel.writeSram(bytes)
    }

    private suspend fun saveState(slot: Int) = withContext(Dispatchers.IO) {
        val v = retroView ?: return@withContext
        delay(150) // รอให้ emulator กลับมาเดินก่อน (เมนูเพิ่งปิด)
        runCatching { viewModel.stateFile(slot)?.writeBytes(v.serializeState()) }
    }

    private suspend fun loadState(slot: Int) = withContext(Dispatchers.IO) {
        val v = retroView ?: return@withContext
        val file = viewModel.stateFile(slot)?.takeIf { it.exists() } ?: return@withContext
        delay(150)
        runCatching { v.unserializeState(file.readBytes()) }
    }

    // =========================================================================
    // จอยจริง (Bluetooth/USB) — ส่งต่อให้ GLRetroView แม้ focus จะอยู่ที่อื่น
    // =========================================================================
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val v = retroView
        if (v != null && event.keyCode != KeyEvent.KEYCODE_BACK && isGamepad(event.source)) {
            return when (event.action) {
                KeyEvent.ACTION_DOWN -> v.onKeyDown(event.keyCode, event)
                KeyEvent.ACTION_UP -> v.onKeyUp(event.keyCode, event)
                else -> super.dispatchKeyEvent(event)
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent): Boolean {
        retroView?.let { if (isGamepad(ev.source)) return it.onGenericMotionEvent(ev) }
        return super.dispatchGenericMotionEvent(ev)
    }

    private fun isGamepad(source: Int) =
        source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
            source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK

    /** มีจอยจริงต่ออยู่ไหม → ถ้ามี ซ่อนจอยเสมือนให้อัตโนมัติ */
    private fun hasPhysicalGamepad(): Boolean = InputDevice.getDeviceIds().any { id ->
        InputDevice.getDevice(id)?.let { !it.isVirtual && isGamepad(it.sources) } ?: false
    }

    // =========================================================================
    // อื่นๆ
    // =========================================================================
    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    override fun onDestroy() {
        retroView = null
        super.onDestroy()
    }

    private fun errorMessage(code: Int, system: GameSystem): String = when (code) {
        GLRetroView.ERROR_LOAD_LIBRARY -> "ไม่พบ emulator ของ ${system.displayName} ในแอพ (ไฟล์ ${system.coreLibrary}) — ติดตั้งแอพเวอร์ชันเต็มหรือรัน scripts/fetch-cores.sh"
        GLRetroView.ERROR_LOAD_GAME -> "เปิดไฟล์เกมไม่ได้ ไฟล์อาจเสีย หรือไม่ใช่เกม ${system.displayName}" +
            (system.biosNote?.let { "\n\nหมายเหตุ: $it" } ?: "")
        GLRetroView.ERROR_GL_NOT_COMPATIBLE -> "การ์ดจอของมือถือเครื่องนี้ไม่รองรับ ${system.displayName}"
        GLRetroView.ERROR_SERIALIZATION -> "เซฟ/โหลดไม่สำเร็จ"
        else -> "เกิดข้อผิดพลาดใน emulator (รหัส $code)"
    }

    @Composable
    private fun ErrorDialog(message: String) {
        AlertDialog(
            onDismissRequest = { finish() },
            title = { Text("เล่นเกมไม่ได้") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { finish() }) { Text("กลับ") } },
        )
    }

    companion object {
        const val EXTRA_GAME_URI = "gameUri"
        const val EXTRA_FORCE_COPY = "forceCopy"

        /** สร้าง Intent สำหรับเปิดเกม */
        fun intent(context: Context, gameUri: String, forceCopy: Boolean = false): Intent =
            Intent(context, GameActivity::class.java)
                .putExtra(EXTRA_GAME_URI, gameUri)
                .putExtra(EXTRA_FORCE_COPY, forceCopy)
    }
}
