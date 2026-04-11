package dev.losterixx.ttyclient.client.modules.autoreconnect

import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.config.configs.modules.AutoReconnectConfig
import dev.losterixx.ttyclient.client.modules.ClientModule
import dev.losterixx.ttyclient.client.modules.ModuleCategory
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.DisconnectedScreen
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.gui.screens.ConnectScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.resolver.ServerAddress
import net.minecraft.network.chat.Component

object ReconnectManager : ClientModule {

    override val id = "autoreconnect"
    override val configPath = "config/modules/autoreconnect.jsonc"
    override val displayName = "Auto Reconnect"
    override val description = "Automatically reconnects to the last server after a disconnect."
    override val category = ModuleCategory.UTILS

    var lastServerData: ServerData? = null
    var currentReconnectButton: Button? = null
    private var reconnectTicks = 0

    var config: AutoReconnectConfig = AutoReconnectConfig()
        private set

    override fun load() {
        config = ConfigManager.loadConfig(
            configPath,
            AutoReconnectConfig::class.java
        ) { AutoReconnectConfig() }
    }

    override fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            val screen = mc.screen

            if (screen is DisconnectedScreen && config.enabled && lastServerData != null) {
                val delay = config.delaySeconds * 20
                reconnectTicks++

                val remaining = maxOf(0, delay - reconnectTicks) / 20
                currentReconnectButton?.setMessage(Component.literal("Reconnect (${remaining}s)"))

                if (reconnectTicks >= delay) {
                    reconnect(screen)
                }
            } else if (screen !is DisconnectedScreen) {
                reconnectTicks = 0
                currentReconnectButton = null
            }
        }
    }

    fun reconnect(parent: Screen?) {
        val mc = Minecraft.getInstance()
        val data = lastServerData ?: return
        val address = ServerAddress.parseString(data.ip)
        val parentScreen: Screen = parent ?: JoinMultiplayerScreen(TitleScreen())

        reconnectTicks = 0
        ConnectScreen.startConnecting(parentScreen, mc, address, data, false, null)
    }
}

