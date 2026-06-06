package dev.losterixx.ttyclient.client.modules.hud

import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.hud.CpsHudConfig
import dev.losterixx.ttyclient.client.config.configs.hud.DateHudConfig
import dev.losterixx.ttyclient.client.config.configs.hud.FpsHudConfig
import dev.losterixx.ttyclient.client.config.configs.hud.GeneralHudConfig
import dev.losterixx.ttyclient.client.config.configs.hud.LocationHudConfig
import dev.losterixx.ttyclient.client.config.configs.hud.PingHudConfig
import dev.losterixx.ttyclient.client.config.configs.hud.TimeHudConfig
import dev.losterixx.ttyclient.client.hud.BackgroundStyle
import dev.losterixx.ttyclient.client.hud.HudEditorScreen
import dev.losterixx.ttyclient.client.hud.HudElement
import dev.losterixx.ttyclient.client.hud.elements.*
import dev.losterixx.ttyclient.client.modules.ClientModule
import dev.losterixx.ttyclient.client.modules.ModuleCategory
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWMouseButtonCallbackI

object HudManager : ClientModule {

    override val id = "hud"
    override val configPath = "config/hud/general.jsonc"
    override val displayName = "HUD"
    override val description = "Customizable on-screen display with FPS, CPS, Ping, Location, Time, and Date."
    override val category = ModuleCategory.VISUAL

    var generalConfig: GeneralHudConfig = GeneralHudConfig()
        private set

    var fpsConfig: FpsHudConfig = FpsHudConfig()
        private set

    var pingConfig: PingHudConfig = PingHudConfig()
        private set

    var cpsConfig: CpsHudConfig = CpsHudConfig()
        private set

    var locationConfig: LocationHudConfig = LocationHudConfig()
        private set

    var timeConfig: TimeHudConfig = TimeHudConfig()
        private set

    var dateConfig: DateHudConfig = DateHudConfig()
        private set

    private val leftClickTimestamps = mutableListOf<Long>()
    private val rightClickTimestamps = mutableListOf<Long>()

    val elements: List<HudElement> by lazy {
        listOf(
            FpsElement { fpsConfig },
            PingElement { pingConfig },
            CpsElement({ cpsConfig }, leftCps = { leftClickTimestamps.size }, rightCps = { rightClickTimestamps.size }),
            LocationElement { locationConfig },
            TimeElement { timeConfig },
            DateElement { dateConfig },
        )
    }

    override fun load() {
        generalConfig = ConfigManager.loadConfig(
            "config/hud/general.jsonc",
            GeneralHudConfig::class.java
        ) { GeneralHudConfig() }

        fpsConfig = ConfigManager.loadConfig(
            "config/hud/fps.jsonc",
            FpsHudConfig::class.java
        ) { FpsHudConfig() }

        pingConfig = ConfigManager.loadConfig(
            "config/hud/ping.jsonc",
            PingHudConfig::class.java
        ) { PingHudConfig() }

        cpsConfig = ConfigManager.loadConfig(
            "config/hud/cps.jsonc",
            CpsHudConfig::class.java
        ) { CpsHudConfig() }

        locationConfig = ConfigManager.loadConfig(
            "config/hud/location.jsonc",
            LocationHudConfig::class.java
        ) { LocationHudConfig() }

        timeConfig = ConfigManager.loadConfig(
            "config/hud/time.jsonc",
            TimeHudConfig::class.java
        ) { TimeHudConfig() }

        dateConfig = ConfigManager.loadConfig(
            "config/hud/date.jsonc",
            DateHudConfig::class.java
        ) { DateHudConfig() }
    }

    fun saveAll() {
        ConfigManager.saveConfig("config/hud/general.jsonc", generalConfig)
        ConfigManager.saveConfig("config/hud/fps.jsonc", fpsConfig)
        ConfigManager.saveConfig("config/hud/ping.jsonc", pingConfig)
        ConfigManager.saveConfig("config/hud/cps.jsonc", cpsConfig)
        ConfigManager.saveConfig("config/hud/location.jsonc", locationConfig)
        ConfigManager.saveConfig("config/hud/time.jsonc", timeConfig)
        ConfigManager.saveConfig("config/hud/date.jsonc", dateConfig)
    }

    override fun register() {
        ConfigManager.onReload("config/hud/general.jsonc") { load() }
        ConfigManager.onReload("config/hud/fps.jsonc") { load() }
        ConfigManager.onReload("config/hud/ping.jsonc") { load() }
        ConfigManager.onReload("config/hud/cps.jsonc") { load() }
        ConfigManager.onReload("config/hud/location.jsonc") { load() }
        ConfigManager.onReload("config/hud/time.jsonc") { load() }
        ConfigManager.onReload("config/hud/date.jsonc") { load() }

        ClientTickEvents.END_CLIENT_TICK.register(object : ClientTickEvents.EndTick {
            var initDone = false

            override fun onEndTick(client: Minecraft) {
                if (!initDone && client.window != null) {
                    initDone = true
                    registerMouseCallback(client.window.handle())
                }

                val now = System.currentTimeMillis()
                leftClickTimestamps.removeIf { now - it > 1000 }
                rightClickTimestamps.removeIf { now - it > 1000 }
            }
        })
    }

    private fun registerMouseCallback(windowHandle: Long) {
        val previous = GLFW.glfwSetMouseButtonCallback(windowHandle, null)

        GLFW.glfwSetMouseButtonCallback(windowHandle, GLFWMouseButtonCallbackI { win, button, action, mods ->
            previous?.invoke(win, button, action, mods)

            val mc = Minecraft.getInstance()
            if (mc.screen != null) return@GLFWMouseButtonCallbackI
            if (action != GLFW.GLFW_PRESS) return@GLFWMouseButtonCallbackI

            val now = System.currentTimeMillis()
            if (button == GLFW.GLFW_MOUSE_BUTTON_1) leftClickTimestamps.add(now)
            if (button == GLFW.GLFW_MOUSE_BUTTON_2) rightClickTimestamps.add(now)
        })
    }

    fun renderHud(context: net.minecraft.client.gui.GuiGraphicsExtractor) {
        val client = Minecraft.getInstance()

        if (client.screen is HudEditorScreen) return

        if (!generalConfig.enabled) return

        if ((generalConfig.hideOnF1 && client.options.hideGui) ||
            (generalConfig.hideOnF3 && client.debugOverlay.showDebugScreen()) ||
            (generalConfig.hideOnChat && client.screen?.javaClass?.simpleName == "ChatScreen") ||
            (generalConfig.hideOnESC && client.isPaused)
        ) return

        val scale = generalConfig.textScale
        val sw = (context.guiWidth() / scale).toInt()
        val sh = (context.guiHeight() / scale).toInt()

        val style = BackgroundStyle.entries.find { it.name == generalConfig.backgroundStyle }
            ?: BackgroundStyle.PIXEL
        val radius = generalConfig.backgroundRadius

        context.pose().pushMatrix()
        context.pose().scale(scale, scale)

        for (element in elements) {
            if (element.enabled) {
                element.render(
                    context,
                    sw,
                    sh,
                    generalConfig.backgroundEnabled,
                    0x80000000.toInt(),
                    style,
                    radius,
                )
            }
        }

        context.pose().popMatrix()
    }
}
