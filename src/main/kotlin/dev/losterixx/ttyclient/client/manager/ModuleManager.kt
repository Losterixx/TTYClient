package dev.losterixx.ttyclient.client.manager

import dev.losterixx.ttyclient.client.config.ConfigManager
import dev.losterixx.ttyclient.client.modules.ClientModule

object ModuleManager {

    private val modules = mutableListOf<ClientModule>()

    fun init(vararg mods: ClientModule) {
        modules.addAll(mods)
        mods.forEach { module ->
            ConfigManager.onReload(module.configPath) { module.load() }
        }
    }

    fun loadAll() = modules.forEach { it.load() }
    fun registerAll() = modules.forEach { it.register() }

    operator fun get(id: String): ClientModule? =
        modules.find { it.id.equals(id, ignoreCase = true) }

    fun getAll(): List<ClientModule> = modules.toList()
}

