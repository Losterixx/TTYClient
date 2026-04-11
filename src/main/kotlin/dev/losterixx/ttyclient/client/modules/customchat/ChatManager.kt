package dev.losterixx.ttyclient.client.modules.customchat

import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.modules.CustomChatConfig
import dev.losterixx.ttyclient.client.modules.ClientModule
import dev.losterixx.ttyclient.client.modules.ModuleCategory
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ChatScreen
import org.lwjgl.glfw.GLFW

object ChatManager : ClientModule {

    override val id = "customchat"
    override val configPath = "config/modules/customchat.jsonc"
    override val displayName = "Custom Chat"
    override val description = "Adjustable chat size, infinite history, and persistence across servers."
    override val category = ModuleCategory.CHAT

    private val mc: Minecraft get() = Minecraft.getInstance()

    var isCustomChatVisible: Boolean = true
        private set

    var config: CustomChatConfig = CustomChatConfig()
        private set

    private var isDragging = false
    private var lastMouseX = 0.0
    private var lastMouseY = 0.0

    override fun load() {
        config = ConfigManager.loadConfig(
            configPath,
            CustomChatConfig::class.java
        ) { CustomChatConfig() }
    }

    override fun register() {
        ClientTickEvents.END_CLIENT_TICK.register {
            if (config.enabled && mc.screen is ChatScreen) {
                handleMouseDrag()
            } else if (isDragging) {
                isDragging = false
            }
        }
    }

    fun toggle() {
        isCustomChatVisible = !isCustomChatVisible
    }

    fun getChatWidth(): Int = config.width.coerceAtLeast(10)

    fun getChatHeight(focused: Boolean): Int {
        return if (focused) {
            config.height.coerceAtLeast(10)
        } else {
            val opt = mc.options.chatHeightUnfocused().get() as Double
            (opt * 160.0 + 20.0).toInt()
        }
    }

    private fun handleMouseDrag() {
        val window = mc.window
        val handle = window.handle()
        val rightButton = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT)

        val rawX = mc.mouseHandler.xpos()
        val rawY = mc.mouseHandler.ypos()
        val scaledMouseX = (rawX * window.guiScaledWidth / window.width).toInt()
        val scaledMouseY = (rawY * window.guiScaledHeight / window.height).toInt()

        if (rightButton == GLFW.GLFW_PRESS) {
            if (!isDragging && isMouseInChatArea(scaledMouseX, scaledMouseY)) {
                isDragging = true
                lastMouseX = scaledMouseX.toDouble()
                lastMouseY = scaledMouseY.toDouble()
            }

            if (isDragging) {
                val deltaX = scaledMouseX - lastMouseX.toInt()
                val deltaY = -(scaledMouseY - lastMouseY.toInt())

                config.width = (config.width + deltaX).coerceAtLeast(10)
                config.height = (config.height + deltaY).coerceAtLeast(10)

                lastMouseX = scaledMouseX.toDouble()
                lastMouseY = scaledMouseY.toDouble()
            }
        } else {
            if (isDragging) {
                ConfigManager.saveConfig("config/modules/customchat.jsonc", config)
            }

            isDragging = false
        }
    }

    private fun isMouseInChatArea(mouseX: Int, mouseY: Int): Boolean {
        if (!isCustomChatVisible) return false
        val window = mc.window

        val chatX = 2
        val chatY = window.guiScaledHeight - getChatHeight(true) - 40
        val w = getChatWidth()
        val h = getChatHeight(true)

        return mouseX >= chatX && mouseX <= chatX + w && mouseY >= chatY && mouseY <= chatY + h
    }

    fun isResizing(): Boolean = isDragging
}





