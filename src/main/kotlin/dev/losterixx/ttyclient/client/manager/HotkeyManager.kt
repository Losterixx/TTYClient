package dev.losterixx.ttyclient.client.manager

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import kotlin.text.isNotEmpty
import kotlin.text.uppercase

object HotkeyManager {

    private val client get() = Minecraft.getInstance()
    private val registeredHotkeys = mutableListOf<RegisteredHotkey>()

    data class RegisteredHotkey(
        val combo: KeyCombo,
        val onPress: () -> Unit,
        val onRelease: (() -> Unit)? = null,
        var wasPressed: Boolean = false
    )

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register {
            val window = client.window.handle()

            if (client.screen == null) {
                registeredHotkeys.forEach { hotkey ->
                    val isPressed = hotkey.combo.isPressed(window)
                    if (isPressed && !hotkey.wasPressed) hotkey.onPress()
                    if (!isPressed && hotkey.wasPressed) hotkey.onRelease?.invoke()
                    hotkey.wasPressed = isPressed
                }
            } else {
                registeredHotkeys.forEach { it.wasPressed = false }
            }
        }
    }

    fun registerHotkey(combo: KeyCombo, onPress: () -> Unit, onRelease: (() -> Unit)? = null) {
        registeredHotkeys.add(RegisteredHotkey(combo, onPress, onRelease))
    }

    fun unregisterHotkey(combo: KeyCombo) {
        registeredHotkeys.removeIf { it.combo.matches(combo) }
    }

    fun getAllRegisteredHotkeys(): List<RegisteredHotkey> = registeredHotkeys.toList()
    fun getAllRegisteredCombos(): List<KeyCombo> = registeredHotkeys.map { it.combo }

    data class KeyCombo(
        var key: Int = 0,
        var mouseButton: Int = -1,
        var shift: Boolean = false,
        var ctrl: Boolean = false,
        var alt: Boolean = false
    ) {
        fun isPressed(window: Long): Boolean {
            if (shift && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) != GLFW.GLFW_PRESS && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) != GLFW.GLFW_PRESS) return false
            if (ctrl && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) != GLFW.GLFW_PRESS && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) != GLFW.GLFW_PRESS) return false
            if (alt && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) != GLFW.GLFW_PRESS && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) != GLFW.GLFW_PRESS) return false

            if (mouseButton >= 0) return GLFW.glfwGetMouseButton(window, mouseButton) == GLFW.GLFW_PRESS
            if (key > 0) return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS
            return false
        }

        fun isEmpty(): Boolean = key == 0 && mouseButton < 0

        fun matches(other: KeyCombo): Boolean {
            return key == other.key && mouseButton == other.mouseButton &&
                    shift == other.shift && ctrl == other.ctrl && alt == other.alt
        }

        fun getKeyName(): String {
            return getKeyName(key)
        }

        override fun toString(): String {
            if (isEmpty()) return "None"

            val parts = mutableListOf<String>()

            if (ctrl) parts.add("Ctrl")
            if (shift) parts.add("Shift")
            if (alt) parts.add("Alt")

            if (mouseButton >= 0) {
                parts.add(when (mouseButton) {
                    0 -> "LMB"
                    1 -> "RMB"
                    2 -> "MMB"
                    else -> "M${mouseButton + 1}"
                })
            } else if (key > 0) {
                val name = GLFW.glfwGetKeyName(key, 0)
                parts.add(if (name != null && name.isNotEmpty()) name.uppercase() else getKeyName(key))
            }

            return parts.joinToString("+")
        }

        companion object {
            fun getKeyName(code: Int): String = when (code) {
                GLFW.GLFW_KEY_ESCAPE -> "Esc"
                GLFW.GLFW_KEY_ENTER -> "Enter"
                GLFW.GLFW_KEY_TAB -> "Tab"
                GLFW.GLFW_KEY_BACKSPACE -> "Back"
                GLFW.GLFW_KEY_INSERT -> "Insert"
                GLFW.GLFW_KEY_DELETE -> "Delete"
                GLFW.GLFW_KEY_RIGHT -> "→"
                GLFW.GLFW_KEY_LEFT -> "←"
                GLFW.GLFW_KEY_DOWN -> "↓"
                GLFW.GLFW_KEY_UP -> "↑"
                GLFW.GLFW_KEY_PAGE_UP -> "PageUp"
                GLFW.GLFW_KEY_PAGE_DOWN -> "PageDown"
                GLFW.GLFW_KEY_HOME -> "Home"
                GLFW.GLFW_KEY_END -> "End"
                GLFW.GLFW_KEY_CAPS_LOCK -> "Caps"
                GLFW.GLFW_KEY_SCROLL_LOCK -> "Scroll"
                GLFW.GLFW_KEY_NUM_LOCK -> "Num"
                GLFW.GLFW_KEY_PRINT_SCREEN -> "Print"
                GLFW.GLFW_KEY_PAUSE -> "Pause"
                GLFW.GLFW_KEY_F1 -> "F1"
                GLFW.GLFW_KEY_F2 -> "F2"
                GLFW.GLFW_KEY_F3 -> "F3"
                GLFW.GLFW_KEY_F4 -> "F4"
                GLFW.GLFW_KEY_F5 -> "F5"
                GLFW.GLFW_KEY_F6 -> "F6"
                GLFW.GLFW_KEY_F7 -> "F7"
                GLFW.GLFW_KEY_F8 -> "F8"
                GLFW.GLFW_KEY_F9 -> "F9"
                GLFW.GLFW_KEY_F10 -> "F10"
                GLFW.GLFW_KEY_F11 -> "F11"
                GLFW.GLFW_KEY_F12 -> "F12"
                GLFW.GLFW_KEY_LEFT_SHIFT -> "Shift"
                GLFW.GLFW_KEY_LEFT_CONTROL -> "Ctrl"
                GLFW.GLFW_KEY_LEFT_ALT -> "Alt"
                GLFW.GLFW_KEY_LEFT_SUPER -> "Super"
                GLFW.GLFW_KEY_RIGHT_SHIFT -> "R-Shift"
                GLFW.GLFW_KEY_RIGHT_CONTROL -> "R-Ctrl"
                GLFW.GLFW_KEY_RIGHT_ALT -> "R-Alt"
                GLFW.GLFW_KEY_RIGHT_SUPER -> "R-Super"
                GLFW.GLFW_KEY_SPACE -> "Space"
                GLFW.GLFW_KEY_MENU -> "Menu"

                GLFW.GLFW_KEY_0 -> "0"
                GLFW.GLFW_KEY_1 -> "1"
                GLFW.GLFW_KEY_2 -> "2"
                GLFW.GLFW_KEY_3 -> "3"
                GLFW.GLFW_KEY_4 -> "4"
                GLFW.GLFW_KEY_5 -> "5"
                GLFW.GLFW_KEY_6 -> "6"
                GLFW.GLFW_KEY_7 -> "7"
                GLFW.GLFW_KEY_8 -> "8"
                GLFW.GLFW_KEY_9 -> "9"

                GLFW.GLFW_KEY_A -> "A"
                GLFW.GLFW_KEY_B -> "B"
                GLFW.GLFW_KEY_C -> "C"
                GLFW.GLFW_KEY_D -> "D"
                GLFW.GLFW_KEY_E -> "E"
                GLFW.GLFW_KEY_F -> "F"
                GLFW.GLFW_KEY_G -> "G"
                GLFW.GLFW_KEY_H -> "H"
                GLFW.GLFW_KEY_I -> "I"
                GLFW.GLFW_KEY_J -> "J"
                GLFW.GLFW_KEY_K -> "K"
                GLFW.GLFW_KEY_L -> "L"
                GLFW.GLFW_KEY_M -> "M"
                GLFW.GLFW_KEY_N -> "N"
                GLFW.GLFW_KEY_O -> "O"
                GLFW.GLFW_KEY_P -> "P"
                GLFW.GLFW_KEY_Q -> "Q"
                GLFW.GLFW_KEY_R -> "R"
                GLFW.GLFW_KEY_S -> "S"
                GLFW.GLFW_KEY_T -> "T"
                GLFW.GLFW_KEY_U -> "U"
                GLFW.GLFW_KEY_V -> "V"
                GLFW.GLFW_KEY_W -> "W"
                GLFW.GLFW_KEY_X -> "X"
                GLFW.GLFW_KEY_Y -> "Y"
                GLFW.GLFW_KEY_Z -> "Z"

                GLFW.GLFW_KEY_APOSTROPHE -> "'"
                GLFW.GLFW_KEY_COMMA -> ","
                GLFW.GLFW_KEY_MINUS -> "-"
                GLFW.GLFW_KEY_PERIOD -> "."
                GLFW.GLFW_KEY_SLASH -> "/"
                GLFW.GLFW_KEY_SEMICOLON -> ";"
                GLFW.GLFW_KEY_EQUAL -> "="
                GLFW.GLFW_KEY_LEFT_BRACKET -> "["
                GLFW.GLFW_KEY_BACKSLASH -> "\\"
                GLFW.GLFW_KEY_RIGHT_BRACKET -> "]"
                GLFW.GLFW_KEY_GRAVE_ACCENT -> "`"
                GLFW.GLFW_KEY_WORLD_1 -> "W1"
                GLFW.GLFW_KEY_WORLD_2 -> "W2"

                GLFW.GLFW_KEY_KP_0 -> "Num0"
                GLFW.GLFW_KEY_KP_1 -> "Num1"
                GLFW.GLFW_KEY_KP_2 -> "Num2"
                GLFW.GLFW_KEY_KP_3 -> "Num3"
                GLFW.GLFW_KEY_KP_4 -> "Num4"
                GLFW.GLFW_KEY_KP_5 -> "Num5"
                GLFW.GLFW_KEY_KP_6 -> "Num6"
                GLFW.GLFW_KEY_KP_7 -> "Num7"
                GLFW.GLFW_KEY_KP_8 -> "Num8"
                GLFW.GLFW_KEY_KP_9 -> "Num9"
                GLFW.GLFW_KEY_KP_DECIMAL -> "Num."
                GLFW.GLFW_KEY_KP_DIVIDE -> "Num/"
                GLFW.GLFW_KEY_KP_MULTIPLY -> "Num*"
                GLFW.GLFW_KEY_KP_SUBTRACT -> "Num-"
                GLFW.GLFW_KEY_KP_ADD -> "Num+"
                GLFW.GLFW_KEY_KP_ENTER -> "NumEnter"
                GLFW.GLFW_KEY_KP_EQUAL -> "Num="

                else -> "Key $code"
            }
        }
    }
}
