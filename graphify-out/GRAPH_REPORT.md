# Graph Report - ALL Emulator Project  (2026-11-26)

## Corpus Check
- 75 files · ~71,930 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 29 file(s) not represented in the graph (top: .xml 11, .pro 9, (none) 4)
- WARNING: 29 of 134 files seen (22%) have no supported extractor and are missing from this graph entirely. It may look complete while representing only part of the codebase.

## Summary
- 1629 nodes · 3028 edges · 129 communities (84 shown, 45 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 31 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8ec50e0a`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- LayoutEditor.kt
- GameDao
- GameRepository
- สถาปัตยกรรมของ ALL Emulator
- AppSettingsScreen.kt
- LibraryScreen.kt
- GameEntity
- PlayerOverlays.kt
- GlassComponents.kt
- PerformanceSettings
- GameScanner.kt
- GameActivity
- BillingManager.kt
- AdManager.kt
- GameEntity
- SettingsRepository
- LiquidBackground.kt
- retro_key
- LayoutEditor.kt
- Inject
- Tutorial.kt
- GameSystem
- retro_language
- GameFileResolver.kt
- OverlaySettings
- GlassScaffold.kt
- Theme.kt
- retro_get_proc_address_interface
- make-console-icons.py
- retro_vfs_interface
- SystemSettingsScreen.kt
- build-cores.sh
- ConsoleIcon
- CLAUDE.md
- fetch-cores.sh
- graphify.sh
- PerformanceMonitor
- CoverImage.kt
- AppLanguage
- AllEmulatorApp.kt
- ThemeMode
- HomeScreen.kt
- libretro.h
- retro_disk_control_ext_callback
- armsx2_shim.c
- retro_hw_render_callback
- retro_camera_callback
- retro_perf_callback
- retro_microphone_interface
- retro_netpacket_callback
- retro_led_interface
- .feed
- retro_hw_context_type
- retro_game_info_ext
- retro_location_callback
- retro_midi_interface
- retro_mod
- Glass.kt
- SnapshotGlass.kt
- retro_core_option_v2_definition
- retro_memory_descriptor
- retro_framebuffer
- retro_hw_render_interface_type
- retro_message_ext
- retro_sensor_action
- retro_subsystem_rom_info
- retro_perf_counter
- retro_av_enable_flags
- retro_core_option_definition
- retro_game_geometry
- retro_input_descriptor
- retro_log_level
- retro_power_state
- retro_savestate_context
- retro_subsystem_info
- retro_system_info
- retro_audio_callback
- retro_fastforwarding_override
- retro_game_info
- retro_message_type
- retro_pixel_format
- retro_sensor_interface
- retro_set_environment
- v_opendir
- retro_camera_buffer
- SystemAssets.kt
- retro_device_power
- retro_message_target
- retro_rumble_effect
- retro_system_content_info_override
- retro_audio_buffer_status_callback
- retro_controller_info
- .cleanTitle
- retro_core_option_value
- retro_core_options_intl
- retro_core_options_v2
- retro_controller_description
- retro_core_options_update_display_callback
- retro_hw_render_context_negotiation_interface_type
- retro_hw_render_interface
- retro_keyboard_callback
- retro_memory_map
- retro_message
- retro_rumble_interface
- retro_subsystem_memory_info
- retro_system_av_info
- retro_system_timing
- retro_variable
- retro_vfs_interface_info
- v_readdir
- retro_hw_render_context_negotiation_interface
- retro_throttle_state

## God Nodes (most connected - your core abstractions)
1. `retro_key` - 163 edges
2. `GameSystem` - 70 edges
3. `tr()` - 48 edges
4. `GameEntity` - 43 edges
5. `SettingsRepository` - 42 edges
6. `retro_vfs_interface` - 39 edges
7. `retro_language` - 38 edges
8. `GameActivity` - 32 edges
9. `PerformanceSettings` - 29 edges
10. `GameRepository` - 27 edges

## Surprising Connections (you probably didn't know these)
- `AllEmulatorApp` --references--> `SettingsRepository`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AllEmulatorApp.kt → core/data/src/main/java/com/allemulator/core/data/SettingsRepository.kt
- `AppNavHost()` --calls--> `HomeScreen()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/home/src/main/java/com/allemulator/feature/home/HomeScreen.kt
- `AppNavHost()` --calls--> `TutorialOverlay()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/home/src/main/java/com/allemulator/feature/home/Tutorial.kt
- `AppNavHost()` --calls--> `LibraryScreen()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/library/src/main/java/com/allemulator/feature/library/LibraryScreen.kt
- `AppNavHost()` --calls--> `LayoutEditorScreen()`  [EXTRACTED]
  app/src/main/java/com/allemulator/app/AppNavHost.kt → feature/player/src/main/java/com/allemulator/feature/player/gamepad/LayoutEditor.kt

## Import Cycles
- None detected.

## Communities (129 total, 45 thin omitted)

### Community 0 - "LayoutEditor.kt"
Cohesion: 0.06
Nodes (24): ControllerLayouts, ButtonKind, ANALOG, DPAD, FACE, SHOULDER, SMALL, ControllerLayout (+16 more)

### Community 2 - "GameDao"
Cohesion: 0.09
Nodes (4): AppDatabase, GameDao, SystemCount, DataModule

### Community 4 - "สถาปัตยกรรมของ ALL Emulator"
Cohesion: 0.05
Nodes (38): 1. ภาพรวมโมดูล, 2. ไฟล์สำคัญ — "อยากแก้ X ไปที่ไฟล์ไหน", 3. เส้นทางตอนกดเล่นเกม, 4. การเก็บข้อมูล, 5. Liquid Glass ทำงานยังไง, 6. ข้อจำกัดที่ควรรู้, สถาปัตยกรรมของ Emu-X, Emulator cores ที่ใช้ (+30 more)

### Community 5 - "AppSettingsScreen.kt"
Cohesion: 0.15
Nodes (5): AppSettingsScreen(), AppSettingsViewModel, Credit, findActivity(), openUrl()

### Community 6 - "LibraryScreen.kt"
Cohesion: 0.09
Nodes (8): BiosGuideDialog(), tr(), EmptyState(), EmptyStateContent(), FoldersDialog(), formatSize(), GameRow(), LibraryScreen()

### Community 7 - "GameEntity"
Cohesion: 0.17
Nodes (5): PlayerState, Error, Loading, Ready, PlayerViewModel

### Community 9 - "GlassComponents.kt"
Cohesion: 0.21
Nodes (9): BetaBadge(), GlassButton(), GlassCard(), GlassFab(), GlassIconButton(), GlassSection(), GlassTopBar(), pressScale() (+1 more)

### Community 10 - "PerformanceSettings"
Cohesion: 0.20
Nodes (5): AutoTuner, Step, CoreOptions, PerformanceSettings, CoreOptionsTest

### Community 14 - "AdManager.kt"
Cohesion: 0.13
Nodes (3): AdManager, FullScreenContentCallback, InterstitialAdLoadCallback

### Community 15 - "GameEntity"
Cohesion: 0.13
Nodes (4): FolderEntity, GameEntity, LibraryUiState, LibraryViewModel

### Community 18 - "retro_key"
Cohesion: 0.01
Nodes (163): retro_key, RETROK_0, RETROK_1, RETROK_2, RETROK_3, RETROK_4, RETROK_5, RETROK_6 (+155 more)

### Community 19 - "LayoutEditor.kt"
Cohesion: 0.08
Nodes (3): Label(), LanguageSwitch(), GlassColors

### Community 21 - "Tutorial.kt"
Cohesion: 0.16
Nodes (4): PageIcon(), pages(), TutorialOverlay(), TutorialPage

### Community 22 - "GameSystem"
Cohesion: 0.14
Nodes (8): GameSystem, GB, GBA, GBC, NES, PS1, PS2, PSP

### Community 23 - "retro_language"
Cohesion: 0.05
Nodes (38): retro_language, RETRO_LANGUAGE_ARABIC, RETRO_LANGUAGE_ASTURIAN, RETRO_LANGUAGE_BELARUSIAN, RETRO_LANGUAGE_BRITISH_ENGLISH, RETRO_LANGUAGE_CATALAN, RETRO_LANGUAGE_CATALAN_VALENCIA, RETRO_LANGUAGE_CHINESE_SIMPLIFIED (+30 more)

### Community 24 - "GameFileResolver.kt"
Cohesion: 0.16
Nodes (8): GameFileResolver, LoadStrategy, COPY, FD_PATH, VFS, ResolvedGame, Path, Virtual

### Community 25 - "OverlaySettings"
Cohesion: 0.12
Nodes (20): AspectMode, FULL, ORIGINAL, STANDARD, WIDE, OverlayCorner, BOTTOM_END, BOTTOM_START (+12 more)

### Community 27 - "Theme.kt"
Cohesion: 0.07
Nodes (10): AppNavHost(), AppSettingsRoute, HomeRoute, LayoutEditorRoute, LibraryRoute, SystemSettingsRoute, AllEmulatorTheme(), colorSchemeOf() (+2 more)

### Community 29 - "make-console-icons.py"
Cohesion: 0.22
Nodes (5): cut_bg(), erase(), load(), near(), save()

### Community 30 - "retro_vfs_interface"
Cohesion: 0.06
Nodes (15): retro_vfs_interface, close, dirent_get_name, dirent_is_dir, flush, get_path, open, read (+7 more)

### Community 31 - "SystemSettingsScreen.kt"
Cohesion: 0.20
Nodes (8): ChipChoice(), NavRow(), ScreenOptions(), SwitchRow(), InGameSettings(), Section(), SystemSettingsScreen(), SystemSettingsViewModel

### Community 32 - "build-cores.sh"
Cohesion: 0.80
Nodes (4): build_cmake(), build_ndk_build(), install_so(), build-cores.sh script

### Community 51 - "PerformanceMonitor"
Cohesion: 0.09
Nodes (6): CoverStore, HomeUiState, HomeViewModel, SystemCardUi, PerformanceMonitor, PerfSnapshot

### Community 53 - "AppLanguage"
Cohesion: 0.23
Nodes (4): AppLanguage, EN, TH, L10n

### Community 55 - "ThemeMode"
Cohesion: 0.21
Nodes (5): ThemeMode, DARK, LIGHT, SYSTEM, ThemePrefs

### Community 56 - "HomeScreen.kt"
Cohesion: 0.14
Nodes (3): HomeScreen(), RecentRow(), SystemCard()

### Community 57 - "libretro.h"
Cohesion: 0.07
Nodes (24): retro_core_option_display, key, visible, retro_core_option_v2_category, desc, info, key, retro_core_options_v2_intl (+16 more)

### Community 58 - "retro_disk_control_ext_callback"
Cohesion: 0.08
Nodes (19): retro_disk_control_callback, add_image_index, get_eject_state, get_image_index, get_num_images, replace_image_index, set_eject_state, set_image_index (+11 more)

### Community 59 - "armsx2_shim.c"
Cohesion: 0.12
Nodes (5): v_dirent_is_dir(), v_mkdir(), v_stat(), mkdir, stat

### Community 60 - "retro_hw_render_callback"
Cohesion: 0.12
Nodes (13): retro_hw_render_callback, bottom_left_origin, cache_context, context_destroy, context_reset, context_type, debug_context, depth (+5 more)

### Community 61 - "retro_camera_callback"
Cohesion: 0.13
Nodes (10): retro_camera_callback, caps, deinitialized, frame_opengl_texture, frame_raw_framebuffer, height, initialized, start (+2 more)

### Community 62 - "retro_perf_callback"
Cohesion: 0.13
Nodes (8): retro_perf_callback, get_cpu_features, get_perf_counter, get_time_usec, perf_log, perf_register, perf_start, perf_stop

### Community 63 - "retro_microphone_interface"
Cohesion: 0.14
Nodes (8): retro_microphone_interface, close_mic, get_mic_state, get_params, interface_version, open_mic, read_mic, set_mic_state

### Community 64 - "retro_netpacket_callback"
Cohesion: 0.14
Nodes (8): retro_netpacket_callback, connected, disconnected, poll, protocol_version, receive, start, stop

### Community 67 - "retro_hw_context_type"
Cohesion: 0.15
Nodes (13): retro_hw_context_type, RETRO_HW_CONTEXT_D3D10, RETRO_HW_CONTEXT_D3D11, RETRO_HW_CONTEXT_D3D12, RETRO_HW_CONTEXT_D3D9, RETRO_HW_CONTEXT_DUMMY, RETRO_HW_CONTEXT_NONE, RETRO_HW_CONTEXT_OPENGL (+5 more)

### Community 68 - "retro_game_info_ext"
Cohesion: 0.17
Nodes (12): retro_game_info_ext, archive_file, archive_path, data, dir, ext, file_in_archive, full_path (+4 more)

### Community 69 - "retro_location_callback"
Cohesion: 0.17
Nodes (7): retro_location_callback, deinitialized, get_position, initialized, set_interval, start, stop

### Community 70 - "retro_midi_interface"
Cohesion: 0.18
Nodes (6): retro_midi_interface, flush, input_enabled, output_enabled, read, write

### Community 71 - "retro_mod"
Cohesion: 0.20
Nodes (10): retro_mod, RETROKMOD_ALT, RETROKMOD_CAPSLOCK, RETROKMOD_CTRL, RETROKMOD_DUMMY, RETROKMOD_META, RETROKMOD_NONE, RETROKMOD_NUMLOCK (+2 more)

### Community 74 - "retro_core_option_v2_definition"
Cohesion: 0.22
Nodes (9): retro_core_option_v2_definition, category_key, default_value, desc, desc_categorized, info, info_categorized, key (+1 more)

### Community 75 - "retro_memory_descriptor"
Cohesion: 0.22
Nodes (9): retro_memory_descriptor, addrspace, disconnect, flags, len, offset, ptr, select (+1 more)

### Community 76 - "retro_framebuffer"
Cohesion: 0.25
Nodes (8): retro_framebuffer, access_flags, data, format, height, memory_flags, pitch, width

### Community 77 - "retro_hw_render_interface_type"
Cohesion: 0.25
Nodes (8): retro_hw_render_interface_type, RETRO_HW_RENDER_INTERFACE_D3D10, RETRO_HW_RENDER_INTERFACE_D3D11, RETRO_HW_RENDER_INTERFACE_D3D12, RETRO_HW_RENDER_INTERFACE_D3D9, RETRO_HW_RENDER_INTERFACE_DUMMY, RETRO_HW_RENDER_INTERFACE_GSKIT_PS2, RETRO_HW_RENDER_INTERFACE_VULKAN

### Community 78 - "retro_message_ext"
Cohesion: 0.25
Nodes (8): retro_message_ext, duration, level, msg, priority, progress, target, type

### Community 79 - "retro_sensor_action"
Cohesion: 0.25
Nodes (8): retro_sensor_action, RETRO_SENSOR_ACCELEROMETER_DISABLE, RETRO_SENSOR_ACCELEROMETER_ENABLE, RETRO_SENSOR_DUMMY, RETRO_SENSOR_GYROSCOPE_DISABLE, RETRO_SENSOR_GYROSCOPE_ENABLE, RETRO_SENSOR_ILLUMINANCE_DISABLE, RETRO_SENSOR_ILLUMINANCE_ENABLE

### Community 80 - "retro_subsystem_rom_info"
Cohesion: 0.25
Nodes (8): retro_subsystem_rom_info, block_extract, desc, memory, need_fullpath, num_memory, required, valid_extensions

### Community 81 - "retro_perf_counter"
Cohesion: 0.29
Nodes (7): retro_perf_counter, call_cnt, ident, registered, start, total, retro_perf_tick_t

### Community 82 - "retro_av_enable_flags"
Cohesion: 0.33
Nodes (6): retro_av_enable_flags, RETRO_AV_ENABLE_AUDIO, RETRO_AV_ENABLE_DUMMY, RETRO_AV_ENABLE_FAST_SAVESTATES, RETRO_AV_ENABLE_HARD_DISABLE_AUDIO, RETRO_AV_ENABLE_VIDEO

### Community 83 - "retro_core_option_definition"
Cohesion: 0.33
Nodes (6): retro_core_option_definition, default_value, desc, info, key, values

### Community 84 - "retro_game_geometry"
Cohesion: 0.33
Nodes (6): retro_game_geometry, aspect_ratio, base_height, base_width, max_height, max_width

### Community 85 - "retro_input_descriptor"
Cohesion: 0.33
Nodes (6): retro_input_descriptor, description, device, id, index, port

### Community 86 - "retro_log_level"
Cohesion: 0.33
Nodes (6): retro_log_level, RETRO_LOG_DEBUG, RETRO_LOG_DUMMY, RETRO_LOG_ERROR, RETRO_LOG_INFO, RETRO_LOG_WARN

### Community 87 - "retro_power_state"
Cohesion: 0.33
Nodes (6): retro_power_state, RETRO_POWERSTATE_CHARGED, RETRO_POWERSTATE_CHARGING, RETRO_POWERSTATE_DISCHARGING, RETRO_POWERSTATE_PLUGGED_IN, RETRO_POWERSTATE_UNKNOWN

### Community 88 - "retro_savestate_context"
Cohesion: 0.33
Nodes (6): retro_savestate_context, RETRO_SAVESTATE_CONTEXT_NORMAL, RETRO_SAVESTATE_CONTEXT_ROLLBACK_NETPLAY, RETRO_SAVESTATE_CONTEXT_RUNAHEAD_SAME_BINARY, RETRO_SAVESTATE_CONTEXT_RUNAHEAD_SAME_INSTANCE, RETRO_SAVESTATE_CONTEXT_UNKNOWN

### Community 89 - "retro_subsystem_info"
Cohesion: 0.33
Nodes (6): retro_subsystem_info, desc, id, ident, num_roms, roms

### Community 90 - "retro_system_info"
Cohesion: 0.33
Nodes (6): retro_system_info, block_extract, library_name, library_version, need_fullpath, valid_extensions

### Community 91 - "retro_audio_callback"
Cohesion: 0.40
Nodes (3): retro_audio_callback, callback, set_state

### Community 92 - "retro_fastforwarding_override"
Cohesion: 0.40
Nodes (5): retro_fastforwarding_override, fastforward, inhibit_toggle, notification, ratio

### Community 93 - "retro_game_info"
Cohesion: 0.40
Nodes (5): retro_game_info, data, meta, path, size

### Community 94 - "retro_message_type"
Cohesion: 0.40
Nodes (5): retro_message_type, RETRO_MESSAGE_TYPE_NOTIFICATION, RETRO_MESSAGE_TYPE_NOTIFICATION_ALT, RETRO_MESSAGE_TYPE_PROGRESS, RETRO_MESSAGE_TYPE_STATUS

### Community 95 - "retro_pixel_format"
Cohesion: 0.40
Nodes (5): retro_pixel_format, RETRO_PIXEL_FORMAT_0RGB1555, RETRO_PIXEL_FORMAT_RGB565, RETRO_PIXEL_FORMAT_UNKNOWN, RETRO_PIXEL_FORMAT_XRGB8888

### Community 96 - "retro_sensor_interface"
Cohesion: 0.40
Nodes (3): retro_sensor_interface, get_sensor_input, set_sensor_state

### Community 98 - "v_opendir"
Cohesion: 0.50
Nodes (4): v_closedir(), v_opendir(), closedir, opendir

### Community 99 - "retro_camera_buffer"
Cohesion: 0.50
Nodes (4): retro_camera_buffer, RETRO_CAMERA_BUFFER_DUMMY, RETRO_CAMERA_BUFFER_OPENGL_TEXTURE, RETRO_CAMERA_BUFFER_RAW_FRAMEBUFFER

### Community 101 - "retro_device_power"
Cohesion: 0.50
Nodes (4): retro_device_power, percent, seconds, state

### Community 102 - "retro_message_target"
Cohesion: 0.50
Nodes (4): retro_message_target, RETRO_MESSAGE_TARGET_ALL, RETRO_MESSAGE_TARGET_LOG, RETRO_MESSAGE_TARGET_OSD

### Community 103 - "retro_rumble_effect"
Cohesion: 0.50
Nodes (4): retro_rumble_effect, RETRO_RUMBLE_DUMMY, RETRO_RUMBLE_STRONG, RETRO_RUMBLE_WEAK

### Community 104 - "retro_system_content_info_override"
Cohesion: 0.50
Nodes (4): retro_system_content_info_override, extensions, need_fullpath, persistent_data

### Community 107 - "retro_controller_info"
Cohesion: 0.67
Nodes (3): retro_controller_info, num_types, types

### Community 109 - "retro_core_option_value"
Cohesion: 0.67
Nodes (3): retro_core_option_value, label, value

### Community 110 - "retro_core_options_intl"
Cohesion: 0.67
Nodes (3): retro_core_options_intl, local, us

### Community 112 - "retro_core_options_v2"
Cohesion: 0.67
Nodes (3): retro_core_options_v2, categories, definitions

### Community 113 - "retro_controller_description"
Cohesion: 0.67
Nodes (3): retro_controller_description, desc, id

### Community 115 - "retro_hw_render_context_negotiation_interface_type"
Cohesion: 0.67
Nodes (3): retro_hw_render_context_negotiation_interface_type, RETRO_HW_RENDER_CONTEXT_NEGOTIATION_INTERFACE_DUMMY, RETRO_HW_RENDER_CONTEXT_NEGOTIATION_INTERFACE_VULKAN

### Community 116 - "retro_hw_render_interface"
Cohesion: 0.67
Nodes (3): retro_hw_render_interface, interface_type, interface_version

### Community 118 - "retro_memory_map"
Cohesion: 0.67
Nodes (3): retro_memory_map, descriptors, num_descriptors

### Community 119 - "retro_message"
Cohesion: 0.67
Nodes (3): retro_message, frames, msg

### Community 121 - "retro_subsystem_memory_info"
Cohesion: 0.67
Nodes (3): retro_subsystem_memory_info, extension, type

### Community 122 - "retro_system_av_info"
Cohesion: 0.67
Nodes (3): retro_system_av_info, geometry, timing

### Community 123 - "retro_system_timing"
Cohesion: 0.67
Nodes (3): retro_system_timing, fps, sample_rate

### Community 124 - "retro_variable"
Cohesion: 0.67
Nodes (3): retro_variable, key, value

### Community 125 - "retro_vfs_interface_info"
Cohesion: 0.67
Nodes (3): retro_vfs_interface_info, iface, required_interface_version

### Community 127 - "retro_hw_render_context_negotiation_interface"
Cohesion: 0.67
Nodes (3): retro_hw_render_context_negotiation_interface, interface_type, interface_version

### Community 128 - "retro_throttle_state"
Cohesion: 0.67
Nodes (3): retro_throttle_state, mode, rate

## Knowledge Gaps
- **540 isolated node(s):** `PSP`, `PS1`, `PS2`, `GB`, `GBC` (+535 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 867 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **45 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `retro_key` connect `retro_key` to `libretro.h`?**
  _High betweenness centrality (0.184) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `tr()` (e.g. with `.nextLighter()` and `.resolutionChoices()`) actually correct?**
  _`tr()` has 2 INFERRED edges - model-reasoned connections that need verification._
- **What connects `PSP`, `PS1`, `PS2` to the rest of the system?**
  _540 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `LayoutEditor.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.061057692307692306 - nodes in this community are weakly interconnected._
- **Why does `tr()` connect `LibraryScreen.kt` to `LayoutEditor.kt`, `GameActivity.kt`, `AppSettingsScreen.kt`, `GameEntity`, `PlayerOverlays.kt`, `PerformanceSettings`, `GameActivity`, `BillingManager.kt`, `GameEntity`, `LayoutEditor.kt`, `AppLanguage`, `Tutorial.kt`, `HomeScreen.kt`, `GameFileResolver.kt`, `SystemSettingsScreen.kt`?**
  _High betweenness centrality (0.168) - this node is a cross-community bridge._
- **Should `GameActivity.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06666666666666667 - nodes in this community are weakly interconnected._
- **Why does `SettingsRepository` connect `SettingsRepository` to `LayoutEditor.kt`, `AppSettingsScreen.kt`, `GameEntity`, `BillingManager.kt`, `AdManager.kt`, `LayoutEditor.kt`, `Tutorial.kt`, `AppLanguage`, `AllEmulatorApp.kt`, `ThemeMode`, `Theme.kt`, `SystemSettingsScreen.kt`?**
  _High betweenness centrality (0.090) - this node is a cross-community bridge._