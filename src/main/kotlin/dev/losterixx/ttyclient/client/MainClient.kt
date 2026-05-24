package dev.losterixx.ttyclient.client

import dev.losterixx.ttyclient.client.commandsys.CommandManager
import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.manager.HotkeyManager
import dev.losterixx.ttyclient.client.manager.KeybindManager
import dev.losterixx.ttyclient.client.manager.ModuleManager
import dev.losterixx.ttyclient.client.event.ClientTickEvent
import dev.losterixx.ttyclient.client.event.EventBus
import dev.losterixx.ttyclient.client.modules.autoreconnect.ReconnectManager
import dev.losterixx.ttyclient.client.modules.crosshair.CrosshairManager
import dev.losterixx.ttyclient.client.modules.customchat.ChatManager
import dev.losterixx.ttyclient.client.modules.debug.KeyVisualizerManager
import dev.losterixx.ttyclient.client.modules.freelook.FreelookManager
import dev.losterixx.ttyclient.client.modules.fullbright.FullbrightManager
import dev.losterixx.ttyclient.client.modules.notifications.NotificationManager
import dev.losterixx.ttyclient.client.modules.renderutils.RenderUtilsManager
import dev.losterixx.ttyclient.client.modules.screenshots.ScreenshotManager
import dev.losterixx.ttyclient.client.modules.utils.UtilsManager
import dev.losterixx.ttyclient.client.modules.zoom.ZoomManager
import dev.losterixx.ttyclient.client.ui.Theme
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MainClient : ClientModInitializer {

    const val MOD_ID = "ttyclient"
    const val PREFIX = "§c·» §7"
    val LOGGER: Logger = LoggerFactory.getLogger("TTYClient")
    val VERSION: String by lazy {
        FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .map { it.metadata.version.friendlyString }
            .orElse("?.?.?")
    }

    override fun onInitializeClient() {
        LOGGER.info("Initializing TTYClient...")

        // -> Register modules
        ModuleManager.init(
            FreelookManager,
            ZoomManager,
            CrosshairManager,
            FullbrightManager,
            ChatManager,
            ReconnectManager,
            RenderUtilsManager,
            UtilsManager,
            ScreenshotManager,
            NotificationManager
        )

        // -> Other config watchers
        ConfigManager.onReload("config/theme.jsonc") { Theme.loadFromConfig() }
        ConfigManager.onReload("config/keybinds.jsonc") { KeybindManager.load() }

        // -> Load everything
        ConfigManager.load()
        ModuleManager.loadAll()
        Theme.loadFromConfig()
        ConfigManager.startWatcher()

        // -> Register event listeners
        HotkeyManager.register()
        ModuleManager.registerAll()

        // -> Drive EventBus ClientTickEvent
        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            EventBus.post(ClientTickEvent(mc))
        }

        // -> Keybinds from config
        KeybindManager.load()

        // -> Commands
        CommandManager.init()

        LOGGER.info("TTYClient has been initialized.")
    }

    fun onShutdown() {
        ConfigManager.stopWatcher()
        ConfigManager.save()
    }
}
