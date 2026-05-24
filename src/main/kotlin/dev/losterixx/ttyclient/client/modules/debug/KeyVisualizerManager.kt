package dev.losterixx.ttyclient.client.modules.debug

import dev.losterixx.ttyclient.client.modules.notifications.NotificationManager
import dev.losterixx.ttyclient.client.modules.notifications.NotificationType
import org.lwjgl.glfw.GLFW

object KeyVisualizerManager {

    var keysEnabled: Boolean = false

    fun onKeyPress(keyCode: Int, scanCode: Int, mods: Int) {
        if (!keysEnabled) return

        NotificationManager.show(
            title = "Debug: Keyboard Key",
            message = "key: ${glfwToKeybindName(keyCode, scanCode)}, id: $keyCode  scan: $scanCode",
            type = NotificationType.INFO,
            durationMs = 1500L,
            silent = true
        )
    }

    fun onMouseButton(button: Int, mods: Int) {
        if (!keysEnabled) return

        val name = when (button) {
            0 -> "LMB"
            1 -> "RMB"
            2 -> "MMB"
            else -> "MOUSE$button"
        }

        NotificationManager.show(
            title = "Debug: Mouse Key",
            message = "key: $name, btn: $button",
            type = NotificationType.SUCCESS,
            durationMs = 1500L,
            silent = true
        )
    }

    fun onScroll(deltaX: Double, deltaY: Double) {
        if (!keysEnabled) return
        if (deltaX == 0.0 && deltaY == 0.0) return

        val direction = when {
            deltaY > 0 -> "SCROLL_UP"
            deltaY < 0 -> "SCROLL_DOWN"
            deltaX > 0 -> "SCROLL_RIGHT"
            else -> "SCROLL_LEFT"
        }

        val delta = if (deltaY != 0.0) deltaY else deltaX
        NotificationManager.show(
            title = direction,
            message = "Δ ${"%.1f".format(delta)}",
            type = NotificationType.WARNING,
            durationMs = 800L,
            silent = true
        )
    }

    private fun glfwToKeybindName(keyCode: Int, scanCode: Int): String {
        val printable = GLFW.glfwGetKeyName(keyCode, scanCode)
        if (!printable.isNullOrEmpty()) return printable.uppercase()

        return when (keyCode) {
            GLFW.GLFW_KEY_ESCAPE          -> "ESCAPE"
            GLFW.GLFW_KEY_ENTER           -> "ENTER"
            GLFW.GLFW_KEY_TAB             -> "TAB"
            GLFW.GLFW_KEY_BACKSPACE       -> "BACKSPACE"
            GLFW.GLFW_KEY_INSERT          -> "INSERT"
            GLFW.GLFW_KEY_DELETE          -> "DELETE"
            GLFW.GLFW_KEY_RIGHT           -> "RIGHT"
            GLFW.GLFW_KEY_LEFT            -> "LEFT"
            GLFW.GLFW_KEY_DOWN            -> "DOWN"
            GLFW.GLFW_KEY_UP              -> "UP"
            GLFW.GLFW_KEY_PAGE_UP         -> "PAGEUP"
            GLFW.GLFW_KEY_PAGE_DOWN       -> "PAGEDOWN"
            GLFW.GLFW_KEY_HOME            -> "HOME"
            GLFW.GLFW_KEY_END             -> "END"
            GLFW.GLFW_KEY_CAPS_LOCK       -> "CAPS"
            GLFW.GLFW_KEY_SCROLL_LOCK     -> "SCROLLLOCK"
            GLFW.GLFW_KEY_NUM_LOCK        -> "NUMLOCK"
            GLFW.GLFW_KEY_PRINT_SCREEN    -> "PRINT"
            GLFW.GLFW_KEY_PAUSE           -> "PAUSE"
            GLFW.GLFW_KEY_SPACE           -> "SPACE"
            GLFW.GLFW_KEY_MENU            -> "MENU"
            GLFW.GLFW_KEY_F1              -> "F1"
            GLFW.GLFW_KEY_F2              -> "F2"
            GLFW.GLFW_KEY_F3              -> "F3"
            GLFW.GLFW_KEY_F4              -> "F4"
            GLFW.GLFW_KEY_F5              -> "F5"
            GLFW.GLFW_KEY_F6              -> "F6"
            GLFW.GLFW_KEY_F7              -> "F7"
            GLFW.GLFW_KEY_F8              -> "F8"
            GLFW.GLFW_KEY_F9              -> "F9"
            GLFW.GLFW_KEY_F10             -> "F10"
            GLFW.GLFW_KEY_F11             -> "F11"
            GLFW.GLFW_KEY_F12             -> "F12"
            GLFW.GLFW_KEY_LEFT_SHIFT,
            GLFW.GLFW_KEY_RIGHT_SHIFT     -> "SHIFT"
            GLFW.GLFW_KEY_LEFT_CONTROL,
            GLFW.GLFW_KEY_RIGHT_CONTROL   -> "CTRL"
            GLFW.GLFW_KEY_LEFT_ALT,
            GLFW.GLFW_KEY_RIGHT_ALT       -> "ALT"
            GLFW.GLFW_KEY_LEFT_SUPER,
            GLFW.GLFW_KEY_RIGHT_SUPER     -> "SUPER"
            GLFW.GLFW_KEY_KP_0            -> "NUM0"
            GLFW.GLFW_KEY_KP_1            -> "NUM1"
            GLFW.GLFW_KEY_KP_2            -> "NUM2"
            GLFW.GLFW_KEY_KP_3            -> "NUM3"
            GLFW.GLFW_KEY_KP_4            -> "NUM4"
            GLFW.GLFW_KEY_KP_5            -> "NUM5"
            GLFW.GLFW_KEY_KP_6            -> "NUM6"
            GLFW.GLFW_KEY_KP_7            -> "NUM7"
            GLFW.GLFW_KEY_KP_8            -> "NUM8"
            GLFW.GLFW_KEY_KP_9            -> "NUM9"
            GLFW.GLFW_KEY_KP_DECIMAL      -> "NUM."
            GLFW.GLFW_KEY_KP_DIVIDE       -> "NUM/"
            GLFW.GLFW_KEY_KP_MULTIPLY     -> "NUM*"
            GLFW.GLFW_KEY_KP_SUBTRACT     -> "NUM-"
            GLFW.GLFW_KEY_KP_ADD          -> "NUM+"
            GLFW.GLFW_KEY_KP_ENTER        -> "NUMENTER"
            GLFW.GLFW_KEY_KP_EQUAL        -> "NUM="
            else                          -> "KEY_$keyCode"
        }
    }
}
