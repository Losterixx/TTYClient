package dev.losterixx.ttyclient.client.event

import dev.losterixx.ttyclient.client.modules.ClientModule
import net.minecraft.client.Minecraft

data class ClientTickEvent(val mc: Minecraft)
data class ModuleToggleEvent(val module: ClientModule, val enabled: Boolean)
data class ConfigReloadedEvent(val path: String)

