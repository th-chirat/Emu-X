/*
 * AppNavHost.kt
 * -----------------------------------------------------------------------------
 * "แผนที่" ของแอพ — บอกว่ามีหน้าอะไรบ้าง และกดอะไรแล้วไปหน้าไหน
 *
 *   Home ──กดการ์ด──▶ Library ──กดเฟือง──▶ SystemSettings ──▶ LayoutEditor
 *    │                  │
 *    │                  └─กดเกม──▶ GameActivity (หน้าเล่นเกม แยก Activity)
 *    └─กดเฟือง──▶ AppSettings
 *
 * แต่ละหน้า (route) เป็น class ที่ @Serializable — ส่งพารามิเตอร์ได้แบบปลอดภัย (type-safe)
 * ชื่อพารามิเตอร์ "systemId" ต้องตรงกับที่ ViewModel อ่านจาก SavedStateHandle
 * -----------------------------------------------------------------------------
 */
package com.allemulator.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.allemulator.feature.home.TutorialOverlay
import com.allemulator.feature.home.TutorialViewModel
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.allemulator.core.data.db.GameEntity
import com.allemulator.feature.home.HomeScreen
import com.allemulator.feature.library.LibraryScreen
import com.allemulator.feature.player.gamepad.LayoutEditorScreen
import com.allemulator.feature.settings.AppSettingsScreen
import com.allemulator.feature.settings.SystemSettingsScreen
import kotlinx.serialization.Serializable

// ---------------- รายชื่อหน้าทั้งหมด ----------------
@Serializable object HomeRoute
@Serializable data class LibraryRoute(val systemId: String)
@Serializable data class SystemSettingsRoute(val systemId: String)
@Serializable data class LayoutEditorRoute(val systemId: String)
@Serializable object AppSettingsRoute

@Composable
fun AppNavHost(
    onPlayGame: (GameEntity) -> Unit,
    versionName: String,
    privacyUrl: String,
    sourceUrl: String,
) {
    val nav = rememberNavController()
    val tutorial: TutorialViewModel = hiltViewModel()
    val showTutorial by tutorial.show.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
    NavHost(
        navController = nav,
        startDestination = HomeRoute,
        // แอนิเมชันเปลี่ยนหน้าแบบ "ของเหลว": ขยาย + จางเข้า
        enterTransition = { fadeIn() + scaleIn(initialScale = 0.96f) },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() + scaleOut(targetScale = 0.96f) },
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onOpenSystem = { nav.navigate(LibraryRoute(it.id)) },
                onPlayGame = onPlayGame,
                onOpenSettings = { nav.navigate(AppSettingsRoute) },
            )
        }
        composable<LibraryRoute> {
            LibraryScreen(
                onBack = { nav.popBackStack() },
                onPlay = onPlayGame,
                onOpenSystemSettings = { nav.navigate(SystemSettingsRoute(it.id)) },
            )
        }
        composable<SystemSettingsRoute> {
            SystemSettingsScreen(
                onBack = { nav.popBackStack() },
                onEditLayout = { nav.navigate(LayoutEditorRoute(it.id)) },
            )
        }
        composable<LayoutEditorRoute> {
            LayoutEditorScreen(onBack = { nav.popBackStack() })
        }
        composable<AppSettingsRoute> {
            AppSettingsScreen(
                onBack = { nav.popBackStack() },
                versionName = versionName,
                privacyUrl = privacyUrl,
                sourceUrl = sourceUrl,
            )
        }
    }
        // หน้าสอนใช้งานครั้งแรก (ข้ามได้) — วางทับทุกหน้า
        if (showTutorial == true) TutorialOverlay(onDone = { tutorial.finish() })
    }
}
