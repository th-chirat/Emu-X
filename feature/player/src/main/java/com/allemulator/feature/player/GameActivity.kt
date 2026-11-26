/*
 * GameActivity.kt
 * -----------------------------------------------------------------------------
 * หน้าเล่นเกม (แยกเป็น Activity ของตัวเอง เพื่อคุมจอเต็ม/การหมุนจอ/วงจรชีวิต OpenGL ได้ง่าย)
 *
 * ชั้นของหน้าจอ (ล่าง → บน):
 *   1) GLRetroView   — จอ emulator (OpenGL) จาก LibretroDroid
 *   2) VirtualGamepad — จอยเสมือน
 *   3) PerfOverlay   — FPS/CPU (ถ้าเปิดไว้)
 *   4) PauseMenu     — เมนูหยุดเกม (กดปุ่มย้อนกลับ หรือปุ่ม ☰ มุมบน)
 *
 * จอยจริง (Bluetooth/USB): ต่อแล้วจอยบนจอซ่อนเอง → แตะจอเมื่อไหร่ จอยบนจอโผล่กลับมา
 * เครื่อง Nintendo: พื้นหลังเป็น "บอดี้เครื่อง" สีตามเครื่อง + กรอบจอ ไม่มีพื้นที่สีดำโล่งๆ
 *
 * เริ่มเกมจากที่อื่นด้วย: startActivity(GameActivity.intent(context, gameUri))
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.player

import androidx.compose.foundation.layout.height
import kotlin.math.roundToInt
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.composed
import com.allemulator.core.emulation.AspectMode
import com.allemulator.core.designsystem.glass.glass
import kotlin.coroutines.resume
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import android.view.PixelCopy
import android.os.Looper
import android.os.Handler
import android.graphics.Bitmap
import com.allemulator.core.emulation.tr
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.input.InputManager
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
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
import com.allemulator.core.emulation.AutoTuner
import com.allemulator.core.emulation.CoreOptions
import com.allemulator.core.emulation.GameSystem
import com.allemulator.core.emulation.PerformanceSettings
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

    /**
     * true = ผู้ใช้กำลังใช้จอยจริงอยู่ → ซ่อนจอยบนจอ
     * เปลี่ยนเป็น true เมื่อ: เปิดเกมตอนต่อจอยอยู่ / เสียบจอยระหว่างเล่น / กดปุ่มบนจอยจริง
     * เปลี่ยนเป็น false เมื่อ: แตะหน้าจอ / ถอดจอยออก
     */
    private val gamepadActive = mutableStateOf(false)

    private val inputListener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(deviceId: Int) {
            if (InputDevice.getDevice(deviceId)?.let { !it.isVirtual && isGamepad(it.sources) } == true) {
                gamepadActive.value = true
            }
        }
        override fun onInputDeviceRemoved(deviceId: Int) {
            if (!hasPhysicalGamepad()) gamepadActive.value = false
        }
        override fun onInputDeviceChanged(deviceId: Int) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemBars()
        gamepadActive.value = hasPhysicalGamepad()
        getSystemService(InputManager::class.java).registerInputDeviceListener(inputListener, null)

        setContent {
            // หน้าเล่นเกมใช้โทนมืดเสมอ (จอเกมเด่น ไม่แสบตา) ไม่ว่าจะตั้งธีมแอพเป็นอะไร
            AllEmulatorTheme(forceDark = true) {
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
        // core ถูกแตกไฟล์ไว้ที่ nativeLibraryDir (ตั้ง useLegacyPackaging = true ใน app/build.gradle.kts)
        val corePath = "${applicationInfo.nativeLibraryDir}/${s.system.coreLibrary}"
        val data = GLRetroViewData(this).apply {
            coreFilePath = corePath
            when (val r = s.resolved) {
                is ResolvedGame.Virtual -> gameVirtualFiles = r.files
                is ResolvedGame.Path -> gameFilePath = r.path
            }
            systemDirectory = s.systemDir.absolutePath
            savesDirectory = s.savesDir.absolutePath
            variables = CoreOptions.build(s.system, s.performance).map { Variable(it.key, it.value) }.toTypedArray()
            saveRAMState = s.sram
            shader = shaderConfig(s.performance.shader)
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
        // ภาพหน้าจอตอนเปิดเมนู → ใช้ทำพื้นหลังเบลอให้เมนูเป็นกระจก
        var menuSnapshot by remember { mutableStateOf<Bitmap?>(null) }
        var fastForward by remember { mutableStateOf(false) }
        // จอยบนจอซ่อนอัตโนมัติเมื่อใช้จอยจริง (แตะจอเมื่อไหร่ก็โผล่กลับมา)
        val padVisible = !gamepadActive.value
        // เปิดเมนู: ถ่ายภาพจอก่อน (ก่อนเมนูขึ้น ภาพจะได้ไม่ติดตัวเมนู) แล้วค่อยเปิด
        val openMenu: () -> Unit = {
            scope.launch {
                menuSnapshot = captureScreen()
                menuOpen = true
            }
        }
        var perf by remember { mutableStateOf(PerfSnapshot()) }
        // ค่าที่ปรับได้ระหว่างเล่น (เมนู → ตั้งค่า)
        var performance by remember { mutableStateOf(s.performance) }
        var overlay by remember { mutableStateOf(s.overlay) }
        // สั่นเมื่อกดปุ่ม: ตั้งในหน้าตั้งค่าแอพที่เดียว (ไม่ซ้ำในเมนูเกมแล้ว)
        val haptics = s.haptics
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
        // ---- วัดประสิทธิภาพทุก 1 วินาที → แสดงบน overlay + ปรับความลื่นอัตโนมัติ (AutoTuner) ----
        val tuner = remember { AutoTuner(s.system) }
        val debuggable = applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
        var tuneMessage by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(Unit) {
            while (isActive) {
                delay(1_000)
                perf = withContext(Dispatchers.Default) { monitor.sample() }
                // debug build: log ไว้วัดผลผ่าน adb logcat -s AllEmuPerf (ใช้ตอนปรับความลื่น)
                if (debuggable) android.util.Log.i("AllEmuPerf", "${s.system.id} fps=${perf.fps} cpu=${perf.cpuPercent}")
                // ตอนเปิดเมนู (เกมหยุด) หรือเร่งความเร็ว FPS ไม่ใช่ค่าจริง → ไม่นับ
                if (menuOpen || fastForward) {
                    tuner.reset()
                    continue
                }
                tuner.onSample(perf.fps, performance)?.let { step ->
                    applyPerformance(view, s.system, performance, step.settings)
                    performance = step.settings
                    viewModel.savePerformance(step.settings) // เปิดเกมครั้งหน้าเริ่มที่ค่านี้เลย
                    tuneMessage = step.message
                    android.util.Log.i("AllEmuPerf", "autotune: ${step.message}")
                }
            }
        }
        // ข้อความ "ปรับอัตโนมัติ" หายเองใน 4 วินาที
        LaunchedEffect(tuneMessage) {
            if (tuneMessage != null) { delay(4_000); tuneMessage = null }
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

        BackHandler { if (menuOpen) menuOpen = false else openMenu() }

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

        val body = consoleBody(s.system)
        Box(Modifier.fillMaxSize().then(if (body != null) Modifier.background(body) else Modifier)) {
            // แนวตั้ง: จอเกมอยู่ด้านบน / แนวนอน: เต็มจอ
            val gameHeight = when {
                landscape -> 1f
                else -> 0.5f
            }
            // สัดส่วนภาพที่ผู้ใช้เลือก (ตามเกม / เต็มจอ / 16:9 / 4:3) — บรีฟรอบ 5 ข้อ 3
            val mode = performance.aspect
            val stretch = mode != AspectMode.ORIGINAL
            val contentAspect = contentAspect(s.system, performance)
            if (body != null) {
                // เครื่อง Nintendo: จอเกมอยู่ในกรอบจอ บนบอดี้สีของเครื่อง
                BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
                    val areaW = maxWidth - 24.dp
                    val areaH = maxHeight * gameHeight - 24.dp
                    val aspect = when (mode) {
                        AspectMode.ORIGINAL -> screenAspect(s.system)
                        AspectMode.FULL -> areaW / areaH
                        else -> mode.ratio!!
                    }
                    val w = minOf(areaW, areaH * aspect)
                    Box(
                        Modifier
                            .align(if (landscape) Alignment.Center else Alignment.TopCenter)
                            .padding(top = if (landscape) 0.dp else 12.dp)
                            .size(w + 16.dp, w / aspect + 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF15151C)) // กรอบจอ (สีเข้มเหมือนกระจกหน้าจอ)
                            .padding(8.dp),
                    ) {
                        AndroidView(factory = { view }, modifier = Modifier.fillMaxSize().stretchBuffer(view, stretch, contentAspect))
                    }
                }
            } else {
                BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
                    val areaW = maxWidth
                    val areaH = maxHeight * gameHeight
                    val target = when (mode) {
                        AspectMode.ORIGINAL -> null
                        AspectMode.FULL -> areaW / areaH
                        else -> mode.ratio
                    }
                    Box(Modifier.fillMaxWidth().height(areaH), contentAlignment = Alignment.Center) {
                        val viewSize = if (target == null) {
                            Modifier.fillMaxSize()
                        } else {
                            val w = minOf(areaW, areaH * target)
                            Modifier.size(w, w / target)
                        }
                        AndroidView(factory = { view }, modifier = viewSize.stretchBuffer(view, stretch, contentAspect))
                    }
                }
            }

            if (padVisible) {
                VirtualGamepad(
                    buttons = s.layout.forOrientation(landscape),
                    listener = listener,
                    haptics = haptics,
                    modifier = Modifier.safeDrawingPadding(),
                )
            }

            if (overlay.anyEnabled) {
                PerfOverlay(perf, overlay, Modifier.safeDrawingPadding().padding(8.dp))
            }

            // แจ้งว่าแอพเพิ่งปรับความลื่นให้อัตโนมัติ
            tuneMessage?.let { msg ->
                Text(
                    msg,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                        .glass(RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }

            // ปุ่มเปิดเมนู (มุมขวาบน)
            GlassIconButton(
                Icons.Rounded.Menu, tr("เมนู", "Menu"), openMenu,
                modifier = Modifier.align(Alignment.TopCenter).safeDrawingPadding().padding(top = 4.dp),
                size = 36,
            )

            if (menuOpen) {
                PauseMenu(
                    title = s.game.title,
                    snapshot = menuSnapshot,
                    fastForward = fastForward,
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
                    onReset = { menuOpen = false; scope.launch { delay(100); view.reset() } },
                    onExit = {
                        // ต้อง resume ก่อน เพื่อให้ thread ของ emulator ทำงานเซฟได้
                        view.onResume()
                        scope.launch { saveSram(); finish() }
                    },
                    settingsContent = {
                        InGameSettings(
                            system = s.system,
                            performance = performance,
                            overlay = overlay,
                            onPerformance = { new ->
                                applyPerformance(view, s.system, performance, new)
                                performance = new
                                viewModel.savePerformance(new)
                            },
                            onOverlay = { overlay = it; viewModel.saveOverlay(it) },
                        )
                    },
                )
            }
        }

        error?.let { ErrorDialog(it) }
    }

    /**
     * ถ่ายภาพหน้าจอทั้งหน้าต่าง (รวมจอเกม OpenGL) ด้วย PixelCopy — มีตั้งแต่ Android 8
     * ถ่ายไม่ได้/ช้าเกิน 300 ms → null (เมนูจะใช้พื้นมืดโปร่งแทน)
     */
    private suspend fun captureScreen(): Bitmap? {
        val decor = window.decorView
        if (decor.width <= 0 || decor.height <= 0) return null
        val bitmap = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
        return withTimeoutOrNull(300) {
            suspendCancellableCoroutine { cont ->
                runCatching {
                    PixelCopy.request(window, bitmap, { result ->
                        if (cont.isActive) cont.resume(if (result == PixelCopy.SUCCESS) bitmap else null)
                    }, Handler(Looper.getMainLooper()))
                }.onFailure { if (cont.isActive) cont.resume(null) }
            }
        }
    }

    private fun shaderConfig(shader: ScreenShader): ShaderConfig = when (shader) {
        ScreenShader.NONE -> ShaderConfig.Default
        ScreenShader.SHARP -> ShaderConfig.Sharp
        ScreenShader.LCD -> ShaderConfig.LCD
        ScreenShader.CRT -> ShaderConfig.CRT
    }

    /** ใช้ค่าความลื่น/ฟิลเตอร์ใหม่กับเกมที่กำลังเล่นทันที */
    private fun applyPerformance(view: GLRetroView, system: GameSystem, old: PerformanceSettings, new: PerformanceSettings) {
        if (old.shader != new.shader) view.shader = shaderConfig(new.shader)
        val vars = CoreOptions.build(system, new).map { Variable(it.key, it.value) }
        if (vars.isNotEmpty()) runCatching { view.updateVariables(*vars.toTypedArray()) }
    }

    /** สีบอดี้เครื่อง (เฉพาะเครื่อง Nintendo) — null = ใช้พื้นดำแบบเดิม */
    private fun consoleBody(system: GameSystem): Brush? = when (system) {
        GameSystem.GB, GameSystem.GBC, GameSystem.GBA, GameSystem.NES -> Brush.verticalGradient(
            listOf(
                lerp(Color(system.accentStart), Color.Black, 0.30f),
                lerp(Color(system.accentEnd), Color.Black, 0.55f),
            ),
        )
        else -> null
    }

    /** สัดส่วนจอ (กว้าง/สูง) ของแต่ละเครื่อง — ใช้ทำกรอบจอให้พอดีภาพ ไม่มีแถบดำ */
    /**
     * สัดส่วนภาพที่ core วาดออกมาจริง (ใช้คำนวณขนาด buffer ตอนยืดภาพ)
     * (PS2 ส่งภาพ 4:3 เสมอ แม้ตั้ง 16:9 — แอพยืดเอง ดู CoreOptions)
     */
    private fun contentAspect(system: GameSystem, p: PerformanceSettings): Float = when (system) {
        GameSystem.PSP -> 480f / 272f
        GameSystem.PS2 -> 4f / 3f
        else -> screenAspect(system)
    }

    /**
     * ยืดภาพเกมให้เต็มกรอบตามสัดส่วนที่เลือก
     *
     * LibretroDroid ใส่ขอบดำให้ภาพคงสัดส่วนเดิมของเกมเสมอ (ไม่มีคำสั่งให้ยืด)
     * วิธีแก้: ให้ GL วาดลง buffer ที่มีสัดส่วนเท่าภาพเกมพอดี (เลยไม่มีขอบดำ)
     * แล้วให้ระบบ Android ยืด buffer นั้นให้เต็มขนาดกรอบเอง (setFixedSize) — ไม่ต้องแก้ LibretroDroid
     */
    private fun Modifier.stretchBuffer(view: GLRetroView, stretch: Boolean, contentAspect: Float): Modifier = composed {
        var size by remember { mutableStateOf(IntSize.Zero) }
        LaunchedEffect(size, stretch, contentAspect) {
            if (size.height <= 0) return@LaunchedEffect
            if (stretch) {
                view.holder.setFixedSize((size.height * contentAspect).roundToInt().coerceAtLeast(1), size.height)
            } else {
                view.holder.setSizeFromLayout()
            }
        }
        onSizeChanged { size = it }
    }

    private fun screenAspect(system: GameSystem): Float = when (system) {
        GameSystem.GB, GameSystem.GBC -> 160f / 144f
        GameSystem.GBA -> 240f / 160f
        GameSystem.NES -> 4f / 3f
        else -> 4f / 3f
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
            gamepadActive.value = true // กดจอยจริง → ซ่อนจอยบนจอ
            return when (event.action) {
                KeyEvent.ACTION_DOWN -> v.onKeyDown(event.keyCode, event)
                KeyEvent.ACTION_UP -> v.onKeyUp(event.keyCode, event)
                else -> super.dispatchKeyEvent(event)
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent): Boolean {
        retroView?.let {
            if (isGamepad(ev.source)) {
                gamepadActive.value = true
                return it.onGenericMotionEvent(ev)
            }
        }
        return super.dispatchGenericMotionEvent(ev)
    }

    /** แตะหน้าจอ → โชว์จอยบนจอกลับมา (กรณีถูกซ่อนเพราะใช้จอยจริง) */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN && gamepadActive.value) {
            gamepadActive.value = false
            return true // แตะแรกแค่เรียกจอยกลับมา ไม่กดปุ่มใดๆ
        }
        return super.dispatchTouchEvent(ev)
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
        if (isFinishing) exitedGame = true
        getSystemService(InputManager::class.java).unregisterInputDeviceListener(inputListener)
        retroView = null
        super.onDestroy()
    }

    private fun errorMessage(code: Int, system: GameSystem): String = when (code) {
        GLRetroView.ERROR_LOAD_LIBRARY -> tr("ไม่พบ emulator ของ ${system.displayName} ในแอพ (ไฟล์ ${system.coreLibrary}) — ติดตั้งแอพเวอร์ชันเต็มหรือรัน scripts/fetch-cores.sh", "The ${system.displayName} emulator is missing (${system.coreLibrary}) — install the full app or run scripts/fetch-cores.sh")
        GLRetroView.ERROR_LOAD_GAME -> tr("เปิดไฟล์เกมไม่ได้ ไฟล์อาจเสีย หรือไม่ใช่เกม ${system.displayName}", "Can't open the game. The file may be damaged or isn't a ${system.displayName} game") +
            (system.biosNote?.let { tr("\n\nหมายเหตุ: $it", "\n\nNote: $it") } ?: "")
        GLRetroView.ERROR_GL_NOT_COMPATIBLE -> tr("การ์ดจอของมือถือเครื่องนี้ไม่รองรับ ${system.displayName}", "This phone's GPU doesn't support ${system.displayName}")
        GLRetroView.ERROR_SERIALIZATION -> tr("เซฟ/โหลดไม่สำเร็จ", "Save/load failed")
        else -> tr("เกิดข้อผิดพลาดใน emulator (รหัส $code)", "Emulator error (code $code)")
    }

    @Composable
    private fun ErrorDialog(message: String) {
        AlertDialog(
            onDismissRequest = { finish() },
            title = { Text(tr("เล่นเกมไม่ได้", "Can't play this game")) },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { finish() }) { Text(tr("กลับ", "Back")) } },
        )
    }

    companion object {
        const val EXTRA_GAME_URI = "gameUri"
        const val EXTRA_FORCE_COPY = "forceCopy"

        /** ผู้ใช้เพิ่งออกจากเกม → หน้าหลักใช้ตัดสินใจโชว์โฆษณา (จังหวะพัก) */
        @Volatile private var exitedGame = false

        /** อ่านแล้วล้างค่า: true = เพิ่งออกจากเกม */
        fun consumeExitedFlag(): Boolean = exitedGame.also { exitedGame = false }

        /** สร้าง Intent สำหรับเปิดเกม */
        fun intent(context: Context, gameUri: String, forceCopy: Boolean = false): Intent =
            Intent(context, GameActivity::class.java)
                .putExtra(EXTRA_GAME_URI, gameUri)
                .putExtra(EXTRA_FORCE_COPY, forceCopy)
    }
}
