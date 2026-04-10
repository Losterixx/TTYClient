package dev.losterixx.ttyclient.client.modules.screenshots

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.modules.ScreenshotConfig
import dev.losterixx.ttyclient.client.modules.ClientModule
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.math.roundToInt

object ScreenshotManager : ClientModule {

    override val id = "screenshots"
    override val configPath = "config/modules/screenshots.jsonc"

    private val logger = LoggerFactory.getLogger("TTYClient/Screenshots")

    var config: ScreenshotConfig = ScreenshotConfig()
        private set

    override fun load() {
        config = ConfigManager.loadConfig(
            configPath,
            ScreenshotConfig::class.java
        ) { ScreenshotConfig() }
    }

    fun calculateTargetSize(nativeWidth: Int, nativeHeight: Int): IntArray? {
        val (targetW, targetH) = when (config.quality.lowercase().trim()) {
            "720p" -> 1280 to 720
            "1080p" -> 1920 to 1080
            "2k" -> 2560 to 1440
            "4k" -> 3840 to 2160
            "8k" -> 7680 to 4320

            else -> return null
        }

        val scaleX = targetW.toFloat() / nativeWidth
        val scaleY = targetH.toFloat() / nativeHeight
        val scale = minOf(scaleX, scaleY)

        val finalW = (nativeWidth * scale).roundToInt().coerceAtLeast(1)
        val finalH = (nativeHeight * scale).roundToInt().coerceAtLeast(1)

        if (finalW == nativeWidth && finalH == nativeHeight) return null

        return intArrayOf(finalW, finalH)
    }

    fun sendScreenshotMessage(file: File) {
        val mc = Minecraft.getInstance()
        logger.info("Screenshot saved: ${file.name} (${file.length()} bytes)")

        val prefix = ConfigManager.general.commandPrefix

        val message = Component.literal("${MainClient.PREFIX}Screenshot saved as ")
            .append(
                Component.literal(file.name)
                    .withStyle { it.withUnderlined(true) }
            )
            .append(Component.literal("  "))
            .append(
                Component.literal("[Copy]").withStyle { style ->
                    style.withColor(0x55FF55)
                        .withBold(true)
                        .withHoverEvent(HoverEvent.ShowText(Component.literal("Copy image to clipboard")))
                        .withClickEvent(ClickEvent.RunCommand("${prefix}screenshot copy ${file.absolutePath}"))
                }
            )
            .append(Component.literal("  "))
            .append(
                Component.literal("[Open]").withStyle { style ->
                    style.withColor(0x5599FF)
                        .withBold(true)
                        .withHoverEvent(HoverEvent.ShowText(Component.literal("Open screenshots folder")))
                        .withClickEvent(ClickEvent.OpenFile(file.parentFile))
                }
            )
            .append(Component.literal("  "))
            .append(
                Component.literal("[Upload]").withStyle { style ->
                    style.withColor(0xFFAA00)
                        .withBold(true)
                        .withHoverEvent(HoverEvent.ShowText(Component.literal("Upload for a shareable link (not yet available)")))
                        .withClickEvent(ClickEvent.RunCommand("${prefix}screenshot upload ${file.absolutePath}"))
                }
            )

        mc.execute {
            mc.gui.chat.addClientSystemMessage(message)
        }
    }

    fun copyImageToClipboard(imagePath: String) {
        Thread {
            try {
                val os = System.getProperty("os.name").lowercase()
                val mc = Minecraft.getInstance()

                val success = when {
                    os.contains("nix") || os.contains("nux") -> copyLinux(imagePath, mc)
                    os.contains("win") -> copyWindows(imagePath, mc)
                    os.contains("mac") -> copyMac(imagePath, mc)
                    else -> {
                        mc.execute {
                            mc.gui.chat.addClientSystemMessage(Component.literal("${MainClient.PREFIX}§cUnsupported OS for clipboard copy."))
                        }

                        false
                    }
                }

                if (success) {
                    mc.execute {
                        mc.gui.chat.addClientSystemMessage(Component.literal("${MainClient.PREFIX}Screenshot copied to clipboard."))
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to copy screenshot to clipboard: ${e.message}")

                val mc = Minecraft.getInstance()
                mc.execute {
                    mc.gui.chat.addClientSystemMessage(Component.literal("${MainClient.PREFIX}§cFailed to copy: ${e.message}"))
                }
            }
        }.also {
            it.isDaemon = true
            it.start()
        }
    }

    private fun copyLinux(path: String, mc: Minecraft): Boolean {
        val isWayland = System.getenv("WAYLAND_DISPLAY") != null
        return if (isWayland) copyWayland(path, mc) else copyX11(path, mc)
    }

    private fun copyWayland(path: String, mc: Minecraft): Boolean {
        if (Runtime.getRuntime().exec(arrayOf("which", "wl-copy")).waitFor() != 0) {
            mc.execute {
                mc.gui.chat.addClientSystemMessage(Component.literal("${MainClient.PREFIX}§cwl-copy not found. Install wl-clipboard (e.g. sudo pacman -S wl-clipboard)."))
            }

            return false
        }

        val process = ProcessBuilder("wl-copy", "--type", "image/png").start()
        File(path).inputStream().use { it.copyTo(process.outputStream) }
        process.outputStream.close()

        return process.waitFor() == 0
    }

    private fun copyX11(path: String, mc: Minecraft): Boolean {
        if (Runtime.getRuntime().exec(arrayOf("which", "xclip")).waitFor() == 0) {
            return Runtime.getRuntime()
                .exec(arrayOf("xclip", "-selection", "clipboard", "-t", "image/png", "-i", path))
                .waitFor() == 0
        }

        mc.execute {
            mc.gui.chat.addClientSystemMessage(Component.literal("${MainClient.PREFIX}§cxclip not found. Install xclip (e.g. sudo pacman -S xclip)."))
        }

        return false
    }

    // this should work, but is untested
    private fun copyWindows(path: String, @Suppress("UNUSED_PARAMETER") mc: Minecraft): Boolean {
        val process = Runtime.getRuntime().exec(arrayOf("powershell", "-command", "Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.Clipboard]::SetImage([System.Drawing.Image]::FromFile('$path'))"))
        return process.waitFor() == 0
    }

    // same as copyWindows
    private fun copyMac(path: String, @Suppress("UNUSED_PARAMETER") mc: Minecraft): Boolean {
        val process = Runtime.getRuntime().exec(
            arrayOf("osascript", "-e", "set the clipboard to (read (POSIX file \"$path\") as JPEG picture)")
        )

        return process.waitFor() == 0
    }

    fun uploadImage(file: File) {
        val mc = Minecraft.getInstance()
        mc.execute {
            mc.gui.chat.addClientSystemMessage(Component.literal("${MainClient.PREFIX}§7Upload is not yet implemented. A custom upload server will be added in a future update."))
        }
    }
}