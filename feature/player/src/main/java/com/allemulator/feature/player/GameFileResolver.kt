/*
 * GameFileResolver.kt
 * -----------------------------------------------------------------------------
 * ปัญหา: เกมของผู้ใช้อยู่ในรูป content:// URI (จาก SAF) แต่ emulator core
 *        เขียนด้วย C/C++ ที่ต้องการ "ไฟล์" เพื่อเปิดอ่าน
 *
 * วิธีส่งเกมให้ core มี 3 แบบ (LoadStrategy):
 *   VFS    — ส่ง file descriptor เป็น "ไฟล์เสมือน" (libretro VFS) ไม่ต้องก๊อป
 *             ใช้กับเกมแผ่น (PS1/PSP) เกมหลายไฟล์ (.cue+.bin) ส่งไฟล์ข้างเคียงไปด้วย
 *   COPY    — ก๊อปไปไว้ใน cache ก่อน — ใช้กับตลับเกมเล็ก (GB/GBC/GBA/NES)
 *             ไฟล์ .zip ของทุกเครื่อง → แตกไฟล์ลง cache อัตโนมัติ
 *             และเป็นทางสำรองเมื่อแบบอื่นล้มเหลว
 *             เก็บสำเนาล่าสุดไว้ 1 เกม เปิดเกมเดิมซ้ำจะไม่ก๊อปใหม่
 *
 * ถ้า core โหลดเกมไม่ได้ GameActivity จะเปิดใหม่อัตโนมัติด้วยแบบ COPY (ดู forceCopy)
 * -----------------------------------------------------------------------------
 */
package com.allemulator.feature.player

import com.allemulator.core.emulation.tr
import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import com.allemulator.core.data.db.GameEntity
import com.allemulator.core.emulation.GameSystem
import com.swordfish.libretrodroid.VirtualFile
import java.io.Closeable
import java.io.File
import java.util.zip.ZipInputStream

/** ผลลัพธ์: วิธีที่จะส่งเกมให้ core */
sealed interface ResolvedGame : Closeable {
    /** วิธีที่ใช้ (เอาไว้ตัดสินใจว่าควร retry แบบ COPY ไหม) */
    val strategy: LoadStrategy

    class Virtual(val files: List<VirtualFile>) : ResolvedGame {
        override val strategy = LoadStrategy.VFS
        override fun close() = Unit // LibretroDroid เป็นเจ้าของ fd หลังโหลดเกมแล้ว
    }

    /** path ที่ core เปิดได้ตรงๆ — [keepAlive] ต้องเปิดค้างไว้จนจบเกม (ใช้กับ FD_PATH) */
    class Path(
        val path: String,
        override val strategy: LoadStrategy,
        private val keepAlive: ParcelFileDescriptor? = null,
    ) : ResolvedGame {
        override fun close() { keepAlive?.close() }
    }
}

enum class LoadStrategy { VFS, FD_PATH, COPY }

class GameFileResolver(private val context: Context) {

    private val resolver get() = context.contentResolver

    /**
     * @param forceCopy true = ข้ามวิธีอื่น ก๊อปไฟล์ลง cache เลย (ใช้ตอน retry)
     */
    fun resolve(game: GameEntity, system: GameSystem, forceCopy: Boolean = false): ResolvedGame {
        val uri = Uri.parse(game.uri)
        val ext = game.fileName.substringAfterLast('.').lowercase()
        val files = listOf(game.fileName to uri) + siblingsFor(game, uri, ext)

        // .zip ทุกเครื่อง → แตกไฟล์ก่อน (core ส่วนใหญ่เปิด zip เองไม่ได้)
        if (ext == "zip") return extractZipToCache(game, uri, system)
        // ไฟล์ที่แตกจาก zip ไว้ในแอพแล้ว (file://) → เปิดด้วย path ตรงๆ ได้เลย
        if (uri.scheme == "file") return ResolvedGame.Path(requireNotNull(uri.path), LoadStrategy.COPY)
        if (forceCopy) return copyToCache(game, files)

        return when (system) {
            // ตลับเกมเล็ก (ไม่กี่ MB) → ก๊อปลง cache (ใช้เวลาไม่ถึงวินาที และ core ทุกตัวเปิดได้)
            // ทดสอบแล้ว: fceumm โหลดจาก RAM (BYTES) ไม่ได้ จึงใช้ไฟล์จริงแทน
            GameSystem.GB, GameSystem.GBC, GameSystem.GBA, GameSystem.NES -> copyToCache(game, files)


            // เกมแผ่น → ลองไฟล์เสมือนก่อน (ไม่เปลืองพื้นที่)
            GameSystem.PS1, GameSystem.PSP -> ResolvedGame.Virtual(
                files.mapNotNull { (name, u) -> resolver.openFileDescriptor(u, "r")?.let { VirtualFile(name, it) } },
            )
        }
    }

    // -------------------------------------------------------------------------
    // .zip: แตกไฟล์เกมข้างในออกมาไว้ใน cache
    // -------------------------------------------------------------------------
    private fun extractZipToCache(game: GameEntity, uri: Uri, system: GameSystem): ResolvedGame {
        val dir = cacheDirFor(game)
        val done = File(dir, ".extracted")
        if (!done.exists()) {
            // แตกทุกไฟล์ (เกมแผ่นอาจมี .cue + .bin หลายไฟล์ที่ต้องอยู่ด้วยกัน)
            val input = requireNotNull(resolver.openInputStream(uri)) { tr("เปิดไฟล์เกมไม่ได้", "Can't open the game file") }
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (entry.isDirectory) continue
                    // ข้ามไฟล์ขยะที่ Mac ใส่มาใน zip (__MACOSX/ และไฟล์ชื่อขึ้นต้น "._") — ไม่ใช่เกมจริง
                    val base = entry.name.substringAfterLast('/')
                    if (entry.name.startsWith("__MACOSX/") || base.startsWith(".")) continue
                    val target = File(dir, entry.name.substringAfterLast('/'))
                    // กันไฟล์ zip อันตรายที่ตั้งชื่อ "../" เพื่อเขียนนอกโฟลเดอร์ (zip slip)
                    if (!target.canonicalPath.startsWith(dir.canonicalPath)) continue
                    target.outputStream().use { zip.copyTo(it, 1 shl 20) }
                }
            }
            done.createNewFile() // เปิดเกมเดิมซ้ำจะไม่ต้องแตกใหม่
        }
        // เลือกไฟล์หลัก: .cue/.m3u ก่อน (เกมแผ่น) แล้วค่อยไฟล์อื่นที่เครื่องนี้รับ
        val candidates = dir.listFiles().orEmpty().filter { system.accepts(it.name) && !it.name.endsWith(".zip", true) }
        val main = candidates.firstOrNull { it.extension.lowercase() in setOf("cue", "m3u") }
            ?: candidates.maxByOrNull { it.length() }
            ?: error(tr("ไม่พบไฟล์เกม ${system.displayName} ในไฟล์ zip นี้", "No ${system.displayName} game inside this zip"))
        return ResolvedGame.Path(main.absolutePath, LoadStrategy.COPY)
    }

    // -------------------------------------------------------------------------
    // COPY: ก๊อปลง cache/game/<id>/ (เก็บแค่เกมล่าสุด 1 เกม)
    // -------------------------------------------------------------------------
    private fun copyToCache(game: GameEntity, files: List<Pair<String, Uri>>): ResolvedGame {
        val dir = cacheDirFor(game)
        files.forEach { (name, uri) ->
            val target = File(dir, name)
            if (target.exists() && target.length() > 0 && name == game.fileName && target.length() == game.sizeBytes) return@forEach
            resolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { input.copyTo(it, 1 shl 20) }
            }
        }
        return ResolvedGame.Path(File(dir, game.fileName).absolutePath, LoadStrategy.COPY)
    }

    /** โฟลเดอร์ cache ของเกมนี้ — ลบสำเนาของเกมอื่นทิ้ง (เก็บแค่เกมล่าสุด ไม่ให้เปลืองพื้นที่) */
    private fun cacheDirFor(game: GameEntity): File {
        val root = File(context.cacheDir, "game")
        val dir = File(root, game.uri.hashCode().toUInt().toString(16))
        root.listFiles()?.filter { it != dir }?.forEach { it.deleteRecursively() }
        return dir.apply { mkdirs() }
    }

    // -------------------------------------------------------------------------
    // เกมหลายไฟล์: .cue อ้างถึง .bin, .m3u อ้างถึงหลายแผ่น
    // -------------------------------------------------------------------------
    private fun siblingsFor(game: GameEntity, uri: Uri, ext: String): List<Pair<String, Uri>> = when {
        uri.scheme == "file" || game.parentUri.isEmpty() -> emptyList() // ไฟล์ในแอพ/ไฟล์เดี่ยว ไม่ต้องหา
        else -> when (ext) {
        "cue" -> referencedFiles(uri, game.parentUri, cueRegex)
        "m3u" -> referencedFiles(uri, game.parentUri, m3uRegex)
        else -> emptyList()
        }
    }

    private fun referencedFiles(playlist: Uri, parentUri: String, pattern: Regex): List<Pair<String, Uri>> {
        val text = runCatching {
            resolver.openInputStream(playlist)?.bufferedReader()?.use { it.readText() }
        }.getOrNull() ?: return emptyList()
        val wanted = pattern.findAll(text)
            .map { it.groupValues[1].trim().substringAfterLast('/').substringAfterLast('\\') }
            .toSet()
        if (wanted.isEmpty()) return emptyList()

        val siblings = listSiblings(Uri.parse(parentUri))
        return wanted.mapNotNull { name ->
            siblings.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.let { it.key to it.value }
        }
    }

    /** ไฟล์ทั้งหมดในโฟลเดอร์ (ชื่อ → URI) */
    private fun listSiblings(parentDocUri: Uri): Map<String, Uri> {
        val parentId = DocumentsContract.getDocumentId(parentDocUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentDocUri, parentId)
        val result = mutableMapOf<String, Uri>()
        resolver.query(
            childrenUri,
            arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null, null, null,
        )?.use { c ->
            while (c.moveToNext()) {
                val name = c.getString(1) ?: continue
                result[name] = DocumentsContract.buildDocumentUriUsingTree(parentDocUri, c.getString(0))
            }
        }
        return result
    }

    private companion object {

        /** บรรทัดใน .cue เช่น  FILE "Game (Track 1).bin" BINARY */
        val cueRegex = Regex("""(?im)^\s*FILE\s+"?([^"\r\n]+?)"?\s+\w+\s*$""")
        /** บรรทัดใน .m3u = ชื่อไฟล์ 1 บรรทัด (ข้ามบรรทัดที่ขึ้นต้นด้วย #) */
        val m3uRegex = Regex("""(?m)^(?!#)(\S[^\r\n]*)$""")
    }
}
