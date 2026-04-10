package dev.losterixx.ttyclient.client.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.Strictness
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.config.configs.GeneralConfig
import dev.losterixx.ttyclient.client.config.configs.KeybindsConfig
import dev.losterixx.ttyclient.client.config.configs.ThemeConfig
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.TimeUnit

object ConfigManager {

    private object HexColorAdapter : TypeAdapter<Int>() {
        override fun write(out: JsonWriter, value: Int) {
            val a = (value ushr 24) and 0xFF
            val rgb = value and 0x00FFFFFF

            out.value(if (a == 0xFF) "#%06X".format(rgb) else "#%02X%06X".format(a, rgb))
        }

        override fun read(input: JsonReader): Int {
            val s = input.nextString().trim()

            return when {
                s.startsWith("#") -> when (s.length - 1) {
                    6 -> (0xFF000000.toInt()) or s.substring(1).toInt(16)
                    8 -> s.substring(1).toLong(16).toInt()
                    else -> 0xFF000000.toInt()
                }

                s.startsWith("0x", ignoreCase = true) -> s.toLong(16).toInt()

                else -> s.toIntOrNull() ?: 0xFF000000.toInt()
            }
        }
    }

    private val gson: Gson = GsonBuilder()
        .setStrictness(Strictness.LENIENT)
        .create()

    private val themeGson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .setStrictness(Strictness.LENIENT)
        .registerTypeAdapter(Int::class.java, HexColorAdapter)
        .registerTypeAdapter(Int::class.javaObjectType, HexColorAdapter)
        .create()

    private val jsonWriter = CommentedJsonWriter(gson)
    private val themeJsonWriter = CommentedJsonWriter(themeGson)

    private val logger = MainClient.LOGGER

    val baseDir: File = File(System.getProperty("user.home"), "TTYClient")

    @Volatile var general: GeneralConfig = GeneralConfig()
        private set
    @Volatile var theme: ThemeConfig = ThemeConfig()
        private set
    @Volatile var keybinds: KeybindsConfig = KeybindsConfig()
        private set

    private val listeners = mutableMapOf<String, MutableList<() -> Unit>>()
    private var watcherThread: Thread? = null

    private val watchedDirs = mutableSetOf<File>()

    private data class MergeResult(
        val merged: JsonObject,
        val outdated: Set<String>,
        val changed: Boolean
    )

    fun onReload(relativePath: String, listener: () -> Unit) {
        listeners.getOrPut(relativePath) { mutableListOf() }.add(listener)
    }

    fun load() {
        general = loadFile("config/general.jsonc", GeneralConfig::class.java, gson) { GeneralConfig() }
        theme = loadFile("config/theme.jsonc", ThemeConfig::class.java, themeGson) { ThemeConfig() }
        logger.info("Configs loaded.")
    }

    fun save() {
        saveFile("config/general.jsonc", general, jsonWriter)
        saveFile("config/theme.jsonc", theme, themeJsonWriter)
        logger.info("Configs saved.")
    }

    fun reset() {
        general = GeneralConfig()
        theme = ThemeConfig()
        save()
    }

    fun <T : Any> loadConfig(
        relativePath: String,
        type: Class<T>,
        useThemeGson: Boolean = false,
        default: () -> T
    ): T {
        val g = if (useThemeGson) themeGson else gson
        return loadFile(relativePath, type, g, default)
    }

    fun saveConfig(relativePath: String, data: Any, useThemeGson: Boolean = false) {
        val writer = if (useThemeGson) themeJsonWriter else jsonWriter
        saveFile(relativePath, data, writer)
    }

    fun startWatcher() {
        val defaultConfigDir = File(baseDir, "config")
        if (watchedDirs.isEmpty()) {
            defaultConfigDir.mkdirs()
            watchedDirs.add(defaultConfigDir)
        }

        val watchService = FileSystems.getDefault().newWatchService()
        watchedDirs.forEach { dir ->
            dir.mkdirs()
            dir.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
        }

        watcherThread = Thread {
            try {
                while (!Thread.currentThread().isInterrupted) {
                    val key = watchService.poll(500, TimeUnit.MILLISECONDS) ?: continue
                    Thread.sleep(100L)
                    val watchedDir = (key.watchable() as? Path)?.toFile()
                    val changedFiles = key.pollEvents().map { it.context().toString() }.toSet()
                    key.reset()

                    changedFiles.forEach { filename ->
                        val file = watchedDir?.let { File(it, filename) } ?: return@forEach
                        val relativePath = file.relativeTo(baseDir).path

                        when (relativePath) {
                            "config/general.jsonc" -> {
                                general = loadFile("config/general.jsonc", GeneralConfig::class.java, gson) { GeneralConfig() }
                                logger.info("Live-reloaded: $relativePath")
                            }

                            "config/theme.jsonc" -> {
                                theme = loadFile("config/theme.jsonc", ThemeConfig::class.java, themeGson) { ThemeConfig() }
                                logger.info("Live-reloaded: $relativePath")
                            }
                        }

                        listeners[relativePath]?.forEach { it() }
                    }
                }
            } catch (_: InterruptedException) {
            } finally {
                watchService.close()
            }
        }.also {
            it.isDaemon = true
            it.name = "TTYClient-ConfigWatcher"
            it.start()
        }
    }

    fun stopWatcher() {
        watcherThread?.interrupt()
        watcherThread = null
    }

    private fun <T : Any> loadFile(
        relativePath: String,
        type: Class<T>,
        g: Gson,
        default: () -> T
    ): T {
        val file = File(baseDir, relativePath)
        watchedDirs.add(file.parentFile)

        if (!file.exists()) {
            file.parentFile?.mkdirs()

            val filename = file.name
            val resourcePath = "/assets/ttyclient/config/$filename"
            val stream = ConfigManager::class.java.getResourceAsStream(resourcePath)

            if (stream != null) {
                stream.use { file.writeBytes(it.readBytes()) }
                logger.info("Extracted default template: $filename")
            } else {
                val instance = default()
                val writer = if (g === themeGson) themeJsonWriter else jsonWriter
                saveFile(relativePath, instance, writer)
                return instance
            }
        }

        return try {
            val fileText = file.readText()
            val existingTree = gson.fromJson(fileText, JsonObject::class.java)
                ?: return default()

            val defaultInstance = default()
            val defaultTree = g.toJsonTree(defaultInstance) as? JsonObject
                ?: return g.fromJson(fileText, type) ?: defaultInstance

            val prevOutdated = extractPrevOutdatedLeafKeys(fileText)
            val result = mergeJsonObjects(existingTree, defaultTree, "", prevOutdated)

            val markedCount = fileText.lines().count { it.trimEnd().endsWith("// OUTDATED") }

            val needsSave = result.changed
                    || (result.outdated.isNotEmpty() && !outdatedAlreadyMarked(fileText, result.outdated))
                    || (markedCount != result.outdated.size)

            if (needsSave) {
                val writer = if (g === themeGson) themeJsonWriter else jsonWriter
                saveMergedFile(relativePath, type, result.merged, result.outdated, writer)
                logger.info("Config auto-migrated: $relativePath")
            }

            g.fromJson(result.merged, type) ?: defaultInstance
        } catch (e: Exception) {
            logger.error("Failed to parse $relativePath: ${e.message}")
            logger.error("  -> Keeping file on disk unchanged. Defaults are used in-memory until the file is fixed.")
            default()
        }
    }

    private fun mergeJsonObjects(
        existing: JsonObject,
        default: JsonObject,
        path: String = "",
        prevOutdatedLeafKeys: Set<String> = emptySet()
    ): MergeResult {
        val merged = JsonObject()
        val outdated = mutableSetOf<String>()
        var changed = false

        val hasComingBack = existing.keySet().any { it in prevOutdatedLeafKeys && default.has(it) }

        if (hasComingBack) {
            changed = true

            default.entrySet().forEach { (key, defaultValue) ->
                val fullPath = if (path.isEmpty()) key else "$path.$key"
                val existingValue = existing.get(key)

                when {
                    existingValue == null -> {
                        merged.add(key, defaultValue)
                        logger.info("  + Config [$path]: added missing key \"$key\"")
                    }
                    existingValue is JsonObject && defaultValue is JsonObject -> {
                        val child = mergeJsonObjects(existingValue, defaultValue, fullPath, prevOutdatedLeafKeys)
                        merged.add(key, child.merged); outdated.addAll(child.outdated); if (child.changed) changed = true
                    }
                    else -> merged.add(key, existingValue)
                }
            }

            existing.entrySet().forEach { (key, value) ->
                if (!default.has(key)) {
                    val fullPath = if (path.isEmpty()) key else "$path.$key"
                    merged.add(key, value); outdated.add(fullPath)
                    logger.info("  ~ Config [$path]: outdated key \"$key\"")
                }
            }
        } else {
            existing.entrySet().forEach { (key, existingValue) ->
                val fullPath = if (path.isEmpty()) key else "$path.$key"
                val defaultValue = default.get(key)

                when {
                    defaultValue == null -> {
                        merged.add(key, existingValue); outdated.add(fullPath)
                        logger.info("  ~ Config [$path]: outdated key \"$key\"")
                    }
                    existingValue is JsonObject && defaultValue is JsonObject -> {
                        val child = mergeJsonObjects(existingValue, defaultValue, fullPath, prevOutdatedLeafKeys)
                        merged.add(key, child.merged); outdated.addAll(child.outdated); if (child.changed) changed = true
                    }
                    else -> merged.add(key, existingValue)
                }
            }

            default.entrySet().forEach { (key, defaultValue) ->
                if (!existing.has(key)) {
                    val fullPath = if (path.isEmpty()) key else "$path.$key"
                    merged.add(key, defaultValue); changed = true
                    logger.info("  + Config [$path]: added missing key \"$key\"")
                }
            }
        }

        return MergeResult(merged, outdated, changed)
    }

    private fun extractPrevOutdatedLeafKeys(fileText: String): Set<String> =
        fileText.lines()
            .filter { it.trimEnd().endsWith("// OUTDATED") }
            .mapNotNull { line -> Regex("""^\s*"([^"]+)"\s*:""").find(line)?.groupValues?.get(1) }
            .toSet()

    private fun outdatedAlreadyMarked(fileText: String, outdatedPaths: Set<String>): Boolean =
        outdatedPaths.all { path ->
            val leafKey = "\"${path.substringAfterLast('.')}\""
            fileText.lines().any { line -> line.contains(leafKey) && line.trimEnd().endsWith("// OUTDATED") }
        }

    private fun saveMergedFile(
        relativePath: String,
        type: Class<*>,
        tree: JsonObject,
        outdatedKeys: Set<String>,
        writer: CommentedJsonWriter
    ) {
        try {
            val file = File(baseDir, relativePath)
            file.parentFile?.mkdirs()
            file.writeText(writer.writeTree(type, tree, outdatedKeys))
        } catch (e: Exception) {
            logger.error("Failed to save migrated $relativePath: ${e.message}")
        }
    }

    private fun saveFile(relativePath: String, data: Any, writer: CommentedJsonWriter) {
        try {
            val file = File(baseDir, relativePath)
            file.parentFile?.mkdirs()
            file.writeText(writer.write(data))
        } catch (e: Exception) {
            logger.error("Failed to save $relativePath: ${e.message}")
        }
    }
}