# Graph Report - ALL Emulator Project  (2026-11-20)

## Corpus Check
- 61 files · ~20,602 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 26 file(s) not represented in the graph (top: .xml 10, .pro 9, (none) 2)
- WARNING: 26 of 87 files seen (30%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 745 nodes · 1709 edges · 50 communities (19 shown, 31 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 24 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- LayoutEditor.kt
- GameActivity.kt
- GameDao
- GameSystem
- สถาปัตยกรรมของ ALL Emulator
- AppSettingsScreen.kt
- LibraryScreen.kt
- GameFileResolver.kt
- PlayerOverlays.kt
- GlassComponents.kt
- ConsoleIcon
- GameScanner.kt
- PlayerViewModel.kt
- BillingManager.kt
- LiquidBackground.kt
- GameEntity
- SettingsRepository
- OverlaySettings
- AdManager.kt
- SettingsWidgets.kt
- AppNavHost.kt
- HomeScreen.kt
- Glass.kt
- GlassScaffold.kt
- Inject
- PerformanceSettings
- AdManager
- HomeViewModel.kt
- SystemAssets.kt
- BillingClientStateListener
- SystemSettingsViewModel
- build-cores.sh
- AllEmulatorApp.kt
- CLAUDE.md
- fetch-cores.sh
- graphify.sh

## God Nodes (most connected - your core abstractions)
1. `GameSystem` - 53 edges
2. `GameEntity` - 37 edges
3. `SettingsRepository` - 31 edges
4. `GameRepository` - 23 edges
5. `GameDao` - 23 edges
6. `GameActivity` - 22 edges
7. `GlassIconButton()` - 18 edges
8. `ConsoleIcon()` - 17 edges
9. `BillingManager` - 17 edges
10. `Painter` - 16 edges

## Surprising Connections (you probably didn't know these)
- `AppNavHost()` --calls--> `HomeScreen()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/home/src/main/java/com/allemulator/feature/home/HomeScreen.kt
- `AppNavHost()` --calls--> `LibraryScreen()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/library/src/main/java/com/allemulator/feature/library/LibraryScreen.kt
- `AppNavHost()` --calls--> `LayoutEditorScreen()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/player/src/main/java/com/allemulator/feature/player/gamepad/LayoutEditor.kt
- `AppNavHost()` --calls--> `AppSettingsScreen()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/settings/src/main/java/com/allemulator/feature/settings/AppSettingsScreen.kt
- `AppNavHost()` --calls--> `SystemSettingsScreen()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/settings/src/main/java/com/allemulator/feature/settings/SystemSettingsScreen.kt

## Import Cycles
- None detected.

## Communities (50 total, 31 thin omitted)

### Community 0 - "LayoutEditor.kt"
Cohesion: 0.05
Nodes (24): ControllerLayouts, ButtonKind, ANALOG, DPAD, FACE, SHOULDER, SMALL, ControllerLayout (+16 more)

### Community 1 - "GameActivity.kt"
Cohesion: 0.05
Nodes (4): GameActivity, PerformanceMonitor, PerfSnapshot, PerfOverlay()

### Community 2 - "GameDao"
Cohesion: 0.09
Nodes (4): AppDatabase, GameDao, SystemCount, DataModule

### Community 3 - "GameSystem"
Cohesion: 0.11
Nodes (9): GameRepository, GameSystem, GB, GBC, NDS, NES, PS1, PS2 (+1 more)

### Community 4 - "สถาปัตยกรรมของ ALL Emulator"
Cohesion: 0.07
Nodes (25): 1. ภาพรวมโมดูล, 2. ไฟล์สำคัญ — "อยากแก้ X ไปที่ไฟล์ไหน", 3. เส้นทางตอนกดเล่นเกม, 4. การเก็บข้อมูล, 5. Liquid Glass ทำงานยังไง, 6. ข้อจำกัดที่ควรรู้, สถาปัตยกรรมของ ALL Emulator, Emulator cores ที่ใช้ (+17 more)

### Community 5 - "AppSettingsScreen.kt"
Cohesion: 0.10
Nodes (3): AppSettingsViewModel, findActivity(), openUrl()

### Community 6 - "LibraryScreen.kt"
Cohesion: 0.11
Nodes (6): BetaWarning(), EmptyState(), FoldersDialog(), formatSize(), GameRow(), LibraryScreen()

### Community 7 - "GameFileResolver.kt"
Cohesion: 0.16
Nodes (8): GameFileResolver, LoadStrategy, COPY, FD_PATH, VFS, ResolvedGame, Path, Virtual

### Community 9 - "GlassComponents.kt"
Cohesion: 0.20
Nodes (9): BetaBadge(), GlassButton(), GlassCard(), GlassFab(), GlassIconButton(), GlassSection(), GlassTopBar(), pressScale() (+1 more)

### Community 11 - "GameScanner.kt"
Cohesion: 0.17
Nodes (3): GameScanner, Child, CleanTitleTest

### Community 12 - "PlayerViewModel.kt"
Cohesion: 0.15
Nodes (5): PlayerState, Error, Loading, Ready, PlayerViewModel

### Community 15 - "GameEntity"
Cohesion: 0.17
Nodes (4): FolderEntity, GameEntity, LibraryUiState, LibraryViewModel

### Community 17 - "OverlaySettings"
Cohesion: 0.15
Nodes (15): OverlayCorner, BOTTOM_END, BOTTOM_START, TOP_END, TOP_START, OverlaySettings, PerformancePreset, BALANCED (+7 more)

### Community 19 - "SettingsWidgets.kt"
Cohesion: 0.17
Nodes (4): ChipChoice(), NavRow(), SwitchRow(), SystemSettingsScreen()

### Community 20 - "AppNavHost.kt"
Cohesion: 0.22
Nodes (6): AppNavHost(), AppSettingsRoute, HomeRoute, LayoutEditorRoute, LibraryRoute, SystemSettingsRoute

### Community 21 - "HomeScreen.kt"
Cohesion: 0.18
Nodes (3): HomeScreen(), RecentRow(), SystemCard()

### Community 25 - "PerformanceSettings"
Cohesion: 0.29
Nodes (3): CoreOptions, PerformanceSettings, CoreOptionsTest

### Community 28 - "HomeViewModel.kt"
Cohesion: 0.29
Nodes (3): HomeUiState, HomeViewModel, SystemCardUi

### Community 32 - "build-cores.sh"
Cohesion: 0.80
Nodes (4): build_cmake(), build_ndk_build(), install_so(), build-cores.sh script

## Knowledge Gaps
- **31 isolated node(s):** `PSP`, `PS1`, `PS2`, `GB`, `GBC` (+26 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 226 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **31 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `GameSystem` connect `GameSystem` to `LayoutEditor.kt`, `GameActivity.kt`, `AppSettingsScreen.kt`, `LibraryScreen.kt`, `GameFileResolver.kt`, `ConsoleIcon`, `GameScanner.kt`, `PlayerViewModel.kt`, `GameEntity`, `SettingsRepository`, `HomeScreen.kt`, `PerformanceSettings`, `SettingsRepository.kt`, `HomeViewModel.kt`, `SystemSettingsViewModel`?**
  _High betweenness centrality (0.182) - this node is a cross-community bridge._
- **What connects `PSP`, `PS1`, `PS2` to the rest of the system?**
  _31 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `LayoutEditor.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05331510594668489 - nodes in this community are weakly interconnected._
- **Why does `GameEntity` connect `GameEntity` to `GameDao`, `GameSystem`, `LibraryScreen.kt`, `GameFileResolver.kt`, `GameScanner.kt`, `PlayerViewModel.kt`, `AppNavHost.kt`, `HomeScreen.kt`, `HomeViewModel.kt`?**
  _High betweenness centrality (0.108) - this node is a cross-community bridge._
- **Should `GameActivity.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05129561078794289 - nodes in this community are weakly interconnected._
- **Why does `SettingsRepository` connect `SettingsRepository` to `LayoutEditor.kt`, `AppSettingsScreen.kt`, `PlayerViewModel.kt`, `BillingManager.kt`, `AdManager.kt`, `AdManager`, `SettingsRepository.kt`, `SystemSettingsViewModel`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Should `GameDao` be split into smaller, more focused modules?**
  _Cohesion score 0.09175377468060394 - nodes in this community are weakly interconnected._