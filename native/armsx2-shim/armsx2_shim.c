/*
 * armsx2_shim.c
 * -----------------------------------------------------------------------------
 * "ตัวคั่นกลาง" ระหว่าง LibretroDroid (ตัวเล่นเกมของแอพ) กับ core PS2 (ARMSX2)
 *
 * ปัญหาที่แก้:
 *   core ARMSX2 ขอระบบไฟล์เสมือน (VFS) เวอร์ชัน 3 ก่อน แล้วค่อยลด 2 → 1 ถ้าไม่ได้
 *   แต่ LibretroDroid 0.14.0 ตอบ "ได้" ทุกเวอร์ชัน ทั้งที่มีแค่ฟังก์ชันของเวอร์ชัน 2
 *   → ฟังก์ชันของเวอร์ชัน 3 (stat, mkdir, opendir ...) เป็น null
 *   → core เรียก stat แล้วแอพเด้ง (SIGSEGV ที่ address 0) ตั้งแต่ตอนเปิดเกม
 *
 * วิธีแก้:
 *   LibretroDroid โหลดไฟล์นี้แทน core จริง → ไฟล์นี้โหลด core จริง (libarmsx2_libretro_android.so)
 *   แล้วส่งต่อทุกคำสั่งตรงๆ ยกเว้นตอน core ขอ VFS: ตัวนี้ให้ VFS เวอร์ชัน 3 "ของจริง" แทน
 *     - เปิด/อ่าน/เขียนไฟล์ → ใช้ของ LibretroDroid (ไฟล์เกมเสมือนจาก content:// อยู่ที่นั่น)
 *     - stat / mkdir / อ่านโฟลเดอร์ (ที่ LibretroDroid ไม่มี) → ทำเองที่นี่
 *       stat: ถามระบบก่อน (ไฟล์จริง เช่น BIOS) ถ้าไม่เจอ ลองเปิดผ่าน LibretroDroid (ไฟล์เกมเสมือน)
 *   (ถ้าให้แค่เวอร์ชัน 2 core จะถามระบบเองว่ามีไฟล์เกมไหม → ไม่เจอ เพราะเป็นไฟล์เสมือน → เปิดเกมไม่ได้)
 *
 * ไม่ได้แก้ core หรือ LibretroDroid เลย — ถ้าวันหน้า LibretroDroid แก้แล้ว ลบไฟล์นี้ทิ้งได้
 * build: scripts/fetch-cores.sh (คอมไพล์ด้วย clang ของ NDK)
 * -----------------------------------------------------------------------------
 */
#include <dirent.h>
#include <dlfcn.h>
#include <errno.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <android/log.h>
#include <stdio.h>
#include "libretro.h"

#define TAG "ARMSX2Shim"
#define REAL_CORE "libarmsx2_libretro_android.so"
/* LibretroDroid 0.14.0: VFS::SUPPORTED_VERSION = 2 */
#define FRONTEND_VFS_VERSION 2u

static void *core;
static retro_environment_t real_env;

/* ---------------- โหลด core จริง (ครั้งเดียว) ---------------- */
static void *sym(const char *name) {
    if (!core) {
        core = dlopen(REAL_CORE, RTLD_NOW | RTLD_LOCAL);
        if (!core) {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "dlopen %s failed: %s", REAL_CORE, dlerror());
            return NULL;
        }
    }
    void *f = dlsym(core, name);
    if (!f) __android_log_print(ANDROID_LOG_ERROR, TAG, "missing %s", name);
    return f;
}

/* ส่งต่อฟังก์ชันไปที่ core จริง (ถ้าโหลดไม่ได้ ใช้ค่า fallback แทน — ไม่ให้แอพเด้ง) */
#define FWD_VOID(name, params, args)                          \
    RETRO_API void name params {                              \
        static void (*f) params;                              \
        if (!f) f = (void (*) params) sym(#name);             \
        if (f) f args;                                        \
    }
#define FWD_RET(type, fallback, name, params, args)           \
    RETRO_API type name params {                              \
        static type (*f) params;                              \
        if (!f) f = (type (*) params) sym(#name);             \
        return f ? f args : (fallback);                       \
    }

/* ---------------- VFS เวอร์ชัน 3 (ห่อของ LibretroDroid) ---------------- */
static const struct retro_vfs_interface *lrd; /* ของ LibretroDroid (มีถึงเวอร์ชัน 2) */
static struct retro_vfs_interface vfs3;

/* มีไฟล์/โฟลเดอร์นี้ไหม: ระบบก่อน → ไฟล์เสมือนของ LibretroDroid */
static int RETRO_CALLCONV v_stat(const char *path, int32_t *size) {
    struct stat st;
    if (path && stat(path, &st) == 0) {
        if (size) *size = st.st_size > INT32_MAX ? INT32_MAX : (int32_t) st.st_size;
        return RETRO_VFS_STAT_IS_VALID | (S_ISDIR(st.st_mode) ? RETRO_VFS_STAT_IS_DIRECTORY : 0);
    }
    struct retro_vfs_file_handle *h = path ? lrd->open(path, RETRO_VFS_FILE_ACCESS_READ, RETRO_VFS_FILE_ACCESS_HINT_NONE) : NULL;
    if (!h) return 0;
    int64_t sz = lrd->size(h);
    lrd->close(h);
    if (size) *size = sz > INT32_MAX ? INT32_MAX : (int32_t) (sz < 0 ? 0 : sz);
    return RETRO_VFS_STAT_IS_VALID;
}

static int RETRO_CALLCONV v_mkdir(const char *dir) {
    if (mkdir(dir, 0775) == 0) return 0;
    return errno == EEXIST ? -2 : -1;
}

/* อ่านรายชื่อในโฟลเดอร์ (โฟลเดอร์จริงเท่านั้น — ไฟล์เสมือนไม่มีโฟลเดอร์) */
struct shim_dir { DIR *d; struct dirent *e; bool hidden; char path[]; };

static struct retro_vfs_dir_handle *RETRO_CALLCONV v_opendir(const char *dir, bool include_hidden) {
    DIR *d = opendir(dir);
    if (!d) return NULL;
    struct shim_dir *h = calloc(1, sizeof(struct shim_dir) + strlen(dir) + 1);
    if (!h) { closedir(d); return NULL; }
    h->d = d;
    h->hidden = include_hidden;
    strcpy(h->path, dir);
    return (struct retro_vfs_dir_handle *) h;
}

static bool RETRO_CALLCONV v_readdir(struct retro_vfs_dir_handle *dh) {
    struct shim_dir *h = (struct shim_dir *) dh;
    while ((h->e = readdir(h->d)) != NULL) {
        const char *n = h->e->d_name;
        if (!strcmp(n, ".") || !strcmp(n, "..")) continue;
        if (!h->hidden && n[0] == '.') continue;
        return true;
    }
    return false;
}

static const char *RETRO_CALLCONV v_dirent_get_name(struct retro_vfs_dir_handle *dh) {
    struct shim_dir *h = (struct shim_dir *) dh;
    return h->e ? h->e->d_name : NULL;
}

static bool RETRO_CALLCONV v_dirent_is_dir(struct retro_vfs_dir_handle *dh) {
    struct shim_dir *h = (struct shim_dir *) dh;
    if (!h->e) return false;
    if (h->e->d_type != DT_UNKNOWN) return h->e->d_type == DT_DIR;
    char full[4096];
    struct stat st;
    snprintf(full, sizeof full, "%s/%s", h->path, h->e->d_name);
    return stat(full, &st) == 0 && S_ISDIR(st.st_mode);
}

static int RETRO_CALLCONV v_closedir(struct retro_vfs_dir_handle *dh) {
    struct shim_dir *h = (struct shim_dir *) dh;
    int r = closedir(h->d);
    free(h);
    return r;
}

/* ---------------- ตัวกรองคำสั่ง environment ---------------- */
static bool shim_env(unsigned cmd, void *data) {
    if (cmd == RETRO_ENVIRONMENT_GET_VFS_INTERFACE && data) {
        struct retro_vfs_interface_info *info = (struct retro_vfs_interface_info *) data;
        const uint32_t wanted = info->required_interface_version;
        if (wanted > 3) return false; /* เกินที่เราทำได้ → ตอบ "ไม่ได้" ตามกติกา libretro */
        /* ขอของ LibretroDroid มาก่อน (ได้ฟังก์ชันเปิด/อ่าน/เขียนไฟล์ ซึ่งรู้จักไฟล์เกมเสมือน) */
        struct retro_vfs_interface_info base = {FRONTEND_VFS_VERSION, NULL};
        if (!real_env || !real_env(cmd, &base) || !base.iface) return false;
        lrd = base.iface;
        if (wanted <= FRONTEND_VFS_VERSION) {
            *info = base;
            return true;
        }
        /* เวอร์ชัน 3 = ของ LibretroDroid (ถึงเวอร์ชัน 2) + stat/mkdir/โฟลเดอร์ที่ทำเอง */
        vfs3.get_path = lrd->get_path;
        vfs3.open = lrd->open;
        vfs3.close = lrd->close;
        vfs3.size = lrd->size;
        vfs3.tell = lrd->tell;
        vfs3.seek = lrd->seek;
        vfs3.read = lrd->read;
        vfs3.write = lrd->write;
        vfs3.flush = lrd->flush;
        vfs3.remove = lrd->remove;
        vfs3.rename = lrd->rename;
        vfs3.truncate = lrd->truncate;
        vfs3.stat = v_stat;
        vfs3.mkdir = v_mkdir;
        vfs3.opendir = v_opendir;
        vfs3.readdir = v_readdir;
        vfs3.dirent_get_name = v_dirent_get_name;
        vfs3.dirent_is_dir = v_dirent_is_dir;
        vfs3.closedir = v_closedir;
        info->required_interface_version = 3;
        info->iface = &vfs3;
        return true;
    }
    return real_env ? real_env(cmd, data) : false;
}

RETRO_API void retro_set_environment(retro_environment_t cb) {
    static void (*f)(retro_environment_t);
    real_env = cb;
    if (!f) f = (void (*)(retro_environment_t)) sym("retro_set_environment");
    if (f) f(shim_env);
}

/* ---------------- ส่วนที่เหลือ: ส่งต่อตรงๆ ---------------- */
FWD_VOID(retro_set_video_refresh, (retro_video_refresh_t cb), (cb))
FWD_VOID(retro_set_audio_sample, (retro_audio_sample_t cb), (cb))
FWD_VOID(retro_set_audio_sample_batch, (retro_audio_sample_batch_t cb), (cb))
FWD_VOID(retro_set_input_poll, (retro_input_poll_t cb), (cb))
FWD_VOID(retro_set_input_state, (retro_input_state_t cb), (cb))
FWD_VOID(retro_init, (void), ())
FWD_VOID(retro_deinit, (void), ())
FWD_RET(unsigned, RETRO_API_VERSION, retro_api_version, (void), ())
FWD_VOID(retro_get_system_info, (struct retro_system_info *info), (info))
FWD_VOID(retro_get_system_av_info, (struct retro_system_av_info *info), (info))
FWD_VOID(retro_set_controller_port_device, (unsigned port, unsigned device), (port, device))
FWD_VOID(retro_reset, (void), ())
FWD_VOID(retro_run, (void), ())
FWD_RET(size_t, 0, retro_serialize_size, (void), ())
FWD_RET(bool, false, retro_serialize, (void *data, size_t size), (data, size))
FWD_RET(bool, false, retro_unserialize, (const void *data, size_t size), (data, size))
FWD_VOID(retro_cheat_reset, (void), ())
FWD_VOID(retro_cheat_set, (unsigned index, bool enabled, const char *code), (index, enabled, code))
FWD_RET(bool, false, retro_load_game, (const struct retro_game_info *game), (game))
FWD_RET(bool, false, retro_load_game_special, (unsigned type, const struct retro_game_info *info, size_t num), (type, info, num))
FWD_VOID(retro_unload_game, (void), ())
FWD_RET(unsigned, RETRO_REGION_NTSC, retro_get_region, (void), ())
FWD_RET(void *, NULL, retro_get_memory_data, (unsigned id), (id))
FWD_RET(size_t, 0, retro_get_memory_size, (unsigned id), (id))
