# Graph Report - ALL Emulator Project  (2026-11-26)

## Corpus Check
- 72 files · ~26,508 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 29 file(s) not represented in the graph (top: .xml 11, .pro 9, (none) 4)
- WARNING: 29 of 107 files seen (27%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 904 nodes · 2189 edges · 57 communities (25 shown, 32 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 21 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `54d97f11`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- LayoutEditor.kt
- GameDao
- GameSystem
- สถาปัตยกรรมของ ALL Emulator
- AppSettingsScreen.kt
- LibraryScreen.kt
- GameEntity
- PlayerOverlays.kt
- GlassComponents.kt
- ConsoleIcon
- GameScanner.kt
- BillingManager.kt
- AdManager.kt
- GameEntity
- SettingsRepository
- LiquidBackground.kt
- HomeScreen.kt
- LanguageSwitch.kt
- Inject
- Tutorial.kt
- Glass.kt
- GlassScaffold.kt
- GameFileResolver.kt
- PerformanceSettings
- HomeViewModel.kt
- Theme.kt
- SnapshotGlass.kt
- make-console-icons.py
- SystemSettingsScreen.kt
- build-cores.sh
- CLAUDE.md
- fetch-cores.sh
- graphify.sh
- GameActivity
- PerformanceMonitor
- AppNavHost.kt
- AppLanguage
- AllEmulatorApp.kt
- ThemeMode
- CoverImage.kt

## God Nodes (most connected - your core abstractions)
1. `GameSystem` - 58 edges
2. `SettingsRepository` - 42 edges
3. `GameEntity` - 42 edges
4. `tr()` - 42 edges
5. `GameActivity` - 30 edges
6. `GameRepository` - 27 edges
7. `PerformanceSettings` - 25 edges
8. `GameDao` - 23 edges
9. `LibraryViewModel` - 21 edges
10. `AppSettingsScreen()` - 20 edges

## Surprising Connections (you probably didn't know these)
- `AllEmulatorApp` --references--> `SettingsRepository`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AllEmulatorApp.kt → core/data/src/main/java/com/allemulator/core/data/SettingsRepository.kt
- `AppNavHost()` --calls--> `AllEmulatorTheme()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → core/designsystem/src/main/java/com/allemulator/core/designsystem/theme/Theme.kt
- `AppNavHost()` --calls--> `HomeScreen()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/home/src/main/java/com/allemulator/feature/home/HomeScreen.kt
- `AppNavHost()` --calls--> `TutorialOverlay()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/home/src/main/java/com/allemulator/feature/home/Tutorial.kt
- `AppNavHost()` --calls--> `LibraryScreen()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/library/src/main/java/com/allemulator/feature/library/LibraryScreen.kt

## Import Cycles
- None detected.

## Communities (57 total, 32 thin omitted)

### Community 0 - "LayoutEditor.kt"
Cohesion: 0.05
Nodes (24): ControllerLayouts, ButtonKind, ANALOG, DPAD, FACE, SHOULDER, SMALL, ControllerLayout (+16 more)

### Community 2 - "GameDao"
Cohesion: 0.09
Nodes (4): AppDatabase, GameDao, SystemCount, DataModule

### Community 3 - "GameSystem"
Cohesion: 0.10
Nodes (8): GameRepository, GameSystem, GB, GBA, GBC, NES, PS1, PSP

### Community 4 - "สถาปัตยกรรมของ ALL Emulator"
Cohesion: 0.07
Nodes (25): 1. ภาพรวมโมดูล, 2. ไฟล์สำคัญ — "อยากแก้ X ไปที่ไฟล์ไหน", 3. เส้นทางตอนกดเล่นเกม, 4. การเก็บข้อมูล, 5. Liquid Glass ทำงานยังไง, 6. ข้อจำกัดที่ควรรู้, สถาปัตยกรรมของ Emu-X, Emulator cores ที่ใช้ (+17 more)

### Community 5 - "AppSettingsScreen.kt"
Cohesion: 0.15
Nodes (4): AppSettingsScreen(), AppSettingsViewModel, findActivity(), openUrl()

### Community 6 - "LibraryScreen.kt"
Cohesion: 0.12
Nodes (6): tr(), EmptyState(), FoldersDialog(), formatSize(), GameRow(), LibraryScreen()

### Community 7 - "GameEntity"
Cohesion: 0.16
Nodes (5): PlayerState, Error, Loading, Ready, PlayerViewModel

### Community 8 - "PlayerOverlays.kt"
Cohesion: 0.15
Nodes (3): PerfSnapshot, PauseMenu(), PerfOverlay()

### Community 9 - "GlassComponents.kt"
Cohesion: 0.22
Nodes (9): BetaBadge(), GlassButton(), GlassCard(), GlassFab(), GlassIconButton(), GlassSection(), GlassTopBar(), pressScale() (+1 more)

### Community 11 - "GameScanner.kt"
Cohesion: 0.18
Nodes (3): GameScanner, Child, CleanTitleTest

### Community 14 - "AdManager.kt"
Cohesion: 0.13
Nodes (3): AdManager, FullScreenContentCallback, InterstitialAdLoadCallback

### Community 15 - "GameEntity"
Cohesion: 0.13
Nodes (4): FolderEntity, GameEntity, LibraryUiState, LibraryViewModel

### Community 18 - "HomeScreen.kt"
Cohesion: 0.18
Nodes (3): HomeScreen(), RecentRow(), SystemCard()

### Community 21 - "Tutorial.kt"
Cohesion: 0.17
Nodes (4): pages(), TutorialOverlay(), TutorialPage, TutorialViewModel

### Community 24 - "GameFileResolver.kt"
Cohesion: 0.16
Nodes (8): GameFileResolver, LoadStrategy, COPY, FD_PATH, VFS, ResolvedGame, Path, Virtual

### Community 25 - "PerformanceSettings"
Cohesion: 0.13
Nodes (6): AutoTuner, Step, CoreOptions, PerformanceSettings, AutoTunerTest, CoreOptionsTest

### Community 26 - "HomeViewModel.kt"
Cohesion: 0.28
Nodes (3): HomeUiState, HomeViewModel, SystemCardUi

### Community 27 - "Theme.kt"
Cohesion: 0.11
Nodes (4): AllEmulatorTheme(), colorSchemeOf(), GlassColors, GlassPalette

### Community 29 - "make-console-icons.py"
Cohesion: 0.22
Nodes (5): cut_bg(), erase(), load(), near(), save()

### Community 31 - "SystemSettingsScreen.kt"
Cohesion: 0.08
Nodes (22): ChipChoice(), NavRow(), SwitchRow(), OverlayCorner, BOTTOM_END, BOTTOM_START, TOP_END, TOP_START (+14 more)

### Community 32 - "build-cores.sh"
Cohesion: 0.80
Nodes (4): build_cmake(), build_ndk_build(), install_so(), build-cores.sh script

### Community 51 - "PerformanceMonitor"
Cohesion: 0.08
Nodes (3): CoverStore, PerformanceMonitor, SystemAssets

### Community 52 - "AppNavHost.kt"
Cohesion: 0.17
Nodes (6): AppNavHost(), AppSettingsRoute, HomeRoute, LayoutEditorRoute, LibraryRoute, SystemSettingsRoute

### Community 53 - "AppLanguage"
Cohesion: 0.21
Nodes (4): AppLanguage, EN, TH, L10n

### Community 55 - "ThemeMode"
Cohesion: 0.24
Nodes (5): ThemeMode, DARK, LIGHT, SYSTEM, ThemePrefs

## Knowledge Gaps
- **35 isolated node(s):** `PSP`, `PS1`, `GB`, `GBC`, `GBA` (+30 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 261 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **32 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GameSystem` connect `GameSystem` to `LayoutEditor.kt`, `GameActivity.kt`, `LibraryScreen.kt`, `GameEntity`, `ConsoleIcon`, `GameScanner.kt`, `GameActivity`, `GameEntity`, `SettingsRepository`, `HomeScreen.kt`, `GameFileResolver.kt`, `PerformanceSettings`, `HomeViewModel.kt`, `.applyPerformance`, `SystemSettingsScreen.kt`?**
  _High betweenness centrality (0.137) - this node is a cross-community bridge._
- **What connects `PSP`, `PS1`, `GB` to the rest of the system?**
  _35 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `LayoutEditor.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05263157894736842 - nodes in this community are weakly interconnected._
- **Why does `tr()` connect `LibraryScreen.kt` to `LayoutEditor.kt`, `GameActivity.kt`, `AppSettingsScreen.kt`, `GameEntity`, `PlayerOverlays.kt`, `GameActivity`, `BillingManager.kt`, `GameEntity`, `HomeScreen.kt`, `Tutorial.kt`, `AppLanguage`, `GameFileResolver.kt`, `PerformanceSettings`, `HomeViewModel.kt`, `SystemSettingsScreen.kt`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **Should `GameActivity.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.08 - nodes in this community are weakly interconnected._
- **Why does `GameEntity` connect `GameEntity` to `GameDao`, `GameSystem`, `LibraryScreen.kt`, `GameEntity`, `GameScanner.kt`, `HomeScreen.kt`, `AppNavHost.kt`, `GameFileResolver.kt`, `HomeViewModel.kt`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Should `GameDao` be split into smaller, more focused modules?**
  _Cohesion score 0.09175377468060394 - nodes in this community are weakly interconnected._