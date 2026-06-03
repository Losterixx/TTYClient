package dev.losterixx.ttyclient.client.manager

import dev.losterixx.ttyclient.client.MainClient
import dev.losterixx.ttyclient.client.commandsys.CommandManager
import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.ConfiguredKeybind
import dev.losterixx.ttyclient.client.config.configs.KeybindsConfig
import dev.losterixx.ttyclient.client.modules.freelook.FreelookManager
import dev.losterixx.ttyclient.client.modules.zoom.ZoomManager
import dev.losterixx.ttyclient.client.utils.NoopScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

object KeybindManager {

    private val logger = MainClient.LOGGER

    private val registeredCombos = mutableListOf<HotkeyManager.KeyCombo>()

    fun load() {
        registeredCombos.forEach { HotkeyManager.unregisterHotkey(it) }
        registeredCombos.clear()

        val config = ConfigManager.loadConfig(
            "config/keybinds.jsonc",
            KeybindsConfig::class.java
        ) { KeybindsConfig(binds = defaultBinds()) }

        for (bind in config.binds) {
            val combo = parseCombo(bind.bind)
            if (combo == null) {
                logger.warn("KeybindManager: Could not parse keybind \"${bind.bind}\" - skipping.")
                continue
            }

            HotkeyManager.registerHotkey(combo, { execute(bind) }, releaseHandlerFor(bind))
            registeredCombos.add(combo)
            logger.info("KeybindManager: [${bind.bind}] → ${bind.type}: ${bind.exec}")
        }

        logger.info("KeybindManager: ${registeredCombos.size} keybind(s) active.")
    }

    private fun defaultBinds(): List<ConfiguredKeybind> = listOf(
        ConfiguredKeybind(
            bind = "ALT+C",
            type = "client",
            exec = "config"
        ),
        ConfiguredKeybind(
            bind = "ALT+H",
            type = "client",
            exec = "hud"
        ),
        ConfiguredKeybind(
            bind = "X",
            type = "client",
            exec = "module --trigger freelook"
        ),
        ConfiguredKeybind(
            bind = "C",
            type = "client",
            exec = "module --trigger zoom"
        ),
        ConfiguredKeybind(
            bind = "B",
            type = "client",
            exec = "module --trigger fullbright"
        ),
        ConfiguredKeybind(
            bind = "I",
            type = "client",
            exec = "module --trigger customchat"
        ),
        ConfiguredKeybind(
            bind = "DELETE",
            type = "action",
            exec = "Disconnect"
        ),
        ConfiguredKeybind(
            bind = "PAUSE",
            type = "action",
            exec = "ShowMultiplayer"
        )
    )

    private fun releaseHandlerFor(bind: ConfiguredKeybind): (() -> Unit)? {
        if (bind.type.lowercase() != "client") return null

        val ctx = CommandManager.parse(bind.exec.trim())
        if (ctx.name != "module") return null
        if (!ctx.hasFlag("trigger", 't')) return null

        val moduleName = ctx.args.getOrNull(0)?.lowercase() ?: return null
        return when (moduleName) {
            "freelook", "fl" -> {
                {
                    if (FreelookManager.config.hold && FreelookManager.isActive()) {
                        FreelookManager.trigger()
                    }
                }
            }

            "zoom", "z" -> {
                {
                    if (ZoomManager.config.hold) {
                        ZoomManager.stopZoom()
                    }
                }
            }

            else -> null
        }
    }

    private fun execute(bind: ConfiguredKeybind) {
        when (bind.type.lowercase().trim()) {
            "client" -> {
                val ctx = CommandManager.parse(bind.exec.trim())
                val cmd = CommandManager.getCommand(ctx.name)

                if (cmd != null) {
                    try {
                        cmd.execute(ctx)
                    } catch (e: Exception) {
                        logger.error("KeybindManager: Error in client command \"${bind.exec}\": ${e.message}")
                    }
                } else {
                    logger.warn("KeybindManager: Unknown client command \"${ctx.name}\" (bind: \"${bind.bind}\")")
                }
            }

            "command" -> {
                Minecraft.getInstance().player?.connection?.sendCommand(bind.exec.trimStart('/'))
            }

            "chat" -> {
                Minecraft.getInstance().player?.connection?.sendChat(bind.exec)
            }

            "bash" -> {
                try {
                    Runtime.getRuntime().exec(arrayOf("/bin/bash", "-c", bind.exec))
                    logger.info("KeybindManager: Executed bash: ${bind.exec}")
                } catch (e: Exception) {
                    logger.error("KeybindManager: Bash error for \"${bind.exec}\": ${e.message}")
                }
            }

            "sequence" -> {
                val steps = bind.exec.split(";").map { it.trim() }.filter { it.isNotEmpty() }
                for (step in steps) {
                    val colonIdx = step.indexOf(':')
                    if (colonIdx < 0) {
                        logger.warn("KeybindManager: Sequence step \"$step\" is missing ':' - skipping.")
                        continue
                    }

                    val stepType = step.substring(0, colonIdx).trim().lowercase()
                    val stepExec = step.substring(colonIdx + 1).trim()
                    if (stepType == "sequence") {
                        logger.warn("KeybindManager: Nested sequences are not supported - skipping step.")
                        continue
                    }

                    execute(ConfiguredKeybind(bind = bind.bind, type = stepType, exec = stepExec))
                }
            }

            "noop" -> { }

            "action" -> {
                val mc = Minecraft.getInstance()
                when (bind.exec.trim().lowercase()) {
                    "quitgame" -> mc.stop()

                    "disconnect" -> {
                        if (mc.level != null) {
                            if (mc.isLocalServer) {
                                mc.disconnectFromWorld(Component.translatable("menu.savingLevel"))
                                mc.setScreen(TitleScreen())
                            } else {
                                mc.disconnectFromWorld(Component.translatable("menu.disconnect"))
                                mc.setScreen(JoinMultiplayerScreen(TitleScreen()))
                            }
                        }
                    }

                    "showmultiplayer" -> {
                        if (mc.screen !is JoinMultiplayerScreen) {
                            mc.setScreen(JoinMultiplayerScreen(NoopScreen()))
                        }
                    }

                    else -> logger.warn("KeybindManager: Unknown action \"${bind.exec}\" (bind: \"${bind.bind}\")")
                }
            }

            else -> logger.warn("KeybindManager: Unknown type \"${bind.type}\" (bind: \"${bind.bind}\")")
        }
    }

    fun parseCombo(keyBind: String): HotkeyManager.KeyCombo? {
        val parts = keyBind.uppercase().split("+").map { it.trim() }.filter { it.isNotEmpty() }

        var shift = false
        var ctrl = false
        var alt = false
        var keyCode = 0
        var mouseButton = -1

        for (part in parts) {
            when (part) {
                "SHIFT" -> shift = true
                "CTRL", "CONTROL" -> ctrl = true
                "ALT" -> alt = true
                "LMB", "MOUSE0" -> mouseButton = 0
                "RMB", "MOUSE1" -> mouseButton = 1
                "MMB", "MOUSE2" -> mouseButton = 2

                else -> {
                    if (part.startsWith("MOUSE") && part.length > 5) {
                        val n = part.substring(5).toIntOrNull()
                        if (n != null && n >= 0) { mouseButton = n; continue }
                    }

                    val code = nameToGlfw(part)

                    if (code == 0) {
                        logger.warn("KeybindManager: Unknown key \"$part\" in combo \"$keyBind\"")
                        return null
                    }

                    keyCode = code
                }
            }
        }

        if (keyCode == 0 && mouseButton < 0) return null

        return HotkeyManager.KeyCombo(keyCode, mouseButton, shift, ctrl, alt)
    }

    private fun nameToGlfw(name: String): Int = when (name) {
        "ESCAPE", "ESC" -> GLFW.GLFW_KEY_ESCAPE
        "ENTER", "RETURN" -> GLFW.GLFW_KEY_ENTER
        "TAB" -> GLFW.GLFW_KEY_TAB
        "BACKSPACE", "BACK" -> GLFW.GLFW_KEY_BACKSPACE
        "INSERT" -> GLFW.GLFW_KEY_INSERT
        "DELETE", "DEL" -> GLFW.GLFW_KEY_DELETE
        "RIGHT" -> GLFW.GLFW_KEY_RIGHT
        "LEFT" -> GLFW.GLFW_KEY_LEFT
        "DOWN" -> GLFW.GLFW_KEY_DOWN
        "UP" -> GLFW.GLFW_KEY_UP
        "PAGEUP", "PAGE_UP" -> GLFW.GLFW_KEY_PAGE_UP
        "PAGEDOWN", "PAGE_DOWN" -> GLFW.GLFW_KEY_PAGE_DOWN
        "HOME" -> GLFW.GLFW_KEY_HOME
        "END" -> GLFW.GLFW_KEY_END
        "CAPS", "CAPSLOCK", "CAPS_LOCK" -> GLFW.GLFW_KEY_CAPS_LOCK
        "SCROLLLOCK", "SCROLL_LOCK" -> GLFW.GLFW_KEY_SCROLL_LOCK
        "NUMLOCK", "NUM_LOCK" -> GLFW.GLFW_KEY_NUM_LOCK
        "PRINT", "PRINTSCREEN", "PRINT_SCREEN" -> GLFW.GLFW_KEY_PRINT_SCREEN
        "PAUSE" -> GLFW.GLFW_KEY_PAUSE
        "SPACE" -> GLFW.GLFW_KEY_SPACE
        "MENU" -> GLFW.GLFW_KEY_MENU

        "F1" -> GLFW.GLFW_KEY_F1
        "F2" -> GLFW.GLFW_KEY_F2
        "F3" -> GLFW.GLFW_KEY_F3
        "F4" -> GLFW.GLFW_KEY_F4
        "F5" -> GLFW.GLFW_KEY_F5
        "F6" -> GLFW.GLFW_KEY_F6
        "F7" -> GLFW.GLFW_KEY_F7
        "F8" -> GLFW.GLFW_KEY_F8
        "F9" -> GLFW.GLFW_KEY_F9
        "F10" -> GLFW.GLFW_KEY_F10
        "F11" -> GLFW.GLFW_KEY_F11
        "F12" -> GLFW.GLFW_KEY_F12

        "A" -> GLFW.GLFW_KEY_A
        "B" -> GLFW.GLFW_KEY_B
        "C" -> GLFW.GLFW_KEY_C
        "D" -> GLFW.GLFW_KEY_D
        "E" -> GLFW.GLFW_KEY_E
        "F" -> GLFW.GLFW_KEY_F
        "G" -> GLFW.GLFW_KEY_G
        "H" -> GLFW.GLFW_KEY_H
        "I" -> GLFW.GLFW_KEY_I
        "J" -> GLFW.GLFW_KEY_J
        "K" -> GLFW.GLFW_KEY_K
        "L" -> GLFW.GLFW_KEY_L
        "M" -> GLFW.GLFW_KEY_M
        "N" -> GLFW.GLFW_KEY_N
        "O" -> GLFW.GLFW_KEY_O
        "P" -> GLFW.GLFW_KEY_P
        "Q" -> GLFW.GLFW_KEY_Q
        "R" -> GLFW.GLFW_KEY_R
        "S" -> GLFW.GLFW_KEY_S
        "T" -> GLFW.GLFW_KEY_T
        "U" -> GLFW.GLFW_KEY_U
        "V" -> GLFW.GLFW_KEY_V
        "W" -> GLFW.GLFW_KEY_W
        "X" -> GLFW.GLFW_KEY_X
        "Y" -> GLFW.GLFW_KEY_Y
        "Z" -> GLFW.GLFW_KEY_Z

        "0" -> GLFW.GLFW_KEY_0
        "1" -> GLFW.GLFW_KEY_1
        "2" -> GLFW.GLFW_KEY_2
        "3" -> GLFW.GLFW_KEY_3
        "4" -> GLFW.GLFW_KEY_4
        "5" -> GLFW.GLFW_KEY_5
        "6" -> GLFW.GLFW_KEY_6
        "7" -> GLFW.GLFW_KEY_7
        "8" -> GLFW.GLFW_KEY_8
        "9" -> GLFW.GLFW_KEY_9

        "'", "APOSTROPHE" -> GLFW.GLFW_KEY_APOSTROPHE
        ",", "COMMA" -> GLFW.GLFW_KEY_COMMA
        "-", "MINUS" -> GLFW.GLFW_KEY_MINUS
        ".", "PERIOD" -> GLFW.GLFW_KEY_PERIOD
        "/", "SLASH" -> GLFW.GLFW_KEY_SLASH
        ";", "SEMICOLON" -> GLFW.GLFW_KEY_SEMICOLON
        "=", "EQUAL" -> GLFW.GLFW_KEY_EQUAL
        "[", "LEFT_BRACKET" -> GLFW.GLFW_KEY_LEFT_BRACKET
        "\\", "BACKSLASH" -> GLFW.GLFW_KEY_BACKSLASH
        "]", "RIGHT_BRACKET" -> GLFW.GLFW_KEY_RIGHT_BRACKET
        "`", "GRAVE", "GRAVE_ACCENT" -> GLFW.GLFW_KEY_GRAVE_ACCENT

        "NUM0", "KP0" -> GLFW.GLFW_KEY_KP_0
        "NUM1", "KP1" -> GLFW.GLFW_KEY_KP_1
        "NUM2", "KP2" -> GLFW.GLFW_KEY_KP_2
        "NUM3", "KP3" -> GLFW.GLFW_KEY_KP_3
        "NUM4", "KP4" -> GLFW.GLFW_KEY_KP_4
        "NUM5", "KP5" -> GLFW.GLFW_KEY_KP_5
        "NUM6", "KP6" -> GLFW.GLFW_KEY_KP_6
        "NUM7", "KP7" -> GLFW.GLFW_KEY_KP_7
        "NUM8", "KP8" -> GLFW.GLFW_KEY_KP_8
        "NUM9", "KP9" -> GLFW.GLFW_KEY_KP_9
        "NUM.", "NUMDECIMAL" -> GLFW.GLFW_KEY_KP_DECIMAL
        "NUM/", "NUMDIVIDE" -> GLFW.GLFW_KEY_KP_DIVIDE
        "NUM*", "NUMMULTIPLY" -> GLFW.GLFW_KEY_KP_MULTIPLY
        "NUM-", "NUMSUBTRACT" -> GLFW.GLFW_KEY_KP_SUBTRACT
        "NUM+", "NUMADD" -> GLFW.GLFW_KEY_KP_ADD
        "NUMENTER" -> GLFW.GLFW_KEY_KP_ENTER
        "NUM=", "NUMEQUAL" -> GLFW.GLFW_KEY_KP_EQUAL

        else -> 0
    }
}

